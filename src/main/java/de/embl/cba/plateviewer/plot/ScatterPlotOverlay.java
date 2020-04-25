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

		drawDiagonal( g, globalToViewerTransform );

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

	private void drawDiagonal( Graphics2D g, AffineTransform3D globalToViewerTransform )
	{
		final double[] zero = new double[ 3 ];
		double[] zeroInViewer = new double[ 3 ];
		globalToViewerTransform.apply( zero, zeroInViewer );

		final double[] one = new double[]{ max, max, 0 };
		double[] oneInViewer = new double[ 3 ];
		globalToViewerTransform.apply( one, oneInViewer );

		g.drawLine(
				(int) zeroInViewer[ 0 ], (int) zeroInViewer[ 1 ],
				(int) oneInViewer[ 0 ], (int) oneInViewer[ 1 ] );
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

