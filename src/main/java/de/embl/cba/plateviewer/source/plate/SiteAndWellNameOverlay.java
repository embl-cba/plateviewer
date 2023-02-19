package de.embl.cba.plateviewer.source.plate;


import bdv.util.Bdv;
import bdv.util.BdvOverlay;
import bdv.viewer.InteractiveDisplayCanvas;
import de.embl.cba.plateviewer.source.SingleSiteChannelFile;
import de.embl.cba.plateviewer.source.cellloader.MultiSiteLoader;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.*;
import java.util.Arrays;

import static de.embl.cba.plateviewer.util.Utils.bdvTextOverlayFontSize;


public class SiteAndWellNameOverlay extends BdvOverlay implements MouseMotionListener
{
	private final int numDimensions;
	private final Bdv bdv;
	final MultiSiteLoader multiSiteLoader;
	private String well;
	private String site;
	private final RealPoint globalMouseCoordinates;
	private final AffineTransform3D sourceToViewerTransform;
	private final double[] sourceCoordinates;
	private final int distanceToWindowBottom;
	private final Font font;
	private final InteractiveDisplayCanvas display;
	private String siteInformation;

	public SiteAndWellNameOverlay( Bdv bdv, MultiSiteLoader multiSiteLoader )
	{
		super();
		this.bdv = bdv;
		this.multiSiteLoader = multiSiteLoader;
		this.numDimensions = 2;

		display = bdv.getBdvHandle().getViewerPanel().getDisplay();
		display.addMouseMotionListener( this );

		well = "";
		site = "";
		siteInformation = "";
		globalMouseCoordinates = new RealPoint( 3 );
		sourceToViewerTransform = new AffineTransform3D();
		sourceCoordinates = new double[ 3 ];

		font = new Font( "Monospaced", Font.PLAIN, bdvTextOverlayFontSize );
		distanceToWindowBottom = 1 * ( bdvTextOverlayFontSize + 5 );
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		g.setColor( Color.WHITE );
		g.setFont( font );

		final int bdvWindowHeight = display.getHeight();
		final int bdvWindowWidth = display.getWidth();


		if ( ! site.isEmpty() )
		{
			final String site = "site: " + this.site;
			g.drawString( site,
					bdvWindowWidth / 3,
					bdvWindowHeight - distanceToWindowBottom - 1 * bdvTextOverlayFontSize );
		}

		if ( ! well.isEmpty() )
		{
			final String well = "well: " + this.well;
			g.drawString( well,
					bdvWindowWidth / 3,
					bdvWindowHeight - distanceToWindowBottom - 0 * bdvTextOverlayFontSize );
		}
	}

	@Override
	public void mouseDragged( MouseEvent e )
	{
	}

	@Override
	public void mouseMoved( MouseEvent e )
	{
		bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates( globalMouseCoordinates );

		sourceTransform.inverse().apply( globalMouseCoordinates.positionAsDoubleArray(), sourceCoordinates );
		final long[] longs = Arrays.stream( sourceCoordinates ).mapToLong( x -> ( long ) x ).toArray();

		final SingleSiteChannelFile singleSiteChannelFile = multiSiteLoader.getSingleSiteFile( longs );

		if ( singleSiteChannelFile == null )
		{
			well = "";
			site = "";
			return;
		}

		well = singleSiteChannelFile.getWellName() + " " + singleSiteChannelFile.getWellInformation();
		site = singleSiteChannelFile.getSiteName() + " " + singleSiteChannelFile.getSiteInformation();
	}
}

