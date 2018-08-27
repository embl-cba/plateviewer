package de.embl.cba.multipositionviewer;


import bdv.util.*;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

import java.awt.*;
import java.util.Arrays;

public class BackgroundRemovalLoader< T extends NativeType< T > & RealType< T > > implements CellLoader< T >
{
	final ImagesSource imagesSource;
	final int radius;
	final Bdv bdv;

	public BackgroundRemovalLoader(
			final ImagesSource imagesSource,
			final int radius,
			final Bdv bdv )
	{
		this.imagesSource = imagesSource;
		this.radius = radius;
		this.bdv = bdv;
	}

	public void dispose()
	{
	}

	@Override
	public void load( final SingleCellArrayImg< T, ? > cell ) throws Exception
	{
		if ( imagesSource.getLoader().getImageFile( cell ) == null ) return;

		removeBackgroundAndPutIntoCell( cell );
	}

	public void removeBackgroundAndPutIntoCell( SingleCellArrayImg< T, ? > cell )
	{

		RandomAccessibleInterval< T > input = Views.interval( imagesSource.getCachedCellImg(), cell );
		final RandomAccess< T > inputRandomAccess = input.randomAccess();
		final RectangleShape shape = new RectangleShape( radius, false );
		final RectangleShape.NeighborhoodsAccessible< T > nra = shape.neighborhoodsRandomAccessible( Views.extendBorder( input ) );
		final RandomAccess< Neighborhood< T > > inputNRA = nra.randomAccess( input );

		final int size = ( int ) inputNRA.get().size();
		final double[] values = new double[ size ];

		final Cursor< T > cellCursor = Views.flatIterable( cell ).localizingCursor();

		while ( cellCursor.hasNext() )
		{
			cellCursor.fwd();
			inputNRA.setPosition( cellCursor );

			int index = 0;
			for ( final T pixel : inputNRA.get() )
			{
				values[ index++ ] = pixel.getRealDouble();
			}

			Arrays.sort( values, 0, index );
			double backgroundValue = values[ ( index - 1 ) / 2 ];

			inputRandomAccess.setPosition( cellCursor );
			final double inputValue = inputRandomAccess.get().getRealDouble();

			cellCursor.get().setReal( inputValue - backgroundValue );
		}

	}



}

