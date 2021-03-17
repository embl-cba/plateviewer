package de.embl.cba.plateviewer.screenshot;

import bdv.cache.CacheControl;
import bdv.util.BdvOverlay;
import bdv.util.Prefs;
import bdv.viewer.BasicViewerState;
import bdv.viewer.ViewerPanel;
import bdv.viewer.ViewerState;
import bdv.viewer.overlay.ScaleBarOverlayRenderer;
import bdv.viewer.render.MultiResolutionRenderer;
import bdv.viewer.render.PainterThread;
import bdv.viewer.render.RenderResult;
import bdv.viewer.render.RenderTarget;
import bdv.viewer.render.awt.BufferedImageRenderResult;
import ij.ImagePlus;
import net.imglib2.realtransform.AffineTransform3D;

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
		ViewerState renderState = new BasicViewerState( viewer.state().snapshot() );

		final AffineTransform3D affine = new AffineTransform3D();
		renderState.getViewerTransform( affine );
		affine.set( affine.get( 0, 3 ) - width / 2, 0, 3 );
		affine.set( affine.get( 1, 3 ) - height / 2, 1, 3 );
		affine.scale( ( double ) width / width );
		affine.set( affine.get( 0, 3 ) + width / 2, 0, 3 );
		affine.set( affine.get( 1, 3 ) + height / 2, 1, 3 );
		renderState.setViewerTransform( affine );

		final ScaleBarOverlayRenderer scalebar = Prefs.showScaleBarInMovie() ? new ScaleBarOverlayRenderer() : null;

		class MyTarget implements RenderTarget<BufferedImageRenderResult> {
			final BufferedImageRenderResult renderResult = new BufferedImageRenderResult();

			MyTarget() {
			}

			public BufferedImageRenderResult getReusableRenderResult() {
				return this.renderResult;
			}

			public BufferedImageRenderResult createRenderResult() {
				return new BufferedImageRenderResult();
			}

			public void setRenderResult(BufferedImageRenderResult renderResult) {
			}

			public int getWidth() {
				return width;
			}

			public int getHeight() {
				return height;
			}
		}

		final MyTarget target = new MyTarget();
		final MultiResolutionRenderer renderer = new MultiResolutionRenderer(
				target, new PainterThread( null ), new double[] { 1 }, 0, 1, null, false,
				viewer.getOptionValues().getAccumulateProjectorFactory(), new CacheControl.Dummy() );

		renderState.setCurrentTimepoint( viewer.getState().getCurrentTimepoint() );
		renderer.requestRepaint();
		renderer.paint( renderState );

		if ( Prefs.showScaleBarInMovie() )
		{
			BufferedImage bi = target.renderResult.getBufferedImage();
			final Graphics2D g2 = bi.createGraphics();
			g2.setClip( 0, 0, width, height );
			scalebar.setViewerState( renderState );
			scalebar.paint( g2 );
		}

		if ( overlays != null )
		{
			BufferedImage bi = target.renderResult.getBufferedImage();
			final Graphics2D g = bi.createGraphics();
			for ( BdvOverlay overlay : overlays )
			{
				overlay.drawOverlays( g );
			}
		}

		return new ImagePlus( "ScreenShot", target.renderResult.getBufferedImage() );
	}
}
