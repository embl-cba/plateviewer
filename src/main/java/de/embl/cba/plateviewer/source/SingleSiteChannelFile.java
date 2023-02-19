package de.embl.cba.plateviewer.source;

import net.imglib2.FinalInterval;

import java.io.File;

public class SingleSiteChannelFile
{
	private final File file;
	private String hdf5DataSetName;
	private final FinalInterval interval;
	private final String siteName;
	private final String wellName;
	private String siteInformation = "";
	private String wellInformation = "";

	public SingleSiteChannelFile( File file, String hdf5DataSetName, FinalInterval interval, String siteName, String wellName )
	{
		this.file = file;
		this.hdf5DataSetName = hdf5DataSetName;
		this.interval = interval;
		this.siteName = siteName;
		this.wellName = wellName;
	}

	public SingleSiteChannelFile( File file, FinalInterval interval, String siteName, String wellName )
	{
		this.file = file;
		this.interval = interval;
		this.siteName = siteName;
		this.wellName = wellName;
	}

	public String getSiteName()
	{
		return siteName;
	}

	public String getSiteInformation()
	{
		return siteInformation;
	}

	public String getWellInformation()
	{
		return wellInformation;
	}

	public String getWellName()
	{
		return wellName;
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
