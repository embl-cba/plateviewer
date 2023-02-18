package de.embl.cba.plateviewer.image.channel;

import bdv.util.BdvSource;

public abstract class AbstractBdvViewable implements BdvViewable
{
	private BdvSource bdvSource;

	@Override
	public void setBdvSource( BdvSource bdvSource )
	{
		this.bdvSource = bdvSource;
	}

	public BdvSource getBdvSource()
	{
		return bdvSource;
	}
}
