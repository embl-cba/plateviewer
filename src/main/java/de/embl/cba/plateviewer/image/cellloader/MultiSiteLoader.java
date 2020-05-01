package de.embl.cba.plateviewer.image.cellloader;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.image.SingleSiteChannelFile;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.util.Intervals;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class MultiSiteLoader implements CellLoader
{
	protected final ArrayList< SingleSiteChannelFile > singleSiteChannelFiles;

	public MultiSiteLoader( ArrayList< SingleSiteChannelFile > singleSiteChannelFiles )
	{
		this.singleSiteChannelFiles = singleSiteChannelFiles;
	}

	public SingleSiteChannelFile getChannelSource( String siteName )
	{
		for ( SingleSiteChannelFile singleSiteChannelFile : singleSiteChannelFiles )
			if ( singleSiteChannelFile.getSiteName().equals( siteName ) )
				return singleSiteChannelFile;

		throw new UnsupportedOperationException( "Could not find image " + siteName );
	}

	public SingleSiteChannelFile getChannelSource( int index )
	{
		return singleSiteChannelFiles.get( index );
	}

	public ArrayList< SingleSiteChannelFile > getSingleSiteChannelFiles()
	{
		return singleSiteChannelFiles;
	}


	public SingleSiteChannelFile getChannelSource( Interval cell )
	{
		Interval requestedInterval = Intervals.largestContainedInterval( cell );

		for ( SingleSiteChannelFile singleSiteChannelFile : singleSiteChannelFiles )
		{
			FinalInterval imageInterval = singleSiteChannelFile.getInterval();

			if ( Utils.areIntersecting( requestedInterval, imageInterval ) )
				return singleSiteChannelFile;
		}

		return null;
	}

	public SingleSiteChannelFile getChannelSource( long[] coordinates )
	{
		boolean matches = false;

		for ( SingleSiteChannelFile singleSiteChannelFile : singleSiteChannelFiles )
		{
			FinalInterval interval = singleSiteChannelFile.getInterval();

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

			if ( matches ) return singleSiteChannelFile;
		}
		return null;
	}
}
