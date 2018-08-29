package de.embl.cba.multipositionviewer;

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

public class ImageFilter < T extends NativeType< T > & RealType< T > >
{

	public static String SIMPLE_SEGMENTATION = "simple segmentation";
	public static String SUBTRACT_MEDIAN = "subtract median";
	public static String MAX_MINUS_MIN = "max minus min";


	private final ImageFilterSettings settings;
	private final String outputName;
	private BdvSource bdvSource;
	private ImageFilterLoader< UnsignedShortType > loader;
	private final CachedCellImg< UnsignedShortType, ? > cachedFilterImg;

	public ImageFilter( ImageFilterSettings settings )
	{
		this.settings = settings;

		this.cachedFilterImg = createCachedFilterImg();

		this.outputName = addToViewer( cachedFilterImg );

		settings.imagesSource.getBdvSource().setActive( false );

	}


	public String getOutputName()
	{
		return outputName;
	}


	public static ArrayList< String > getFilterTypes()
	{
		ArrayList< String > filterTypes = new ArrayList<>(  );
		filterTypes.add( SUBTRACT_MEDIAN );
		filterTypes.add( MAX_MINUS_MIN );
		return filterTypes;

	}

	public String addToViewer( CachedCellImg< UnsignedShortType, ? > cachedCellImg )
	{

		String outputName = settings.inputName + " - " + settings.filterType;

		bdvSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( cachedCellImg, settings.multiPositionViewer.getLoadingQueue() ),
				outputName,
				BdvOptions.options().addTo( settings.multiPositionViewer.getBdv() ) );

		bdvSource.setColor( settings.imagesSource.getArgbType() );

		adjustLut();

		return outputName;

	}

	public void adjustLut()
	{
		final double[] lutMinMax = settings.imagesSource.getLutMinMax();

		if ( settings.filterType.equals( SUBTRACT_MEDIAN ) )
		{
			bdvSource.setDisplayRange( 0, lutMinMax[ 1 ] - lutMinMax[ 0 ] );
		}
		else if ( settings.filterType.equals( MAX_MINUS_MIN ) )
		{
			bdvSource.setDisplayRange( 0, lutMinMax[ 1 ] - lutMinMax[ 0 ] );
		}
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
