package de.embl.cba.plateviewer.image.well;

import bdv.util.BdvOverlay;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.image.channel.BdvViewable;
import de.embl.cba.tables.color.ColorUtils;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;

import java.awt.*;

public class OverlayBdvViewable implements BdvViewable
{
	private final BdvOverlay overlay;
	private final String name;

	public OverlayBdvViewable( BdvOverlay overlay, String name )
	{
		this.overlay = overlay;
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public ARGBType getColor()
	{
		return ColorUtils.getARGBType( Color.GRAY );
	}

	@Override
	public double[] getContrastLimits()
	{
		return new double[]{ 0, 255 };
	}

	@Override
	public RandomAccessibleInterval< ? > getRAI()
	{
		return null;
	}

	@Override
	public Source< ? > getSource()
	{
		return null;
	}

	@Override
	public BdvOverlay getOverlay()
	{
		return overlay;
	}

	@Override
	public boolean isInitiallyVisible()
	{
		return true;
	}

	@Override
	public Metadata.Type getType()
	{
		return Metadata.Type.Image;
	}
}
