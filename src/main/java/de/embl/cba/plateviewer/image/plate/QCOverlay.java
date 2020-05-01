package de.embl.cba.plateviewer.image.plate;


import bdv.util.BdvOverlay;
import de.embl.cba.plateviewer.Graphics;
import de.embl.cba.plateviewer.Utils;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform2D;

import java.awt.*;
import java.util.HashMap;
import java.util.Set;

public class QCOverlay extends BdvOverlay
{
	private final HashMap< String, Integer > locationNameToQC;
	private final HashMap< String, Interval > locationNameToInterval;
	private Set< String > locationNames;

	public QCOverlay( HashMap< String, Integer > locationNameToQC, HashMap< String, Interval > locationNameToInterval )
	{
		super();
		this.locationNameToQC = locationNameToQC;
		this.locationNameToInterval = locationNameToInterval;
		locationNames = this.locationNameToInterval.keySet();
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		final AffineTransform2D globalToViewerTransform = new AffineTransform2D();
		getCurrentTransform2D( globalToViewerTransform );

		for ( String name : locationNames )
		{
			if ( locationNameToQC.get( name ) == 0 )
			{
				continue;
			}

			final Interval globalInterval = locationNameToInterval.get( name );
			final Interval viewerInterval = Utils.createViewerInterval( globalToViewerTransform, globalInterval );

			g.setColor( Color.RED );
			Graphics.drawCross( g, viewerInterval, 0.1, 3 );

//			final int fontSize = Utils.setFont( g, dimensions, "X" );
//			final int offset = ( dimensions[ 0 ] - fontSize ) / 2;
//			g.setColor( Color.RED );
//			g.drawString( "X",
//					viewerInterval.min( 0 ) + offset,
//					viewerInterval.max( 1 ) - offset);
		}
	}

}

