package de.embl.cba.plateviewer.image.channel;

import bdv.util.BdvOverlay;
import bdv.util.BdvSource;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.sources.Metadata;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;

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
