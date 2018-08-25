package de.embl.cba.multipositionviewer;


import bdv.util.*;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.ops.parse.token.Int;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Iterator;

public class SimpleSegmentationLoader< T extends NativeType< T > & RealType< T > > implements CellLoader< UnsignedByteType >
{

	final RandomAccessibleInterval< T > inputImage;
	final ArrayList< ImageFile > imageFiles;
	final double realThreshold;
	final Bdv bdv;
	final BdvVolatileTextOverlay bdvVolatileTextOverlay;
	final BdvOverlaySource< BdvOverlay > objectNumberOverlay;

	public SimpleSegmentationLoader(
			ImagesSource imagesSource,
			final double realThreshold,
			final Bdv bdv )
	{
		this.inputImage = imagesSource.getCachedCellImg();
		this.imageFiles = imagesSource.getLoader().getImageFiles();
		this.realThreshold = realThreshold;
		this.bdv = bdv;
		this.bdvVolatileTextOverlay = new BdvVolatileTextOverlay();
		this.objectNumberOverlay = BdvFunctions.showOverlay( bdvVolatileTextOverlay, "overlay", BdvOptions.options().addTo( bdv ) );
	}

	public void removeObjectNumberOverlay()
	{
		objectNumberOverlay.removeFromBdv();
	}

	@Override
	public void load( final SingleCellArrayImg< UnsignedByteType, ? > cell ) throws Exception
	{


		final Cursor< T > inputImageCursor = Views.flatIterable( Views.interval( inputImage, cell ) ).cursor();
		final Cursor< UnsignedByteType > cellCursor = Views.flatIterable( cell ).cursor();

		final UnsignedByteType yes = new UnsignedByteType( 255 );
		final UnsignedByteType no = new UnsignedByteType( 0 );

		T threshold = inputImage.randomAccess().get().copy();
		threshold.setReal( realThreshold );

		while ( cellCursor.hasNext() )
		{
			cellCursor.next().set( inputImageCursor.next().compareTo( threshold ) > 0 ?  yes : no  );
		}

		// connected components
		Img< IntType > connectedComponents = ArrayImgs.ints( Intervals.dimensionsAsLongArray( cell ) );

		ConnectedComponents.labelAllConnectedComponents( cell, connectedComponents, ConnectedComponents.StructuringElement.FOUR_CONNECTED );

		final LabelRegions< Integer > labelRegions = new LabelRegions( connectedComponents );

		for ( LabelRegion labelRegion : labelRegions )
		{
			System.out.println( labelRegion.size() );
		}

		int numConnectedComponents = labelRegions.getExistingLabels().size();


//		// count components = maximum
//		int max = 0;
//		for( IntType pixel : connectedComponents )
//			max = Math.max( max, pixel.getInteger() );
//		// output maximum
//		System.out.println(max);
//

		// Paint number on image
		bdvVolatileTextOverlay.addTextOverlay( "" + numConnectedComponents, Utils.getCenter( cell ) );

	}
}

