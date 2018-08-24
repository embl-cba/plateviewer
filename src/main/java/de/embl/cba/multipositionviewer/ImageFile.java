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
