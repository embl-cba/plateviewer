package de.embl.cba.multipositionviewer;


import bdv.util.*;
import ij.ImagePlus;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.img.display.imagej.ImageJFunctions;
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

		if ( input.numDimensions() == 2 )
		{
			int b = 0;
			final short[] cellData = ( short[] ) cell.getStorageArray();
			final ImagePlus wrap = ImageJFunctions.wrap( input, "" );
			final short[] inputData = ( short[] ) wrap.getProcessor().getPixels() ;
			System.arraycopy( inputData, 0, cellData, 0, cellData.length );
			int a = 1;
		}
		else
		{
			// TODO: below code is super slow...
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

				inputRandomAccess.setPosition( cellCursor );
				final double inputValue = inputRandomAccess.get().getRealDouble();

				cellCursor.get().setReal( inputValue );

				inputNRA.setPosition( cellCursor );

				int index = 0;
				for ( final T pixel : inputNRA.get() )
				{
					values[ index++ ] = pixel.getRealDouble();
				}

				Arrays.sort( values, 0, index );
				double backgroundValue = values[ ( index - 1 ) / 2 ];

				cellCursor.get().setReal( inputValue - backgroundValue );
			}
		}
	}



}

