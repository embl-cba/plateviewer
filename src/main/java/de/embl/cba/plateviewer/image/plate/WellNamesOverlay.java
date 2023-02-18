package de.embl.cba.plateviewer.image.plate;

import bdv.util.BdvOverlay;
import de.embl.cba.plateviewer.util.Utils;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;

import java.awt.*;
import java.util.Map;

public class WellNamesOverlay extends BdvOverlay
{
	private Map< String, Interval > wellNameToInterval;

	public WellNamesOverlay( Map< String, Interval > wellNameToInterval )
	{
		super();
		this.wellNameToInterval = wellNameToInterval;
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		g.setColor( Color.WHITE );

		final AffineTransform3D sourceToViewerTransform = new AffineTransform3D();
		getCurrentTransform3D( sourceToViewerTransform );

		for ( String wellName : wellNameToInterval.keySet() )
		{
			final Interval wellInterval = wellNameToInterval.get( wellName );

			final Interval viewerInterval = Utils.toViewerInterval( wellInterval, sourceToViewerTransform );

			final int[] dimensions = Intervals.dimensionsAsIntArray( viewerInterval );
			Utils.setFont( g, dimensions, wellName );

			final int offset = dimensions[ 0 ] / 10;

			g.drawString( wellName,
					viewerInterval.min( 0 ) + offset,
					viewerInterval.max( 1 ) - offset);
		}
	}

}

