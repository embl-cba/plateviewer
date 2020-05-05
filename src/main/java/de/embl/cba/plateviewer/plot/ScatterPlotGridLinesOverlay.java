package de.embl.cba.plateviewer.plot;


import bdv.util.BdvHandle;
import bdv.util.BdvOverlay;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.*;

import static de.embl.cba.plateviewer.Utils.bdvTextOverlayFontSize;

public class ScatterPlotGridLinesOverlay extends BdvOverlay
{
	public static final String NONE = "None";
	public static final String Y_NX = "y = n * x";
	public static final String Y_N = "y = n";

	private final BdvHandle bdvHandle;
	private final String columnNameX;
	private final String columnNameY;
	private final Interval scatterPlotInterval;
	private final long dataMaxValue;
	private final String lineOverlay;

	// TODO: make an own overlay for the axis labels (columnNameX, columnNameY)
	public ScatterPlotGridLinesOverlay( BdvHandle bdvHandle, String columnNameX, String columnNameY, Interval scatterPlotInterval, String lineOverlay )
	{
		super();
		this.bdvHandle = bdvHandle;
		this.columnNameX = columnNameX;
		this.columnNameY = columnNameY;
		this.scatterPlotInterval = scatterPlotInterval;
		dataMaxValue = Math.max( scatterPlotInterval.max( 0 ), scatterPlotInterval.max( 1 ) );
		this.lineOverlay = lineOverlay;
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		g.setColor( Color.WHITE );

		final AffineTransform3D globalToViewerTransform = new AffineTransform3D();
		getCurrentTransform3D( globalToViewerTransform );

		drawGridLines( g, globalToViewerTransform );
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

	private void drawGridLines( Graphics2D g, AffineTransform3D globalToViewerTransform )
	{
		double[] zero = new double[ 3 ];
		globalToViewerTransform.apply( new double[ 3 ], zero );

		g.setColor( Color.WHITE );

		if ( lineOverlay.equals( Y_NX ) )
		{
			double[] n = new double[ 3 ];

			for ( int i = 0; i < 5; i++ )
			{
				globalToViewerTransform.apply( new double[]{ this.dataMaxValue, i * this.dataMaxValue, 0 }, n );
				g.drawLine( ( int ) zero[ 0 ], ( int ) zero[ 1 ],
						( int ) n[ 0 ], ( int ) n[ 1 ] );
			}
		}
		else if ( lineOverlay.equals( Y_N ) )
		{

			double[] max = new double[ 3 ];
			globalToViewerTransform.apply( new double[]{ this.dataMaxValue, this.dataMaxValue, 0 }, max );

			for ( int i = 0; i < 5; i++ )
			{
				double[] n = new double[ 3 ];
				globalToViewerTransform.apply( new double[]{ 0, i, 0 }, n );

				g.drawLine(
						( int ) zero[ 0 ], ( int ) n[ 1 ],
						( int ) max[ 0 ], ( int ) n[ 1 ] );
			}
		}

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

