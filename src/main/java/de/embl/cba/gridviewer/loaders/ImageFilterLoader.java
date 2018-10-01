package de.embl.cba.gridviewer.loaders;


import bdv.util.BdvOverlay;
import de.embl.cba.gridviewer.bdv.BdvVolatileTextOverlay;
import de.embl.cba.gridviewer.bdv.TextOverlay;
import de.embl.cba.gridviewer.Utils;
import de.embl.cba.gridviewer.imagefilter.FastFilters;
import de.embl.cba.gridviewer.imagefilter.ImageFilter;
import de.embl.cba.gridviewer.imagefilter.ImageFilterSettings;
import ij.process.FloatProcessor;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class ImageFilterLoader < T extends NativeType< T > & RealType< T > > implements CellLoader< T >
{

	private final ImageFilterSettings settings;
	private BdvOverlay bdvOverlay;

	public ImageFilterLoader( ImageFilterSettings settings )
	{
		this.settings = settings;

		if ( settings.filterType.equals( ImageFilter.SIMPLE_SEGMENTATION ) )
		{
			bdvOverlay = new BdvVolatileTextOverlay();
		}

	}

	@Override
	public void load( final SingleCellArrayImg< T, ? > cell ) throws Exception
	{
		if ( settings.multiPositionViewer.isImageExisting( cell ) )
		{
			applyFilterToSourceAndPutResultIntoCell( cell );
		}
	}

	public void applyFilterToSourceAndPutResultIntoCell( SingleCellArrayImg< T, ? > cell )
	{
		final IntervalView< T > inputInterval = Views.interval( settings.inputCachedCellImg, cell );

		if ( settings.filterType.equals( ImageFilter.MEDIAN_DEVIATION ) )
		{
			applyFastFiler( inputInterval, cell );
		}
		else if ( settings.filterType.equals( ImageFilter.SIMPLE_SEGMENTATION ) )
		{
			simpleSegmentation( inputInterval, ( SingleCellArrayImg) cell );
		}
		else if ( settings.filterType.equals( ImageFilter.INFORMATION ) )
		{
			information( inputInterval, ( SingleCellArrayImg) cell );
		}

	}


	public BdvOverlay getBdvOverlay()
	{
		return bdvOverlay;
	}

	private void simpleSegmentation( RandomAccessibleInterval< T > input, SingleCellArrayImg< UnsignedByteType, ?> cell )
	{
		thresholdImageSourceAndPutResultIntoCell( input, cell );

		final LabelRegions< Integer > labelRegions = Utils.createLabelRegions( cell );

		clearCell( ( SingleCellArrayImg ) cell );

		int numValidObjects = paintValidObjectsIntoCell( cell, labelRegions, settings.minObjectSize );

		((BdvVolatileTextOverlay)bdvOverlay).addTextOverlay(
				new TextOverlay(
						"" + numValidObjects,
						Utils.getCenter( cell ),
						200,
						Color.GREEN )
		);

	}

	public void thresholdImageSourceAndPutResultIntoCell( RandomAccessibleInterval< T > input, SingleCellArrayImg< UnsignedByteType, ? > cell )
	{
		final Cursor< T > inputImageCursor = Views.flatIterable( input ).cursor();

		final Cursor< UnsignedByteType > cellCursor = Views.flatIterable( cell ).cursor();

		while ( cellCursor.hasNext() )
		{
			cellCursor.next().setReal( inputImageCursor.next().getRealDouble() > settings.threshold ? 1 : 0 );
		}
	}

	public int paintValidObjectsIntoCell( SingleCellArrayImg< UnsignedByteType, ? > cell, LabelRegions< Integer > labelRegions, long minSize )
	{
		int numValidObjects = 0;
		for ( LabelRegion labelRegion : labelRegions )
		{
			if ( labelRegion.size() > minSize )
			{
				numValidObjects++;
				paintRegionIntoCell( cell, labelRegion );
			}
		}
		return numValidObjects;
	}

	public void clearCell( SingleCellArrayImg< T, ? > cell )
	{
		final Cursor< T > cellCursor2 = cell.cursor();
		while ( cellCursor2.hasNext() )
		{
			cellCursor2.next().setReal( 0 );
		}
	}

	public static void paintRegionIntoCell( SingleCellArrayImg< UnsignedByteType, ? > cell, LabelRegion labelRegion )
	{
		final Cursor< Void > regionCursor = labelRegion.cursor();
		final RandomAccess< UnsignedByteType > access = cell.randomAccess();
		while ( regionCursor.hasNext() )
		{
			regionCursor.fwd();
			access.setPosition( regionCursor );
			access.get().set( 255 );
		}
	}

	public void applyFastFiler( RandomAccessibleInterval< T > input, SingleCellArrayImg< T, ? > cell )
	{
		if ( input.numDimensions() == 2 )
		{
			applyFastFilter( input, cell );
		}
		else
		{
			// TODO
		}
	}

	public void applyFastFilter( RandomAccessibleInterval< T > input, SingleCellArrayImg< T, ? > cell )
	{
		final short[] cellData = ( short[] ) cell.getStorageArray();

		int w = (int) cell.dimension( 0 );
		int h = (int) cell.dimension( 1 );

		float[] floatInputPixels = new float[ h * w ];
		float[] floatInputSnapShot = new float[ h * w ];

		final Cursor< T > inputCursor = Views.flatIterable( input ).cursor();
		int i = 0;
		float value;
		while( inputCursor.hasNext() )
		{
			value = (float) inputCursor.next().getRealDouble();
			floatInputPixels[ i ] = value;
			floatInputSnapShot[ i++ ] = value;
		}

		final FloatProcessor floatInputIp = new FloatProcessor( w, h, floatInputPixels );
		final FloatProcessor floatInputSnapShotIp = new FloatProcessor( w, h, floatInputSnapShot );

		final FastFilters fastFilters = new FastFilters();

		if ( settings.filterType.equals( ImageFilter.MEDIAN_DEVIATION ) )
		{
			fastFilters.configureMedianDeviation( settings );
		}

		final long start = System.currentTimeMillis();
		fastFilters.run( floatInputIp, floatInputSnapShotIp );
		Utils.debug( "Fast filter time [ms]: " + ( System.currentTimeMillis() - start) );

		final short[] shortResultPixels = new short[ h * w ];
		for ( i = 0; i < h * w; ++i )
		{
			shortResultPixels[ i ] = ( short ) floatInputPixels[ i ];
		}

		System.arraycopy( shortResultPixels, 0, cellData, 0, cellData.length );

	}


	private void information( RandomAccessibleInterval< T > input, SingleCellArrayImg cell )
	{
		final RandomAccess< T > inputRandomAccess = input.randomAccess();
		final HyperSphereShape shape = new HyperSphereShape( settings.radius );
		final HyperSphereShape.NeighborhoodsAccessible< T > nra = shape.neighborhoodsRandomAccessible( Views.extendBorder( input ) );
		final RandomAccess< Neighborhood< T > > inputNRA = nra.randomAccess( input );

		final int size = ( int ) inputNRA.get().size();
		final double[] values = new double[ size ];

		final Cursor< T > cellCursor = Views.flatIterable( cell ).localizingCursor();

		final HashSet< Integer > set = new HashSet<>();

		while ( cellCursor.hasNext() )
		{
			cellCursor.fwd();

//			inputRandomAccess.setPosition( cellCursor );
//			final double inputValue = inputRandomAccess.get().getRealDouble();

//			cellCursor.get().setReal( inputValue );

			inputNRA.setPosition( cellCursor );


			set.clear();

			int index = 0;
			for ( final T pixel : inputNRA.get() )
			{
				set.add( (int) ( pixel.getRealDouble() / 100.0 ) );
//				values[ index++ ] = pixel.getRealDouble();
			}

//			Arrays.sort( values, 0, index );
//			double backgroundValue = values[ ( index - 1 ) / 2 ];

			cellCursor.get().setReal( set.size() );
		}

	}


	public void applyMedianSubtractionUsingImgLib2( RandomAccessibleInterval< T > input, SingleCellArrayImg< T, ? > cell )
	{
		final RandomAccess< T > inputRandomAccess = input.randomAccess();
		final RectangleShape shape = new RectangleShape( settings.radius, false );
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

