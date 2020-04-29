package de.embl.cba.plateviewer.bdv;

import bdv.viewer.animate.AbstractTransformAnimator;
import net.imglib2.realtransform.AffineTransform3D;

public class RelativeTranslationAnimator extends AbstractTransformAnimator
{
	private final AffineTransform3D transformStart;

	private final double[] targetTranslation;

	public RelativeTranslationAnimator( final AffineTransform3D transformStart, final double[] targetTranslation, final long duration )
	{
		super( duration );
		this.transformStart = transformStart;
		this.targetTranslation = targetTranslation.clone();
	}

	@Override
	public AffineTransform3D get( final double t )
	{
		final AffineTransform3D transform = new AffineTransform3D();
		transform.set( transformStart );

		final double sx = transform.get( 0, 3 );
		final double sy = transform.get( 1, 3 );
		final double sz = transform.get( 2, 3 );

		final double tx = targetTranslation[ 0 ];
		final double ty = targetTranslation[ 1 ];
		final double tz = targetTranslation[ 2 ];

		transform.set( sx + t * tx, 0, 3 );
		transform.set( sy + t * ty , 1, 3 );
		transform.set( sz + t * tz , 2, 3 );

		return transform;
	}
}

