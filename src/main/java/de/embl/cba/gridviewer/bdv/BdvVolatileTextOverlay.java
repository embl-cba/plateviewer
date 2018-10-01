package de.embl.cba.gridviewer.bdv;

import bdv.util.BdvOverlay;
import net.imglib2.realtransform.AffineTransform2D;

import java.awt.*;
import java.util.ArrayList;

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

	public void addTextOverlay( TextOverlay textOverlay )
	{
		textOverlays.add( textOverlay );
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		final AffineTransform2D t = new AffineTransform2D();

		getCurrentTransform2D( t );

		final double scale = t.get( 0, 0 );

		/* get a copy of the textOverlays field to avoid concurrent modification exception
		that can be caused by the addTextOverlay( ) method.*/
		final ArrayList< TextOverlay > currentTextOverlays = new ArrayList<>( textOverlays );

		for ( final TextOverlay textOverlay : currentTextOverlays )
		{
			String string = textOverlay.text;

			double[] center = new double[ numDimensions ];

			t.apply( textOverlay.position, center );

			g.setColor( textOverlay.color );

			final FontMetrics fontMetrics = setFont( g, ( int ) ( scale * textOverlay.size ) );

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

}
