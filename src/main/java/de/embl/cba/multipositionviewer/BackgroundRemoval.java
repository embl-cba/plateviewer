package de.embl.cba.multipositionviewer;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class BackgroundRemoval
{
	final ImagesSource imagesSource;
	final int radius;
	final double offset;
	final MultiPositionViewer multiPositionViewer;

	private BdvSource removedBackgroundBdvSource;
	private BackgroundRemovalLoader< UnsignedShortType > loader;

	public BackgroundRemoval( ImagesSource imagesSource, int radius, double offset, MultiPositionViewer multiPositionViewer )
	{
		this.imagesSource = imagesSource;
		this.radius = radius;
		this.offset = offset;
		this.multiPositionViewer = multiPositionViewer;

		final CachedCellImg< UnsignedShortType, ? > removedBackground = createCachedCellImg();

		addCachedCellImgToViewer( removedBackground );

	}

	public void addCachedCellImgToViewer( CachedCellImg< UnsignedShortType, ? > cachedCellImg )
	{

		removedBackgroundBdvSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( cachedCellImg, multiPositionViewer.getLoadingQueue() ),
				"background removed",
				BdvOptions.options().addTo( multiPositionViewer.getBdv() ) );

		removedBackgroundBdvSource.setColor( new ARGBType( ARGBType.rgba( 255, 255,255,255 )));
	}

	public CachedCellImg< UnsignedShortType, ? > createCachedCellImg( )
	{
		final CachedCellImg cachedCellImg = imagesSource.getCachedCellImg();

		int[] cellDimensions = new int[ cachedCellImg.getCellGrid().numDimensions() ];
		cachedCellImg.getCellGrid().cellDimensions( cellDimensions );

		final long[] imgDimensions = cachedCellImg.getCellGrid().getImgDimensions();

		loader = new BackgroundRemovalLoader<>(
				imagesSource,
				radius,
				offset, multiPositionViewer.getBdv() );

		return new ReadOnlyCachedCellImgFactory().create(
				imgDimensions,
				new UnsignedShortType(),
				loader,
				ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions ) );
	}


	public void dispose()
	{
		removedBackgroundBdvSource.removeFromBdv();
		removedBackgroundBdvSource = null;
		loader.dispose();
		loader = null;
	}
}
