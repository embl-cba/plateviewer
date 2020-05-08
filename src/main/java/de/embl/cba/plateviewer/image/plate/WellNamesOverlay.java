package de.embl.cba.plateviewer.image.plate;

import bdv.util.BdvOverlay;
import de.embl.cba.plateviewer.util.Utils;
import de.embl.cba.plateviewer.view.ImagePlateViewer;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.util.Intervals;

import java.awt.*;
import java.util.Map;

public class WellNamesOverlay extends BdvOverlay
{
	private final ImagePlateViewer< ?, ? > plateViewer;

	public WellNamesOverlay( ImagePlateViewer< ?, ? > plateViewer )
	{
		super();
		this.plateViewer = plateViewer;
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		g.setColor( Color.WHITE );

		final AffineTransform2D globalToViewerTransform = new AffineTransform2D();
		getCurrentTransform2D( globalToViewerTransform );

		final Map< String, Interval > wellNameToInterval = plateViewer.getWellNameToInterval();

		for ( String wellName : wellNameToInterval.keySet() )
		{
			final Interval globalInterval = wellNameToInterval.get( wellName );

			final Interval viewerInterval = Utils.createViewerInterval( globalToViewerTransform, globalInterval );

			final int[] dimensions = Intervals.dimensionsAsIntArray( viewerInterval );
			Utils.setFont( g, dimensions, wellName );

			final int offset = dimensions[ 0 ] / 10;

			g.drawString( wellName,
					viewerInterval.min( 0 ) + offset,
					viewerInterval.max( 1 ) - offset);
		}
	}

}

