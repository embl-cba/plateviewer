package de.embl.cba.multipositionviewer;


import bdv.util.*;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.Arrays;

public class ImageFilterLoader < T extends NativeType< T > & RealType< T > > implements CellLoader< T >
{
	final ImagesSource imagesSource;
	final RandomAccessibleInterval< T > input;
	final String filterType;
	final int radius;
	final double offset;
	final Bdv bdv;

	public ImageFilterLoader(
			final ImagesSource imagesSource,
			CachedCellImg< T, ? > input, String filterType, final int radius,
			double offset,
			final Bdv bdv )
	{
		this.imagesSource = imagesSource;
		this.input = input;
		this.filterType = filterType;
		this.radius = radius;
		this.offset = offset;
		this.bdv = bdv;
	}

	public void dispose()
	{
	}

	@Override
	public void load( final SingleCellArrayImg< T, ? > cell ) throws Exception
	{
		if ( imagesSource.getLoader().getImageFile( cell ) == null ) return;

		applyFilterAndPutIntoCell( cell );
	}

	public void applyFilterAndPutIntoCell( SingleCellArrayImg< T, ? > cell )
	{

		if ( filterType.equals( ImageFilter.SUBTRACT_MEDIAN ) )
		{
			subtractMedian(  Views.interval( input, cell ), cell );
		}
		else if ( filterType.equals( ImageFilter.MAX_MINUS_MIN ) )
		{
			// TODO
		}
	}

	public void subtractMedian( RandomAccessibleInterval< T > input, SingleCellArrayImg< T, ? > cell )
	{
		if ( input.numDimensions() == 2 )
		{
			applyMedianSubtractionUsingFastFilters( input, cell );
		}
		else
		{
			applyMedianSubtractionUsingImgLib2( input, cell );
		}
	}

	public void applyMedianSubtractionUsingFastFilters( RandomAccessibleInterval< T > input, SingleCellArrayImg< T, ? > cell )
	{
		final short[] cellData = ( short[] ) cell.getStorageArray();
		final ImagePlus inputImp = ImageJFunctions.wrap( input, "" );

		// TODO:
		// one could improve this by giving the cellData to the fastFilters
		// such that can directly write the result into it, thereby avoiding one tmp array.
		// the issue is that the FastFilters work with float arrays, but the cellData is
		// a short array.
		final FastFilters fastFilters = new FastFilters();
		fastFilters.configureMedianSubtraction( radius, offset, inputImp.getType() );
		fastFilters.run( inputImp.getProcessor() );
		final FloatProcessor result = fastFilters.getResult();
		final ShortProcessor shortProcessor = result.convertToShortProcessor();
		final short[] resultData = (short[]) shortProcessor.getPixels();
		System.arraycopy( resultData, 0, cellData, 0, cellData.length );
	}

	public void applyMedianSubtractionUsingImgLib2( RandomAccessibleInterval< T > input, SingleCellArrayImg< T, ? > cell )
	{
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

