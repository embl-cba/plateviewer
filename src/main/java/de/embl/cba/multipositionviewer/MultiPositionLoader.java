package de.embl.cba.multipositionviewer;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiPositionLoader implements CellLoader
{
	final ArrayList< File > files;
	final String multipositionFilenamePattern;
	final int[] imageDimensions;
	final int bitDepth;
	final int numIoThreads;
	final ExecutorService executorService;

	final ArrayList< ImageFile > imageFileList;

	public MultiPositionLoader( ArrayList< File > files, String multipositionFilenamePattern, int[] imageDimensions, int bitDepth, int numIoThreads )
	{
		this.files = files;
		this.multipositionFilenamePattern = multipositionFilenamePattern;
		this.imageDimensions = imageDimensions;
		this.bitDepth = bitDepth;
		this.numIoThreads = numIoThreads;
		executorService = Executors.newFixedThreadPool( numIoThreads );

		if ( multipositionFilenamePattern.equals( Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00 ) )
		{
			ImageFileListGeneratorALMFScreening ImageFileListGeneratorALMFScreening = new ImageFileListGeneratorALMFScreening( files, imageDimensions );
			imageFileList = ImageFileListGeneratorALMFScreening.getList();
		}
		else
		{
			imageFileList = null;
		}

	}

	public ImageFile getImageFile( int index )
	{
		return imageFileList.get( index );
	}


	@Override
	public void load( final SingleCellArrayImg cell ) throws Exception
	{
		ImageFile imageFile = getImageFile( cell );

		if ( imageFile != null )
		{
			executorService.submit( new Runnable()
			{
				@Override
				public void run()
				{
					loadImageIntoCell( cell, imageFile.file );
				}
			});
		}

	}


	private ImageFile getImageFile( SingleCellArrayImg cell )
	{

		long[] coordinates = new long[ 2 ];
		cell.min( coordinates );

		return getImageFile( coordinates );

	}

	public ImageFile getImageFile( long[] coordinates )
	{
		for ( ImageFile imageFile : imageFileList )
		{
			boolean matches = true;

			for ( int d = 0; d < 2; ++d )
			{
				if ( ! ( imageFile.centerCoordinates[ d ] >= coordinates[ d ] && imageFile.centerCoordinates[ d ] <= coordinates[ d ] ) )
				{
					matches = false;
				}
			}

			if ( matches )
			{
				return imageFile;
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
