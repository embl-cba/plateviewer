package de.embl.cba.multipositionviewer;


import bdv.util.BdvOverlay;
import net.imglib2.realtransform.AffineTransform2D;

import java.util.List;
import java.awt.*;


public class BdvPositionIndexOverlay extends BdvOverlay
{

	final List< ImagesSource > imagesSources;
	final List< ImageFile > imageFiles;
	final int numDimensions;

	public BdvPositionIndexOverlay( List< ImagesSource > imagesSources )
	{
		super();
		this.imagesSources = imagesSources;
		this.imageFiles = imagesSources.get( 0 ).getLoader().getImageFiles();
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

		for ( final ImageFile imageFile : imageFiles )
		{
			String name = imageFile.getPositionName();

			double[] center = new double[ numDimensions ];

			t.apply( imageFile.getCenter(), center );

			g.setColor( getColor( ) );

			final FontMetrics fontMetrics = setFont( g, fontSize );

			int[] stringPosition = getStringPosition( name, center, fontMetrics );

			g.drawString( imageFile.getFile().getName(), stringPosition[ 0 ], stringPosition[ 1 ] );
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

	private Color getColor( )
	{
//		int alpha = 255 - ( int ) Math.round( Math.abs( depth ) );

//		if ( alpha < 64 )
//			alpha = 64;

		return new Color( 255, 0, 255, 255 );
	}

	private int getSize( final double depth )
	{
		return ( int ) Math.max( 1, 10 - 0.1 * Math.round( Math.abs( depth ) ) );
	}

}

