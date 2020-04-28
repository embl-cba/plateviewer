package de.embl.cba.plateviewer.image.plate;


import bdv.util.BdvOverlay;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.view.ImagePlateViewer;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.util.Intervals;

import java.awt.*;
import java.util.HashMap;

public class SiteQCOverlay extends BdvOverlay
{
	private final ImagePlateViewer< ?, ? > plateViewer;

	public SiteQCOverlay( ImagePlateViewer< ?, ? > plateViewer )
	{
		super();
		this.plateViewer = plateViewer;
	}

	@Override
	protected void draw( final Graphics2D g )
	{

		final AffineTransform2D globalToViewerTransform = new AffineTransform2D();
		getCurrentTransform2D( globalToViewerTransform );

		final HashMap< String, Interval > siteNameToInterval = plateViewer.getSiteNameToInterval();

		for ( String siteName : siteNameToInterval.keySet() )
		{
			final Interval globalInterval = siteNameToInterval.get( siteName );
			final Interval viewerInterval = Utils.createViewerInterval( globalToViewerTransform, globalInterval );

			final int[] dimensions = Intervals.dimensionsAsIntArray( viewerInterval );
			Utils.setFont( g, dimensions, "X" );

			final int offset = dimensions[ 0 ] / 10;

			g.setColor( Color.RED );
			g.drawString( "X",
					viewerInterval.min( 0 ) + offset,
					viewerInterval.max( 1 ) - offset);
		}
	}

}

