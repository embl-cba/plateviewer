package de.embl.cba.plateviewer.source.table;

import net.imglib2.*;

public class TestImage implements RandomAccessibleInterval
{
	@Override
	public long min( int d )
	{
		return 0;
	}

	@Override
	public void min( long[] min )
	{

	}

	@Override
	public void min( Positionable min )
	{

	}

	@Override
	public long max( int d )
	{
		return 0;
	}

	@Override
	public void max( long[] max )
	{

	}

	@Override
	public void max( Positionable max )
	{

	}

	@Override
	public void dimensions( long[] dimensions )
	{

	}

	@Override
	public long dimension( int d )
	{
		return 0;
	}

	@Override
	public RandomAccess randomAccess()
	{
		return null;
	}

	@Override
	public RandomAccess randomAccess( Interval interval )
	{
		return null;
	}

	@Override
	public double realMin( int d )
	{
		return 0;
	}

	@Override
	public void realMin( double[] min )
	{

	}

	@Override
	public void realMin( RealPositionable min )
	{

	}

	@Override
	public double realMax( int d )
	{
		return 0;
	}

	@Override
	public void realMax( double[] max )
	{

	}

	@Override
	public void realMax( RealPositionable max )
	{

	}

	@Override
	public int numDimensions()
	{
		return 0;
	}
}
