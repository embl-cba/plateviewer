package de.embl.cba.gridviewer;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import java.util.ArrayList;

import static de.embl.cba.gridviewer.ImageFilterUI.addSettingsViaUI;

public class ImageFilter < T extends NativeType< T > & RealType< T > >
{

	public static String SIMPLE_SEGMENTATION = "simple segmentation";
	public static String SUBTRACT_MEDIAN = "subtract median";
	public static String MAX_MINUS_MIN = "max minus min";

	private final ImageFilterSettings settings;
	private final String cachedFilterImgName;
	private BdvSource bdvSource;
	private ImageFilterLoader< UnsignedShortType > loader;

	private final CachedCellImg< UnsignedShortType, ? > cachedFilterImg;

	public ImageFilter( ImageFilterSettings settings )
	{
		this.settings = addSettingsViaUI( settings );

		this.cachedFilterImg = createCachedFilterImg();

		this.cachedFilterImgName = settings.inputName + " - " + settings.filterType;
	}

	public CachedCellImg< UnsignedShortType, ? > getCachedFilterImg()
	{
		return cachedFilterImg;
	}

	public String getCachedFilterImgName()
	{
		return cachedFilterImgName;
	}

	public static ArrayList< String > getFilters()
	{
		ArrayList< String > filterTypes = new ArrayList<>(  );
		filterTypes.add( SUBTRACT_MEDIAN );
		filterTypes.add( MAX_MINUS_MIN );
		return filterTypes;

	}


	public CachedCellImg< UnsignedShortType, ? > createCachedFilterImg( )
	{
		final CachedCellImg cachedCellImg = settings.imagesSource.getCachedCellImg();

		int[] cellDimensions = new int[ cachedCellImg.getCellGrid().numDimensions() ];
		cachedCellImg.getCellGrid().cellDimensions( cellDimensions );

		final long[] imgDimensions = cachedCellImg.getCellGrid().getImgDimensions();

		loader = new ImageFilterLoader( settings );

		return new ReadOnlyCachedCellImgFactory().create(
				imgDimensions,
				new UnsignedShortType(),
				loader,
				ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions ) );
	}


	public void dispose()
	{
		bdvSource.removeFromBdv();
		bdvSource = null;
		loader.dispose();
		loader = null;
	}
}