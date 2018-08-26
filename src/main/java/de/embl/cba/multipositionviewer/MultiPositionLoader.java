package de.embl.cba.multipositionviewer;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.util.Intervals;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiPositionLoader implements CellLoader
{
	private final ArrayList< File > files;
	private final String namingScheme;
	private final int[] imageDimensions;
	private final int bitDepth;
	private final int numIoThreads;
	private final ExecutorService executorService;
	private final ArrayList< ImageSource > imageSources;

	public MultiPositionLoader( ArrayList< File > files, String namingScheme, int[] imageDimensions, int bitDepth, int numIoThreads )
	{
		this.files = files;
		this.namingScheme = namingScheme;
		this.imageDimensions = imageDimensions;
		this.bitDepth = bitDepth;
		this.numIoThreads = numIoThreads;
		executorService = Executors.newFixedThreadPool( numIoThreads );

		// TODO: replace by interface and factory
		if ( namingScheme.equals( Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00 ) )
		{
			ImageFileListGeneratorALMFScreening imageFileListGeneratorALMFScreening = new ImageFileListGeneratorALMFScreening( files, imageDimensions );
			imageSources = imageFileListGeneratorALMFScreening.getFileList();
		}
		else if ( namingScheme.equals( Utils.PATTERN_MD_A01_CHANNEL ) )
		{
			ImageFileListGeneratorMDSingleSite imageFileListGeneratorMDSingleSite = new ImageFileListGeneratorMDSingleSite( files, imageDimensions );
			imageSources = imageFileListGeneratorMDSingleSite.getFileList();
		}
		else
		{
			imageSources = null;
		}

	}

	public ImageSource getImageFile( String imageFileName )
	{
		for ( ImageSource imageSource : imageSources )
		{
			if ( imageSource.getFile().getName().equals( imageFileName ) )
			{
				return imageSource;
			}
		}

		return null;
	}


	public ImageSource getImageFile( int index )
	{
		return imageSources.get( index );
	}

	public ArrayList< ImageSource > getImageSources()
	{
		return imageSources;
	}

	@Override
	public void load( final SingleCellArrayImg cell ) throws Exception
	{
		ImageSource imageSource = getImageFile( cell );

		if ( imageSource != null )
		{
			executorService.submit( new Runnable()
			{
				@Override
				public void run()
				{
					loadImageIntoCell( cell, imageSource.getFile() );
				}
			});
		}

	}


	public ImageSource getImageFile( SingleCellArrayImg cell )
	{
		Interval requestedInterval = Intervals.largestContainedInterval( cell );

		for ( ImageSource imageSource : imageSources )
		{
			FinalInterval imageInterval = imageSource.getInterval();

			if ( Utils.isIntersecting( requestedInterval, imageInterval ) )
			{
				return imageSource;
			}
		}

		return null;
	}

	public ImageSource getImageFile( long[] coordinates )
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

			if ( matches )
			{
				return imageSource;
			}

		}

		return null;
	}


	private void loadImageIntoCell( SingleCellArrayImg cell, File file )
	{
		Utils.debug( "Loading: " + file.getName() );

		// TODO: check for the data type of the loaded cell (cell.getFirstElement())

		final ImagePlus imp = IJ.openImage( file.getAbsolutePath() );

		if ( bitDepth == 8 )
		{
			final byte[] impdata = ( byte[] ) imp.getProcessor().getPixels();
			final byte[] celldata = ( byte[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
		else if ( bitDepth == 16 )
		{
			final short[] impdata = ( short[] ) imp.getProcessor().getPixels();
			final short[] celldata = ( short[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
		else if ( bitDepth == 32 )
		{
			final float[] impdata = ( float[] ) imp.getProcessor().getPixels();
			final float[] celldata = ( float[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
	}

}
