package de.embl.cba.plateviewer.loaders;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.imagesources.ImageSource;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiPositionLoader implements CellLoader
{
	private final ExecutorService executorService;
	private final ArrayList< ImageSource > imageSources;

	public MultiPositionLoader( ArrayList< ImageSource > imageSources, int numIoThreads )
	{
		this.imageSources = imageSources;
		executorService = Executors.newFixedThreadPool( numIoThreads );
	}

	public ImageSource getImageSource( String imageFileName )
	{
		for ( ImageSource imageSource : imageSources )
			if ( imageSource.getFile().getName().equals( imageFileName ) )
				return imageSource;

		return null;
	}

	public ImageSource getImageSource( int index )
	{
		return imageSources.get( index );
	}

	public ArrayList< ImageSource > getImageSources()
	{
		return imageSources;
	}

	@Override
	public synchronized void load( final SingleCellArrayImg cell )
	{
		ImageSource imageSource = getImageSource( cell );

		if ( imageSource != null )
		{
			if ( imageSource.getHdf5DataSetName() != null )
			{
				loadImageIntoCellUsingJHdf5( cell, imageSource.getFile(), imageSource.getHdf5DataSetName() );
			}
			else
			{
				loadImageIntoCellUsingIJOpenImage( cell, imageSource.getFile() );
			}
		}
	}

	private void loadImageIntoCellUsingJHdf5( SingleCellArrayImg cell, File file, String hdf5DataSetName )
	{
		final IHDF5Reader hdf5Reader = HDF5Factory.openForReading( file );
		final HDF5DataSetInformation information = hdf5Reader.getDataSetInformation( hdf5DataSetName );
		final String dataType = information.getTypeInformation().toString();

		if ( dataType.equals( Utils.H5_UNSIGNED_BYTE ) )
		{
			final byte[] data = hdf5Reader.int8().readArray( hdf5DataSetName );
			final byte[] celldata = ( byte[] ) cell.getStorageArray();
			System.arraycopy( data, 0, celldata, 0, celldata.length );
		}
		else if ( dataType.equals( Utils.H5_UNSIGNED_SHORT ) )
		{
			final short[] data = hdf5Reader.int16().readArray( hdf5DataSetName );
			final short[] celldata = ( short[] ) cell.getStorageArray();
			System.arraycopy( data, 0, celldata, 0, celldata.length );
		}
		else if ( dataType.equals( Utils.H5_UNSIGNED_INT ) )
		{
			final int[] data = hdf5Reader.int32().readArray( hdf5DataSetName );
			final int[] celldata = ( int[] ) cell.getStorageArray();
			System.arraycopy( data, 0, celldata, 0, celldata.length );
		}
		else if ( dataType.equals( Utils.H5_FLOAT ) )
		{
			final float[] data = hdf5Reader.float32().readArray( hdf5DataSetName );
			final float[] celldata = ( float[] ) cell.getStorageArray();
			System.arraycopy( data, 0, celldata, 0, celldata.length );
		}
		else
		{
			throw new UnsupportedOperationException( "Hdf5 datatype not supported: " + dataType );
		}
	}

	public ImageSource getImageSource( SingleCellArrayImg cell )
	{
		Interval requestedInterval = Intervals.largestContainedInterval( cell );

		for ( ImageSource imageSource : imageSources )
		{
			FinalInterval imageInterval = imageSource.getInterval();

			if ( Utils.areIntersecting( requestedInterval, imageInterval ) )
				return imageSource;
		}

		return null;
	}

	public ImageSource getImageSource( long[] coordinates )
	{
		boolean matches = false;

		for ( ImageSource imageSource : imageSources )
		{
			FinalInterval interval = imageSource.getInterval();

			for ( int d = 0; d < interval.numDimensions(); ++d )
			{
				if ( interval.min( d ) <= coordinates[ d ] && coordinates[ d ] <= interval.max( d ) )
				{
					matches = true;
				}
				else
				{
					matches = false;
					break;
				}
			}

			if ( matches ) return imageSource;
		}
		return null;
	}

	private void loadImageIntoCellUsingIJOpenImage( SingleCellArrayImg< ? , ? > cell, File file )
	{
		Utils.debug( "Loading: " + file.getName() );

		// TODO: check for the data type of the cell (cell.getFirstElement())

		final ImagePlus imp = IJ.openImage( file.getAbsolutePath() );

		if ( imp.getBitDepth() == 8 )
		{
			final byte[] impdata = ( byte[] ) imp.getProcessor().getPixels();
			final byte[] celldata = ( byte[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
		else if ( imp.getBitDepth() == 16 )
		{
			final short[] impdata = ( short[] ) imp.getProcessor().getPixels();
			final short[] celldata = ( short[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
		else if ( imp.getBitDepth() == 24 ) // RGB
		{
			// Compute sum of RGB values and return as short array

			final byte[][] imgDataRGB = new byte[3][];
			for ( int c = 0; c < 3; c++ )
			{
				imgDataRGB[ c ] = ( byte[] ) ((ColorProcessor )imp.getProcessor()).getChannel( c + 1 );
			}

			final short[] celldata = ( short[] ) cell.getStorageArray();

			for ( int i = 0; i < imgDataRGB[ 0 ].length; i++ )
			{
				for ( int c = 0; c < 3; c++ )
				{
					celldata[ i ] += imgDataRGB[ c ][ i ] & 0xFF;
				}
			}
		}
		else if ( imp.getBitDepth() == 32 )
		{
			final float[] impdata = ( float[] ) imp.getProcessor().getPixels();
			final float[] celldata = ( float[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
	}
}
