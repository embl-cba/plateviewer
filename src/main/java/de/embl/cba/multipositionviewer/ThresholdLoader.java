package de.embl.cba.multipositionviewer;


import bdv.util.*;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

import java.util.ArrayList;

public class ThresholdLoader< T extends NativeType< T > & RealType< T > > implements CellLoader< UnsignedByteType >
{

	final RandomAccessibleInterval< T > inputImage;
	final ArrayList< ImageFile > imageFiles;
	final double realThreshold;
	final Bdv bdv;
	final BdvVolatileTextOverlay bdvVolatileTextOverlay;
	final BdvOverlaySource< BdvOverlay > objectNumberOverlay;

	public ThresholdLoader(
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

		inputImage.
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

		// Paint number on image
		int numObjects = 1;
		bdvVolatileTextOverlay.addTextOverlay( "" + numObjects, Utils.getCenter( cell ) );

	}
}

