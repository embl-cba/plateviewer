package de.embl.cba.multipositionviewer;

import bdv.util.*;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class SimpleSegmentation < T extends NativeType< T > & RealType< T > >
{
	final ImagesSource imagesSource;
	final CachedCellImg< T, ? > input;
	final double threshold;
	final long minObjectSize;
	final MultiPositionViewer multiPositionViewer;
	final String inputName;
	final String outputName;

	private BdvSource segmentationBdvSource;
	private SimpleSegmentationLoader< UnsignedByteType > loader;

	public SimpleSegmentation( ImagesSource imagesSource,
							   CachedCellImg< T, ? > input,
							   String inputName,
							   double threshold,
							   long minObjectSize,
							   MultiPositionViewer multiPositionViewer )
	{
		this.imagesSource = imagesSource;
		this.input = input;
		this.inputName = inputName;
		this.threshold = threshold;
		this.minObjectSize = minObjectSize;
		this.multiPositionViewer = multiPositionViewer;

		final CachedCellImg< UnsignedByteType, ? > cachedCellImg = createCachedCellImg();

		outputName = addToViewer( cachedCellImg );

	}

	public String getOutputName()
	{
		return outputName;
	}


	public String addToViewer( CachedCellImg< UnsignedByteType, ? > cachedCellImg )
	{
		String name = inputName + " - simple segmentation";
		segmentationBdvSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( cachedCellImg,
						multiPositionViewer.getLoadingQueue() ),
				name,
				BdvOptions.options().addTo( multiPositionViewer.getBdv() ) );

		segmentationBdvSource.setColor( new ARGBType( ARGBType.rgba( 0, 255,0,255 )));

		return name;
	}

	public CachedCellImg< UnsignedByteType, ? > createCachedCellImg( )
	{
		final CachedCellImg cachedCellImg = input;

		int[] cellDimensions = new int[ cachedCellImg.getCellGrid().numDimensions() ];
		cachedCellImg.getCellGrid().cellDimensions( cellDimensions );

		final long[] imgDimensions = cachedCellImg.getCellGrid().getImgDimensions();

		loader = new SimpleSegmentationLoader(
				imagesSource,
				input,
				threshold,
				minObjectSize,
				multiPositionViewer.getBdv() );

		return new ReadOnlyCachedCellImgFactory().create(
				imgDimensions,
				new UnsignedByteType(),
				loader,
				ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions ) );
	}


	public void dispose()
	{
		segmentationBdvSource.removeFromBdv();
		segmentationBdvSource = null;
		loader.dispose();
		loader = null;
	}
}
