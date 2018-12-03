package de.embl.cba.plateviewer.bdv;


import bdv.util.Bdv;
import bdv.util.BdvOverlay;
import de.embl.cba.plateviewer.imagesources.ImageSource;
import de.embl.cba.plateviewer.imagesources.ImagesSource;
import de.embl.cba.plateviewer.loaders.MultiPositionLoader;
import net.imglib2.RealPoint;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.awt.*;

import static de.embl.cba.plateviewer.Constants.bdvTextOverlayFontSize;


public class BdvSiteAndWellNamesOverlay extends BdvOverlay implements MouseMotionListener
{
	final int numDimensions;
	final Bdv bdv;
	final MultiPositionLoader multiPositionLoader;
	private final int bdvWindowHeight;
	private String wellName;
	private String siteName;

	public BdvSiteAndWellNamesOverlay( Bdv bdv, List< ImagesSource > imagesSources )
	{
		super();
		this.bdv = bdv;
		this.multiPositionLoader = imagesSources.get( 0 ).getLoader();
		this.numDimensions = 2;

		bdv.getBdvHandle().getViewerPanel().getDisplay().addMouseMotionListener( this );

		bdvWindowHeight = bdv.getBdvHandle().getViewerPanel().getDisplay().getHeight();
		wellName = "";
		siteName = "";
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		g.setColor( Color.WHITE );

		int fontSize = bdvTextOverlayFontSize;

		g.setFont( new Font("TimesRoman", Font.PLAIN, fontSize ) );

		g.drawString( wellName,
				bdvTextOverlayFontSize,
				bdvWindowHeight - ( ( fontSize + 5 ) * 3 ) );

		g.drawString( siteName,
				bdvTextOverlayFontSize,
				bdvWindowHeight - ( ( fontSize + 5 ) * 2 ) );
	}


	@Override
	public void mouseDragged( MouseEvent e )
	{
	}

	public static long[] getCoordinate2D( RealPoint globalMouseCoordinates )
	{
		long[] xyCoordinate = new long[ 2 ];

		for( int d = 0; d < xyCoordinate.length; ++d )
		{
			xyCoordinate[ d ] = ( long ) Math.ceil( globalMouseCoordinates.getDoublePosition( d ) );
		}

		return xyCoordinate;
	}

	@Override
	public void mouseMoved( MouseEvent e )
	{
		RealPoint globalMouseCoordinates = new RealPoint( 3 );
		bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates( globalMouseCoordinates );

		final long[] coordinate2D = getCoordinate2D( globalMouseCoordinates );

		final ImageSource imageSource = multiPositionLoader.getImageSource( coordinate2D );
		if ( imageSource != null )
		{
			wellName = imageSource.getWellName();
			siteName = imageSource.getSiteName();
		}
		else
		{
			wellName = "";
			siteName = "";
		}
	}
}

