package de.embl.cba.plateviewer.bdv;


import bdv.util.Bdv;
import bdv.util.BdvOverlay;
import de.embl.cba.plateviewer.image.SingleSiteChannelFile;
import de.embl.cba.plateviewer.image.cellloader.MultiSiteLoader;
import net.imglib2.RealPoint;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.*;

import static de.embl.cba.plateviewer.Utils.bdvTextOverlayFontSize;


public class BdvSiteAndWellInformationOverlay extends BdvOverlay implements MouseMotionListener
{
	final int numDimensions;
	final Bdv bdv;
	final MultiSiteLoader multiSiteLoader;
	private String wellName;
	private String siteName;

	public BdvSiteAndWellInformationOverlay(
			Bdv bdv,
			MultiSiteLoader multiSiteLoader )
	{
		super();
		this.bdv = bdv;
		this.multiSiteLoader = multiSiteLoader;
		this.numDimensions = 2;

		bdv.getBdvHandle().getViewerPanel().getDisplay().addMouseMotionListener( this );

		wellName = "";
		siteName = "";
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		g.setColor( Color.WHITE );

		int fontSize = bdvTextOverlayFontSize;

		g.setFont( new Font("TimesRoman", Font.PLAIN, fontSize ) );

		int bdvWindowHeight = bdv.getBdvHandle().getViewerPanel().getDisplay().getHeight();
		int bdvWindowWidth = bdv.getBdvHandle().getViewerPanel().getDisplay().getWidth();

		int distanceToWindowBottom = 2 * ( fontSize + 5 );

		g.drawString( wellName,
				bdvWindowWidth / 5,
				bdvWindowHeight - distanceToWindowBottom  );

		distanceToWindowBottom = 1 * ( fontSize + 5 );

		g.drawString( siteName,
				bdvWindowWidth / 5,
				bdvWindowHeight - distanceToWindowBottom );
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

		final SingleSiteChannelFile singleSiteChannelFile = multiSiteLoader.getChannelSource( coordinate2D );
		if ( singleSiteChannelFile != null )
		{
			wellName = singleSiteChannelFile.getWellName() + " " + singleSiteChannelFile.getWellInformation();
			siteName = singleSiteChannelFile.getSiteName() + " " + singleSiteChannelFile.getSiteInformation();
		}
		else
		{
			wellName = "";
			siteName = "";
		}
	}
}

