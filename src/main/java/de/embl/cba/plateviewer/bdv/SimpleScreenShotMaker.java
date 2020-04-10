package de.embl.cba.plateviewer.bdv;

import bdv.cache.CacheControl;
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

public class SimpleScreenShotMaker
{
	public static ImagePlus getSimpleScreenShot( ViewerPanel viewer )
	{
		return getSimpleScreenShot( viewer, viewer.getWidth(), viewer.getHeight() );
	}

	public static ImagePlus getSimpleScreenShot( ViewerPanel viewer, int width, int height )
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

		return new ImagePlus( "ScreenShot", target.bi );
	}
}
