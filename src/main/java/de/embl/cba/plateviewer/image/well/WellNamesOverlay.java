package de.embl.cba.plateviewer.image.well;


import bdv.util.Bdv;
import bdv.util.BdvOverlay;
import de.embl.cba.plateviewer.view.ImagePlateViewer;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;

public class WellNamesOverlay extends BdvOverlay
{
	final Bdv bdv;
	private final ImagePlateViewer< ?, ? > plateViewer;

	public WellNamesOverlay( ImagePlateViewer< ?, ? > plateViewer )
	{
		super();
		this.bdv = plateViewer.getBdvHandle();
		this.plateViewer = plateViewer;
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		g.setColor( Color.WHITE );

		final AffineTransform3D globalToViewerTransform = new AffineTransform3D();
		getCurrentTransform3D( globalToViewerTransform );

		final long[] globalWellPosMin = new long[ 3 ];
		final long[] globalWellPosMax = new long[ 3 ];

		final double[] screenWellPosMin = new double[ 3 ];
		final double[] screenWellPosMax = new double[ 3 ];

		final int[] screenWellSize = new int[ 2 ];

		final HashMap< String, Interval > wellNameToInterval = plateViewer.getWellNameToInterval();

		for ( String wellName : wellNameToInterval.keySet() )
		{
			final Interval interval = wellNameToInterval.get( wellName );

			interval.min( globalWellPosMin );
			interval.max( globalWellPosMax );

			globalToViewerTransform.apply(
					Arrays.stream( globalWellPosMin ).mapToDouble( x -> x ).toArray(),
					screenWellPosMin );
			globalToViewerTransform.apply(
					Arrays.stream( globalWellPosMax ).mapToDouble( x -> x ).toArray(),
					screenWellPosMax );

			for ( int d = 0; d < 2; d++ )
			{
				screenWellSize[ d ] = (int) ( screenWellPosMax[ d ] - screenWellPosMin[ d ] );
			}

			final int offset = screenWellSize[ 0 ] / 10;

			int fontSize = Math.min( screenWellSize[ 0 ], screenWellSize[ 1 ] ) / 2;

			Font font = new Font( "TimesRoman", Font.PLAIN, fontSize );
			g.setFont( font );

			font = getAdaptedSizeFont( g, screenWellSize[ 0 ], wellName, fontSize );
			g.setFont( font );

			g.drawString( wellName,
					(int) screenWellPosMin[ 0 ] + offset,
					(int) screenWellPosMax[ 1 ] - offset);
		}
	}

	private Font getAdaptedSizeFont( Graphics2D g, int i, String wellName, int fontSize )
	{
		Font font;
		final int stringWidth = g.getFontMetrics().stringWidth( wellName );
		if ( stringWidth > i )
		{
			fontSize *= 0.8 * i / stringWidth;
		}

		font = new Font( "TimesRoman", Font.PLAIN, fontSize );
		return font;
	}
}

