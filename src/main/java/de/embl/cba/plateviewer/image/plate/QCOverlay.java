package de.embl.cba.plateviewer.image.plate;

import bdv.util.BdvOverlay;
import de.embl.cba.plateviewer.util.Graphics;
import de.embl.cba.plateviewer.util.Utils;
import de.embl.cba.plateviewer.table.AnnotatedInterval;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform2D;

import java.awt.*;
import java.util.List;

public class QCOverlay < T extends AnnotatedInterval > extends BdvOverlay
{
	private final List< T > sites;

	public QCOverlay( List< T > sites )
	{
		super();
		this.sites = sites;
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		final AffineTransform2D globalToViewerTransform = new AffineTransform2D();
		getCurrentTransform2D( globalToViewerTransform );

		for ( T site : sites )
		{
			if ( site.isOutlier() )
			{
				final Interval globalInterval = site.getInterval();
				final Interval viewerInterval = Utils.createViewerInterval( globalToViewerTransform, globalInterval );

				g.setColor( Color.RED );
				Graphics.drawCross( g, viewerInterval, 0.1, 2 );
			}
		}
	}

}

