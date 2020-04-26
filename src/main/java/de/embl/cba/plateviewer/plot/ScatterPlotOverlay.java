package de.embl.cba.plateviewer.plot;


import bdv.util.BdvHandle;
import bdv.util.BdvOverlay;
import net.imglib2.FinalInterval;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.*;

import static de.embl.cba.plateviewer.Utils.bdvTextOverlayFontSize;

public class ScatterPlotOverlay extends BdvOverlay
{
	private final BdvHandle bdvHandle;
	private final String columnNameX;
	private final String columnNameY;
	private final FinalInterval scatterPlotInterval;
	private final long max;

	public ScatterPlotOverlay( BdvHandle bdvHandle, String columnNameX, String columnNameY, FinalInterval scatterPlotInterval )
	{
		super();
		this.bdvHandle = bdvHandle;
		this.columnNameX = columnNameX;
		this.columnNameY = columnNameY;
		this.scatterPlotInterval = scatterPlotInterval;
		max = Math.max( scatterPlotInterval.max( 0 ), scatterPlotInterval.max( 1 ) );
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		g.setColor( Color.WHITE );

		final AffineTransform3D globalToViewerTransform = new AffineTransform3D();
		getCurrentTransform3D( globalToViewerTransform );

		drawDiagonalAndFrame( g, globalToViewerTransform );

		drawAxisLabels( g );
	}

	private void drawAxisLabels( Graphics2D g )
	{
		int fontSize = bdvTextOverlayFontSize;

		g.setFont( new Font("MonoSpaced", Font.PLAIN, fontSize ) );

		int bdvWindowHeight = bdvHandle.getViewerPanel().getDisplay().getHeight();
		int bdvWindowWidth = bdvHandle.getViewerPanel().getDisplay().getWidth();

		int distanceToWindowBottom = 2 * ( fontSize + 5 );

		g.drawString( "Y: " + columnNameY,
				bdvWindowWidth / 3,
				bdvWindowHeight - distanceToWindowBottom  );

		distanceToWindowBottom = 1 * ( fontSize + 5 );

		g.drawString( "X: " + columnNameX,
				bdvWindowWidth / 3,
				bdvWindowHeight - distanceToWindowBottom );
	}

	private void drawDiagonalAndFrame( Graphics2D g, AffineTransform3D globalToViewerTransform )
	{
		double[] min = new double[ 3 ];
		globalToViewerTransform.apply( new double[ 3 ], min );

		final double[] one = new double[]{ max, max, 0 };
		double[] max = new double[ 3 ];
		globalToViewerTransform.apply( one, max );

		g.setColor( Color.WHITE );

		g.drawLine(
				(int) min[ 0 ], (int) min[ 1 ],
				(int) max[ 0 ], (int) max[ 1 ] );

//		g.drawLine(
//				(int) min[ 0 ], (int) min[ 1 ],
//				(int) min[ 0 ], (int) max[ 1 ] );
//
//		g.drawLine(
//				(int) min[ 0 ], (int) min[ 1 ],
//				(int) max[ 0 ], (int) min[ 1 ] );
//
//		g.drawLine(
//				(int) min[ 0 ], (int) max[ 1 ],
//				(int) max[ 0 ], (int) max[ 1 ] );
//
//		g.drawLine(
//				(int) max[ 0 ], (int) min[ 1 ],
//				(int) max[ 0 ], (int) max[ 1 ] );

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

