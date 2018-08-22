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
	final ArrayList< File > files;
	final String namingScheme;
	final int[] imageDimensions;
	final int bitDepth;
	final int numIoThreads;
	final ExecutorService executorService;

	final ArrayList< ImageFile > imageFiles;

	public MultiPositionLoader( ArrayList< File > files, String namingScheme, int[] imageDimensions, int bitDepth, int numIoThreads )
	{
		this.files = files;
		this.namingScheme = namingScheme;
		this.imageDimensions = imageDimensions;
		this.bitDepth = bitDepth;
		this.numIoThreads = numIoThreads;
		executorService = Executors.newFixedThreadPool( numIoThreads );

		if ( namingScheme.equals( Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00 ) )
		{
			ImageFileListGeneratorALMFScreening ImageFileListGeneratorALMFScreening = new ImageFileListGeneratorALMFScreening( files, imageDimensions );
			imageFiles = ImageFileListGeneratorALMFScreening.getFileList();
		}
		else if ( namingScheme.equals( Utils.PATTERN_MD_A01_CHANNEL ) )
		{
			ImageFileListGeneratorALMFScreening ImageFileListGeneratorALMFScreening = new ImageFileListGeneratorALMFScreening( files, imageDimensions );
			imageFiles = ImageFileListGeneratorALMFScreening.getFileList();
		}
		else
		{
			imageFiles = null;
		}

	}

	public ImageFile getImageFile( int index )
	{
		return imageFiles.get( index );
	}

	public ArrayList< ImageFile > getImageFiles()
	{
		return imageFiles;
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
		Interval requestedInterval = Intervals.largestContainedInterval( cell );

		for ( ImageFile imageFile : imageFiles )
		{
			FinalInterval imageInterval = imageFile.getInterval();
			FinalInterval intersect = Intervals.intersect( requestedInterval, imageInterval );

			int n = intersect.numDimensions();

			boolean isIntersecting = true;

			for ( int d = 0; d < n; ++d )
			{
				if ( intersect.dimension( d ) <= 0 )
				{
					isIntersecting = false;
				}
			}

			if ( isIntersecting )
			{
				return imageFile;
			}
		}

		return null;
	}

	public ImageFile getImageFile( long[] coordinates )
	{
		boolean matches = false;

		for ( ImageFile imageFile : imageFiles )
		{

			FinalInterval interval = imageFile.getInterval();

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
