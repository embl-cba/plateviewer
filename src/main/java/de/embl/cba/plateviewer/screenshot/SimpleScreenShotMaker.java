package de.embl.cba.plateviewer.screenshot;

import bdv.cache.CacheControl;
import bdv.util.BdvOverlay;
import bdv.util.Prefs;
import bdv.viewer.ViewerPanel;
import bdv.viewer.overlay.ScaleBarOverlayRenderer;
import bdv.viewer.render.MultiResolutionRenderer;
import bdv.viewer.state.ViewerState;
import ij.ImagePlus;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.RenderTarget;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;

public class SimpleScreenShotMaker
{
	public static ImagePlus getSimpleScreenShot( ViewerPanel viewer, Set< BdvOverlay > overlays )
	{
		return getSimpleScreenShot( viewer, viewer.getWidth(), viewer.getHeight(), overlays );
	}

	private static ImagePlus getSimpleScreenShot( ViewerPanel viewer, int width, int height, Set< BdvOverlay > overlays )
	{
		final ViewerState renderState = viewer.getState();

		final AffineTransform3D affine = new AffineTransform3D();
		renderState.getViewerTransform( affine );
		affine.set( affine.get( 0, 3 ) - width / 2, 0, 3 );
		affine.set( affine.get( 1, 3 ) - height / 2, 1, 3 );
		affine.scale( ( double ) width / width );
		affine.set( affine.get( 0, 3 ) + width / 2, 0, 3 );
		affine.set( affine.get( 1, 3 ) + height / 2, 1, 3 );
		renderState.setViewerTransform( affine );

		final ScaleBarOverlayRenderer scalebar = Prefs.showScaleBarInMovie() ? new ScaleBarOverlayRenderer() : null;

		class MyTarget implements RenderTarget
		{
			BufferedImage bi;

			@Override
			public BufferedImage setBufferedImage( final BufferedImage bufferedImage )
			{
				bi = bufferedImage;
				return null;
			}

			@Override
			public int getWidth()
			{
				return width;
			}

			@Override
			public int getHeight()
			{
				return height;
			}
		}

		final MyTarget target = new MyTarget();
		final MultiResolutionRenderer renderer = new MultiResolutionRenderer(
				target, new PainterThread( null ), new double[] { 1 }, 0, false, 1, null, false,
				viewer.getOptionValues().getAccumulateProjectorFactory(), new CacheControl.Dummy() );

		renderState.setCurrentTimepoint( viewer.getState().getCurrentTimepoint() );
		renderer.requestRepaint();
		renderer.paint( renderState );

		if ( Prefs.showScaleBarInMovie() )
		{
			final Graphics2D g2 = target.bi.createGraphics();
			g2.setClip( 0, 0, width, height );
			scalebar.setViewerState( renderState );
			scalebar.paint( g2 );
		}

		if ( overlays != null )
		{
			final Graphics2D g = target.bi.createGraphics();
			for ( BdvOverlay overlay : overlays )
			{
				overlay.drawOverlays( g );
			}
		}

		return new ImagePlus( "ScreenShot", target.bi );
	}
}
