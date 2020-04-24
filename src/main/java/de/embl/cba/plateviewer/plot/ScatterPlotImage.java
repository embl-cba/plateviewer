package de.embl.cba.plateviewer.plot;

import bdv.util.*;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.image.channel.BdvViewable;
import de.embl.cba.tables.color.ColorUtils;
import net.imglib2.*;
import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.BiConsumer;

public class ScatterPlotImage implements BdvViewable
{
	public static final String IMAGE_NAME = "plate outlines";
	private Interval imageInterval;
	private RandomAccessibleInterval< UnsignedShortType > rai;
	private double[] contrastLimits = new double[ 2 ];
	private RandomAccessibleIntervalSource< UnsignedShortType > source;
	private FunctionRealRandomAccessible< UnsignedShortType > realRandomAccessible;

	public ScatterPlotImage(  )
	{
		createImage();
	}

	private void createImage( )
	{
		final ArrayList< RealPoint > points = new ArrayList<>();
		final ArrayList< Integer > indices = new ArrayList<>();

		final Random random = new Random( System.currentTimeMillis() );
		final int range = 100;
		final double pointSize = 0.5;

		final int numPoints = 1000;

		for ( int i = 0; i < numPoints; i++ )
		{
			final double x = random.nextDouble() * range;
			final double y = random.nextDouble() * range;
			points.add( new RealPoint( x, y ) );
			indices.add( i );
		}

		imageInterval = FinalInterval.createMinMax( 0, 0, range, range );

		final KDTree< Integer > kdTree = new KDTree<>( indices, points );
		final NearestNeighborSearchOnKDTree< Integer > search = new NearestNeighborSearchOnKDTree<>( kdTree );

		double minDistanceSquared = pointSize * pointSize;
		BiConsumer< RealLocalizable, UnsignedShortType > biConsumer = ( position, t ) ->
		{
			synchronized ( this )
			{
				search.search( position );
				final Sampler< Integer > sampler = search.getSampler();
				final Integer integer = sampler.get();
				if ( search.getSquareDistance() < minDistanceSquared )
				{
					t.set( integer + 1 );
				}
				else
				{
					t.set( 0 );
				}
			}
		};

		realRandomAccessible = new FunctionRealRandomAccessible< UnsignedShortType >( 2, biConsumer, UnsignedShortType::new );

		final BdvStackSource< UnsignedShortType > plot = BdvFunctions.show( realRandomAccessible, imageInterval, "scatter plot", BdvOptions.options().is2D() );
		contrastLimits[ 0 ] = 0;
		contrastLimits[ 1 ] = numPoints;

		plot.setDisplayRange( contrastLimits[ 0 ], contrastLimits[ 1 ] );

	}

	@Override
	public String getName()
	{
		return IMAGE_NAME;
	}

	@Override
	public ARGBType getColor()
	{
		return ColorUtils.getARGBType( Color.WHITE );
	}

	@Override
	public double[] getContrastLimits()
	{
		return contrastLimits;
	}

	@Override
	public RandomAccessibleInterval< ? > getRAI()
	{
		return rai;
	}

	@Override
	public Source< ? > getSource()
	{
		return source;
	}

	@Override
	public BdvOverlay getOverlay()
	{
		return null;
	}

	@Override
	public boolean isInitiallyVisible()
	{
		return true;
	}

	@Override
	public Metadata.Type getType()
	{
		return Metadata.Type.Image;
	}


}
