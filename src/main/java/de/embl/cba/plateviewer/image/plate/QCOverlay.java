package de.embl.cba.plateviewer.image.plate;

import bdv.util.BdvOverlay;
import de.embl.cba.plateviewer.util.Graphics;
import de.embl.cba.plateviewer.util.Utils;
import de.embl.cba.plateviewer.table.AnnotatedInterval;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.*;
import java.util.List;

public class QCOverlay < T extends AnnotatedInterval > extends BdvOverlay
{
	private final List< T > sites;

	public QCOverlay( List sites )
	{
		super();
		this.sites = sites;
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		final AffineTransform3D sourceToViewerTransform = new AffineTransform3D();
		getCurrentTransform3D( sourceToViewerTransform );

		for ( T site : sites )
		{
			if ( site.isOutlier() )
			{
				final Interval siteInterval = site.getInterval();
				final Interval viewerInterval = Utils.toViewerInterval( siteInterval, sourceToViewerTransform );
				g.setColor( Color.RED );
				Graphics.drawCross( g, viewerInterval, 0.1, 2 );
			}
		}
	}

}

