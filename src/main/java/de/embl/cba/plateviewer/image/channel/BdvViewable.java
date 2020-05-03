package de.embl.cba.plateviewer.image.channel;

import bdv.util.BdvOverlay;
import bdv.util.BdvSource;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.sources.Metadata;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;

public interface BdvViewable
{
	String getName();

	ARGBType getColor();

	double[] getContrastLimits();

	RandomAccessibleInterval< ? > getRAI();

	Source< ? > getSource();

	BdvOverlay getOverlay();

	boolean isInitiallyVisible();

	Metadata.Type getType();

	void setBdvSource( BdvSource bdvSource );

	BdvSource getBdvSource();
}
