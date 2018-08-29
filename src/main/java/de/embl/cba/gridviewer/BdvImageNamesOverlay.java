package de.embl.cba.gridviewer;


import bdv.util.BdvOverlay;
import net.imglib2.realtransform.AffineTransform2D;

import java.util.List;
import java.awt.*;


public class BdvImageNamesOverlay extends BdvOverlay
{

	final List< ImagesSource > imagesSources;
	final List< ImageSource > imageSources;
	final int numDimensions;

	public BdvImageNamesOverlay( List< ImagesSource > imagesSources )
	{
		super();
		this.imagesSources = imagesSources;
		this.imageSources = imagesSources.get( 0 ).getLoader().getImageSources();
		this.numDimensions = 2;
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		final AffineTransform2D t = new AffineTransform2D();

		getCurrentTransform2D( t );

		final double scale = t.get( 0, 0 );

		int fontSize = (int) (scale * 50);

//		if ( fontSize < 8 ) return;

		for ( final ImageSource imageSource : imageSources )
		{
			String string = imageSource.getPositionName();

			double[] center = new double[ numDimensions ];

			t.apply( Utils.getCenter( imageSource.getInterval() ), center );

			g.setColor( Color.MAGENTA );

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

}

