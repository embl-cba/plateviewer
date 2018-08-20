package de.embl.cba.multipositionviewer;

import net.imglib2.FinalInterval;

import java.io.File;

public class ImageFile
{
	public File file;
	public FinalInterval interval;

	public long[] getCenter()
	{
		int n = interval.numDimensions();
		final long[] center = new long[ n ];

		for ( int d = 0; d < n; ++d )
		{
			center[ d ] = interval.min( d ) + interval.dimension( d ) / 2;
		}

		return center;
	}

	public long[] getDimensions()
	{
		int n = interval.numDimensions();
		final long[] dimensions = new long[ n ];

		interval.dimensions( dimensions );

		return dimensions;
	}

	public File getFile()
	{
		return file;
	}

	public FinalInterval getInterval()
	{
		return interval;
	}


}
