package de.embl.cba.multipositionviewer;

import net.imglib2.FinalInterval;

import java.io.File;

public class ImageFile
{
	private final File file;
	private final FinalInterval interval;
	private final String positionName;

	public ImageFile( File file, FinalInterval interval, String positionName )
	{
		this.file = file;
		this.interval = interval;
		this.positionName = positionName;
	}

	public String getPositionName()
	{
		return positionName;
	}

	public double[] getCenter()
	{
		int n = interval.numDimensions();
		final double[] center = new double[ n ];

		for ( int d = 0; d < n; ++d )
		{
			center[ d ] = interval.min( d ) + interval.dimension( d ) / 2.0;
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
