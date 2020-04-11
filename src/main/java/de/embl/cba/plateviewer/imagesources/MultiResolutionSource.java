package de.embl.cba.plateviewer.imagesources;

import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.HashMap;

public class MultiResolutionSource< R > implements Source< R >
{
	private final HashMap< Double, RandomAccessibleInterval > scaleToRai;

	public MultiResolutionSource( HashMap< Double, RandomAccessibleInterval > scaleToRai )
	{
		this.scaleToRai = scaleToRai;
	}

	@Override
	public boolean isPresent( int t )
	{
		return false;
	}

	@Override
	public RandomAccessibleInterval< R > getSource( int t, int level )
	{
		return null;
	}

	@Override
	public RealRandomAccessible< R > getInterpolatedSource( int t, int level, Interpolation method )
	{
		return null;
	}

	@Override
	public void getSourceTransform( int t, int level, AffineTransform3D transform )
	{

	}

	@Override
	public R getType()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public VoxelDimensions getVoxelDimensions()
	{
		return null;
	}

	@Override
	public int getNumMipmapLevels()
	{
		return 0;
	}
}
