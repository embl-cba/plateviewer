package de.embl.cba.plateviewer.image.cellloader;

import de.embl.cba.plateviewer.util.Utils;
import de.embl.cba.plateviewer.image.SingleSiteChannelFile;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.util.Intervals;

import java.util.ArrayList;

public abstract class MultiSiteLoader implements CellLoader
{
	protected final ArrayList< SingleSiteChannelFile > singleSiteChannelFiles;

	public MultiSiteLoader( ArrayList< SingleSiteChannelFile > singleSiteChannelFiles )
	{
		this.singleSiteChannelFiles = singleSiteChannelFiles;
	}

	public SingleSiteChannelFile getSingleSiteFile( String siteName )
	{
		for ( SingleSiteChannelFile singleSiteChannelFile : singleSiteChannelFiles )
			if ( singleSiteChannelFile.getSiteName().equals( siteName ) )
				return singleSiteChannelFile;

		throw new UnsupportedOperationException( "Could not find image " + siteName );
	}

	public SingleSiteChannelFile getSingleSiteFile( int index )
	{
		return singleSiteChannelFiles.get( index );
	}

	public ArrayList< SingleSiteChannelFile > getSingleSiteChannelFiles()
	{
		return singleSiteChannelFiles;
	}

	public SingleSiteChannelFile getSingleSiteFile( Interval cell )
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

	public SingleSiteChannelFile getSingleSiteFile( long[] coordinates )
	{
		final Point point = new Point( coordinates[ 0 ], coordinates[ 1 ] );
		for ( SingleSiteChannelFile singleSiteChannelFile : singleSiteChannelFiles )
			if ( Intervals.contains( singleSiteChannelFile.getInterval(), point ) )
				return singleSiteChannelFile;

		return null;
	}
}
