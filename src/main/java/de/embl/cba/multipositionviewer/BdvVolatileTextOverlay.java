package de.embl.cba.multipositionviewer;

import bdv.util.BdvOverlay;
import net.imglib2.realtransform.AffineTransform2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BdvVolatileTextOverlay extends BdvOverlay
{
	private final ArrayList< TextOverlay > textOverlays;
	private final int numDimensions;

	public BdvVolatileTextOverlay( )
	{
		super();
		this.textOverlays = new ArrayList<>( );
		this.numDimensions = 2;
	}

	public void addTextOverlay( String string, double[] position )
	{
		textOverlays.add( new TextOverlay( string, position ) );
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		final AffineTransform2D t = new AffineTransform2D();

		getCurrentTransform2D( t );

		final double scale = t.get( 0, 0 );

		int fontSize = (int) (scale * 200);

		for ( final TextOverlay textOverlay : textOverlays )
		{
			String string = textOverlay.text;

			double[] center = new double[ numDimensions ];

			t.apply( textOverlay.position, center );

			g.setColor(  Color.GREEN );

			final FontMetrics fontMetrics = setFont( g, fontSize );

			int[] stringPosition = getStringPosition( string, center, fontMetrics );

			g.drawString( string, stringPosition[ 0 ], stringPosition[ 1 ] );
		}
	}

	private int[] getStringPosition( String name, double[] center, FontMetrics fontMetrics )
	{
		int[] stringSize = getStringSize( name, fontMetrics );

		int[] stringPosition = new int[ numDimensions ];
		for ( int d = 0; d < numDimensions; ++d )
		{
			stringPosition[ d ] = ( int ) ( center[ d ] - 0.5 * stringSize[ d ] );
		}
		return stringPosition;
	}

	private int[] getStringSize( String name, FontMetrics fontMetrics )
	{
		int[] graphicsSize = new int[ numDimensions ];
		graphicsSize[ 0 ] = fontMetrics.stringWidth( name );
		graphicsSize[ 1 ] = fontMetrics.getHeight();
		return graphicsSize;
	}

	private FontMetrics setFont( Graphics2D g, int fontSize )
	{
		g.setFont( new Font("TimesRoman", Font.PLAIN, fontSize ) );
		return g.getFontMetrics( g.getFont() );
	}

	private class TextOverlay
	{
		String text;
		double[] position;

		public TextOverlay( String text, double[] position )
		{
			this.text = text;
			this.position = position;
		}
	}
}
