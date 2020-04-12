package de.embl.cba.plateviewer.imagesources;

import net.imglib2.FinalInterval;

import java.io.File;

public class ImageSource
{
	private final File file;
	private String hdf5DataSetName;
	private final FinalInterval interval;
	private final String siteName;
	private final String wellName;

	public ImageSource( File file, String hdf5DataSetName, FinalInterval interval, String siteName, String wellName )
	{
		this.file = file;
		this.hdf5DataSetName = hdf5DataSetName;
		this.interval = interval;
		this.siteName = siteName;
		this.wellName = wellName;
	}

	public ImageSource( File file, FinalInterval interval, String siteName, String wellName )
	{
		this.file = file;
		this.interval = interval;
		this.siteName = siteName;
		this.wellName = wellName;
	}

	public String getImageName()
	{
		return siteName;
	}

	public String getWellName()
	{
		return wellName;
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

	public String getHdf5DataSetName()
	{
		return hdf5DataSetName;
	}
}
