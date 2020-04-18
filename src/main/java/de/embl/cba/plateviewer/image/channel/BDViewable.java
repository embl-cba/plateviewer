package de.embl.cba.plateviewer.image.channel;

import bdv.viewer.Source;
import de.embl.cba.bdv.utils.sources.Metadata;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;

public interface BDViewable
{
	String getName();

	ARGBType getColor();

	double[] getContrastLimits();

	RandomAccessibleInterval< ? > getRAI();

	Source< ? > getSource();

	boolean isInitiallyVisible();

	Metadata.Type getType();
}
