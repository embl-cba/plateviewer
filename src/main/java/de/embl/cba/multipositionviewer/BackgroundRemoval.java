package de.embl.cba.multipositionviewer;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class BackgroundRemoval
{
	final ImagesSource imagesSource;
	final int radius;
	final MultiPositionViewer multiPositionViewer;

	private BdvSource removedBackgroundBdvSource;
	private BackgroundRemovalLoader< ShortType > loader;

	public BackgroundRemoval( ImagesSource imagesSource, int radius, MultiPositionViewer multiPositionViewer )
	{
		this.imagesSource = imagesSource;
		this.radius = radius;
		this.multiPositionViewer = multiPositionViewer;

		final CachedCellImg< ShortType, ? > removedBackground = createCachedCellImg();

		addCachedCellImgToViewer( removedBackground );

	}

	public void addCachedCellImgToViewer( CachedCellImg< ShortType, ? > cachedCellImg )
	{

		removedBackgroundBdvSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( cachedCellImg, multiPositionViewer.getLoadingQueue() ),
				"background removed",
				BdvOptions.options().addTo( multiPositionViewer.getBdv() ) );

		removedBackgroundBdvSource.setColor( new ARGBType( ARGBType.rgba( 0, 255,0,255 )));
	}

	public CachedCellImg< ShortType, ? > createCachedCellImg( )
	{
		final CachedCellImg cachedCellImg = imagesSource.getCachedCellImg();

		int[] cellDimensions = new int[ cachedCellImg.getCellGrid().numDimensions() ];
		cachedCellImg.getCellGrid().cellDimensions( cellDimensions );

		final long[] imgDimensions = cachedCellImg.getCellGrid().getImgDimensions();

		loader = new BackgroundRemovalLoader<>(
				imagesSource,
				radius,
				multiPositionViewer.getBdv() );

		return new ReadOnlyCachedCellImgFactory().create(
				imgDimensions,
				new ShortType(),
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
