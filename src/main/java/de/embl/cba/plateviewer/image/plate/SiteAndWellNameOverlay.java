package de.embl.cba.plateviewer.image.plate;


import bdv.util.Bdv;
import bdv.util.BdvOverlay;
import de.embl.cba.plateviewer.image.SingleSiteChannelFile;
import de.embl.cba.plateviewer.image.cellloader.MultiSiteLoader;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.*;
import java.util.Arrays;

import static de.embl.cba.plateviewer.util.Utils.bdvTextOverlayFontSize;


public class SiteAndWellNameOverlay extends BdvOverlay implements MouseMotionListener
{
	final int numDimensions;
	final Bdv bdv;
	final MultiSiteLoader multiSiteLoader;
	private final AffineTransform3D affineTransform3D;
	private String wellName;
	private String siteName;

	public SiteAndWellNameOverlay( Bdv bdv, MultiSiteLoader multiSiteLoader, AffineTransform3D affineTransform3D )
	{
		super();
		this.bdv = bdv;
		this.multiSiteLoader = multiSiteLoader;
		this.affineTransform3D = affineTransform3D;
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

	private long[] getArrayCoordinates( RealPoint globalMouseCoordinates )
	{
		final double[] arrayCoordinates = new double[ 3 ];
		sourceTransform.inverse().apply( globalMouseCoordinates.positionAsDoubleArray(), arrayCoordinates );
		final long[] longs = Arrays.stream( arrayCoordinates ).mapToLong( x -> ( long ) x ).toArray();
		return longs;
	}

	@Override
	public void mouseMoved( MouseEvent e )
	{
		RealPoint globalMouseCoordinates = new RealPoint( 3 );
		bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates( globalMouseCoordinates );

		final long[] coordinates = getArrayCoordinates( globalMouseCoordinates );

		final SingleSiteChannelFile singleSiteChannelFile = multiSiteLoader.getSingleSiteFile( coordinates );
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

