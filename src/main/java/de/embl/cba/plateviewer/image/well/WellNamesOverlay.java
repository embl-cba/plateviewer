package de.embl.cba.plateviewer.image.well;


import bdv.util.Bdv;
import bdv.util.BdvOverlay;
import de.embl.cba.plateviewer.image.channel.BdvViewable;
import de.embl.cba.plateviewer.view.PlateViewerImageView;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;

public class WellNamesOverlay extends BdvOverlay
{
	final Bdv bdv;
	private final PlateViewerImageView< ?, ? > plateViewer;

	public WellNamesOverlay( PlateViewerImageView< ?, ? > plateViewer )
	{
		super();
		this.bdv = plateViewer.getBdv();
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
			final int fontSize = Math.min( screenWellSize[ 0 ], screenWellSize[ 1 ] ) / 2;
			g.setFont( new Font("TimesRoman", Font.PLAIN, fontSize ) );
			g.drawString( wellName,
					(int) screenWellPosMin[ 0 ] + offset,
					(int) screenWellPosMax[ 1 ] - offset);
		}
	}
}

