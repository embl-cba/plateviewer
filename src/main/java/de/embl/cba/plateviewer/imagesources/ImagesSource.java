package de.embl.cba.plateviewer.imagesources;

import bdv.util.BdvOverlaySource;
import bdv.util.BdvSource;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.loaders.MultiPositionLoader;
import de.embl.cba.plateviewer.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.process.LUT;
import net.imglib2.FinalInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;

import java.awt.*;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.*;
import java.util.List;

public class ImagesSource < T extends RealType< T > & NativeType< T > >
{
	public static final String LUT_MIN_MAX = "ContrastLimits";
	public static final String COLOR = "Color";
	public static final String SKIP = "Skip";
	public static final String VISIBLE = "Visible";

	private long[] dimensions;
	private int[] imageDimensions;
	private double[] lutMinMax = new double[]{0, 255};
	private ARGBType argbType;

	private ArrayList< ImageSource > imageSources;

	private ArrayList< String > wellNames;
	private CachedCellImg< T, ? > cachedCellImg;
	private MultiPositionLoader loader;
	private String hdf5DataSetName;
	private String name;

	private BdvSource bdvSource;
	private BdvOverlaySource bdvOverlaySource;
	private NativeType nativeType;
	private Metadata.Type type;
	private boolean isInitiallyVisible;

	public ImagesSource( List< File > files, String name, int numIoThreads )
	{
		this( files, null, name, numIoThreads );
	}

	public ImagesSource( List< File > files, String hdf5DataSetName, String name, int numIoThreads )
	{
		this.hdf5DataSetName = hdf5DataSetName;

		setImageProperties( files.get( 0 ) );

		setImagesSourceAndWellNames( files, hdf5DataSetName, name );

		setMultiPositionLoader( numIoThreads );

		setCachedCellImgDimensions();

		createCachedCellImg();
	}

	public ImagesSource(
			CachedCellImg< T , ? > cachedCellImg,
			String name,
			BdvSource bdvSource,
			BdvOverlaySource bdvOverlaySource )
	{
		this.cachedCellImg = cachedCellImg;
		this.name = name;
		this.hdf5DataSetName = null;
		this.bdvSource = bdvSource;
		this.bdvOverlaySource = bdvOverlaySource;
	}

	public void dispose()
	{
		if( bdvSource != null ) bdvSource.removeFromBdv();
		if ( bdvOverlaySource != null ) bdvOverlaySource.removeFromBdv();
		cachedCellImg = null;
	}

	public void setImagesSourceAndWellNames( List< File > files, String hdf5DataSetName, String namingScheme )
	{
		ImageSourcesGenerator imageSourcesGenerator =
				getImageSourcesGenerator( files, hdf5DataSetName, namingScheme );

		imageSources = imageSourcesGenerator.getImageSources();

		wellNames = imageSourcesGenerator.getWellNames();
	}

	public ImageSourcesGenerator getImageSourcesGenerator(
			List< File > files,
			String hdf5DataSetName,
			String namingScheme )
	{
		ImageSourcesGenerator imageSourcesGenerator = null;

		if ( namingScheme.equals( NamingSchemes.PATTERN_MD_A01_SITE_WAVELENGTH ) )
		{
			imageSourcesGenerator = new ImageSourcesGeneratorMDMultiSite(
					files, imageDimensions, NamingSchemes.PATTERN_MD_A01_SITE_WAVELENGTH  );
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_MD_A01_SITE ) )
		{
			imageSourcesGenerator = new ImageSourcesGeneratorMDMultiSite(
					files, imageDimensions, NamingSchemes.PATTERN_MD_A01_SITE );
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_SCANR_WELL_SITE_CHANNEL ) )
		{
			imageSourcesGenerator = new ImageSourcesGeneratorScanR( files, imageDimensions );
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_ALMF_SCREENING_WELL_SITE_CHANNEL ) )
		{
			imageSourcesGenerator = new ImageSourcesGeneratorALMFScreening( files, imageDimensions );
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_MD_A01_WAVELENGTH ) )
		{
			imageSourcesGenerator = new ImageSourcesGeneratorMDSingleSite( files, imageDimensions );
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_CORONA_HDF5 ) )
		{
			imageSourcesGenerator = new ImageSourcesGeneratorCoronaHdf5( files, hdf5DataSetName ,imageDimensions );
		}
		return imageSourcesGenerator;
	}

	public ArrayList< String > getWellNames()
	{
		return wellNames;
	}

	public ARGBType getColor()
	{
		return argbType;
	}

	public BdvSource getBdvSource()
	{
		return bdvSource;
	}

	public void setBdvSource( BdvSource bdvSource )
	{
		this.bdvSource = bdvSource;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public void setCachedCellImgDimensions()
	{
		final ArrayList< ImageSource > imageSources = loader.getImageSources();

		FinalInterval union = new FinalInterval( imageSources.get( 0 ).getInterval() );

		for ( ImageSource imageSource : imageSources )
			union = Intervals.union( imageSource.getInterval(), union );

		dimensions = new long[ 2 ];

		for ( int d = 0; d < 2; ++d )
			dimensions[ d ] = union.max( d ) + 1;

	}

	private void setMultiPositionLoader( int numIoThreads )
	{
		loader = new MultiPositionLoader( imageSources, numIoThreads );
	}

	public double[] getLutMinMax()
	{
		return lutMinMax;
	}

	public MultiPositionLoader getLoader()
	{
		return loader;
	}

	private void setImageProperties( File file )
	{
		if ( file.getName().endsWith( ".h5" ) )
		{
			setImagePropertiesUsingJHdf5( file );
		}
		else
		{
			setImagePropertiesUsingIJOpenImage( file );
		}
	}

	private void setImagePropertiesUsingIJOpenImage( File file )
	{
		final ImagePlus imagePlus = IJ.openImage( file.getAbsolutePath() );

		setLut( imagePlus );

		setImageDataType( imagePlus );

		setImageDimensions( imagePlus );
	}

	private void setImagePropertiesUsingJHdf5( File file )
	{
		final IHDF5Reader hdf5Reader = HDF5Factory.openForReading( file );

		setLut( hdf5Reader );

		setImageDataType( hdf5Reader );

		setImageDimensions( hdf5Reader );
	}

	private void setLut( IHDF5Reader hdf5Reader )
	{
		String colorName = hdf5Reader.string().getAttr( hdf5DataSetName, COLOR );

		if ( colorName.equals( "Gray" ) ) // TODO: Remove!
			colorName = "White";

		if ( colorName.equals( "Glasbey" ) )
		{
			colorName = "Gray";
			this.type = Metadata.Type.Segmentation;
		}
		else
		{
			this.type = Metadata.Type.Image;
		}

		final Color color = Utils.getColor( colorName );
		argbType = Utils.getARGBType( color );

		isInitiallyVisible = hdf5Reader.bool().getAttr( hdf5DataSetName, VISIBLE );

		setLutMinMax( hdf5Reader );
	}

	private void setLutMinMax( IHDF5Reader hdf5Reader )
	{
		if ( ! hdf5Reader.hasAttribute( hdf5DataSetName, LUT_MIN_MAX ) ) return;

		try
		{
			final short[] lutMinMax = hdf5Reader.int16().getArrayAttr( hdf5DataSetName, LUT_MIN_MAX );
			this.lutMinMax[ 0 ] = lutMinMax[ 0 ];
			this.lutMinMax[ 1 ] = lutMinMax[ 1 ];
		}
		catch ( Exception e )
		{
			this.lutMinMax[ 0 ] = 0;
			this.lutMinMax[ 1 ] = 1;
		}
	}

	private void setImageDimensions( IHDF5Reader hdf5Reader )
	{
		final long[] dimensions = hdf5Reader.getDataSetInformation( hdf5DataSetName ).getDimensions();
		imageDimensions = new int[ 2 ];
		imageDimensions[ 0 ] = (int) dimensions[ 1 ]; // hdf5 is the other way around
		imageDimensions[ 1 ] = (int) dimensions[ 0 ];
	}

	private void setLut( ImagePlus imagePlus )
	{
		setLutColor( imagePlus );

		setLutMinMax( imagePlus );

		isInitiallyVisible = true;
	}

	private void setLutColor( ImagePlus imagePlus )
	{
		final String title = imagePlus.getTitle().toLowerCase();

		if ( title.contains( "gfp" ) )
			argbType = new ARGBType( ARGBType.rgba( 0, 255, 0, 255 ) );
		else if ( title.contains( "mcherry" ) )
			argbType = new ARGBType( ARGBType.rgba( 255, 0, 0, 255 ) );
		else if ( title.contains( "dapi" ) )
			argbType = new ARGBType( ARGBType.rgba( 0, 0, 255, 255 ) );
		else if ( title.contains( "hoechst" ) )
			argbType = new ARGBType( ARGBType.rgba( 0, 0, 255, 255 ) );
		else if ( title.contains( "yfp" ) )
			argbType = new ARGBType( ARGBType.rgba( 255, 255, 0, 255 ) );
		else if ( title.contains( "cfp" ) )
			argbType = new ARGBType( ARGBType.rgba( 0, 255, 255, 255 ) );
		else if ( title.contains( "rfp" ) )
			argbType = new ARGBType( ARGBType.rgba( 255, 0, 0, 255 ) );
		else if ( title.contains( "a488" ) )
			argbType = new ARGBType( ARGBType.rgba( 0, 255, 0, 255 ) );
		else if ( title.contains( "alexa488" ) )
			argbType = new ARGBType( ARGBType.rgba( 0, 255, 0, 255 ) );
		else if ( title.contains( "c00.ome.tif" ) )
			argbType = new ARGBType( ARGBType.rgba( 0, 0, 255, 255 ) );
		else if ( title.contains( "c01.ome.tif" ) )
			argbType = new ARGBType( ARGBType.rgba( 0, 255, 0, 255 ) );
		else if ( title.contains( "c02.ome.tif" ) )
			argbType = new ARGBType( ARGBType.rgba( 255, 0, 255, 255 ) );
		else
		{
			final LUT[] luts = imagePlus.getLuts();

			final LUT lut = luts[ 0 ];
			final IndexColorModel colorModel = lut.getColorModel();
			final int mapSize = colorModel.getMapSize();
			final int red = colorModel.getRed( mapSize - 1 );
			final int green = colorModel.getRed( mapSize - 1 );
			final int blue = colorModel.getRed( mapSize - 1 );

			final int rgba = ARGBType.rgba( red, green, blue, 255 );

			argbType = new ARGBType( rgba );
		}
	}

	private void setLutMinMax( ImagePlus imagePlus )
	{
		lutMinMax = new double[ 2 ];
		lutMinMax[ 0 ] = imagePlus.getProcessor().getMin();
		lutMinMax[ 1 ] = imagePlus.getProcessor().getMax();
	}

	private void setImageDataType( IHDF5Reader hdf5Reader )
	{
		final HDF5DataSetInformation information = hdf5Reader.getDataSetInformation( hdf5DataSetName );
		final String dataType = information.getTypeInformation().toString();
		final boolean signed = information.isSigned();

		if( dataType.equals( Utils.H5_BYTE ) && ! signed )
			nativeType = new UnsignedByteType();
		else if( dataType.equals( Utils.H5_SHORT ) && ! signed )
			nativeType = new UnsignedShortType();
		else if( dataType.equals( Utils.H5_INT ) && ! signed )
			nativeType = new UnsignedIntType();
		else if( dataType.equals( Utils.H5_FLOAT ) )
			nativeType = new FloatType();
		else
			throw new UnsupportedOperationException( "Hdf5 datatype not supported: " + dataType );
	}

	private void setImageDataType( ImagePlus imagePlus )
	{
		int bitDepth = imagePlus.getBitDepth();

		switch ( bitDepth )
		{
			case 8:
				nativeType = new UnsignedByteType();
				break;
			case 16:
				nativeType = new UnsignedShortType();
				break;
			case 24: // RGB: currently returns sum of all three RGB values
				nativeType = new UnsignedShortType();
				break;
			case 32:
				nativeType = new FloatType();
				break;
			default:
				nativeType = null;
		}
	}

	private void setImageDimensions( ImagePlus imagePlus )
	{
		imageDimensions = new int[ 2 ];
		imageDimensions[ 0 ] = imagePlus.getWidth();
		imageDimensions[ 1 ] = imagePlus.getHeight();
	}

	public CachedCellImg getCachedCellImg( )
	{
		return cachedCellImg;
	}

	private void createCachedCellImg()
	{
		cachedCellImg = new ReadOnlyCachedCellImgFactory().create(
				dimensions,
				nativeType,
				loader,
				ReadOnlyCachedCellImgOptions.options().cellDimensions( imageDimensions ) );
	}

	public boolean isInitiallyVisible()
	{
		return isInitiallyVisible;
	}

	//	public static int getNumSites( List< File > files )
//	{
//		Set< String > sites = new HashSet<>( );
//
//		for ( File file : files )
//		{
//			final String pattern = getPattern( file );
//
//			final Matcher matcher = Pattern.compile( pattern ).matcher( file.getName() );
//
//			if ( matcher.matches() )
//			{
//				if ( pattern.equals( Utils.PATTERN_ALMF_SCREENING_TREAT1_TREAT2_WELLNUM ) )
//				{
//					sites.add( matcher.group( 2 ) );
//				}
//			}
//		}
//
//		if ( sites.size() == 0 )
//		{
//			return 1;
//		}
//		else
//		{
//			return sites.size();
//		}
//
//	}
//
//	public static int getNumWells( List< File > files )
//	{
//		Set< String > wells = new HashSet<>( );
//		int maxWellNum = 0;
//
//		for ( File file : files )
//		{
//			final String pattern = getPattern( file );
//
//			final Matcher matcher = Pattern.compile( pattern ).matcher( file.getName() );
//
//			if ( matcher.matches() )
//			{
//				wells.add( matcher.group( 1 ) );
//
//				if ( pattern.equals( Utils.PATTERN_ALMF_SCREENING_TREAT1_TREAT2_WELLNUM ) )
//				{
//					int wellNum = Integer.parseInt( matcher.group( 1 ) );
//
//					if ( wellNum > maxWellNum )
//					{
//						maxWellNum = wellNum;
//					}
//				}
//			}
//		}
//
//
//		if ( maxWellNum > wells.size() )
//		{
//			return maxWellNum;
//		}
//		else
//		{
//			return wells.size();
//		}
//
//	}

//	public static void putCellToMaps( ArrayList< Map< String, File > > cellFileMaps,
//									  String cell,
//									  File file )
//	{
//		boolean cellCouldBePlaceInExistingMap = false;
//
//		for( int iMap = 0; iMap < cellFileMaps.size(); ++iMap )
//		{
//			if ( !cellFileMaps.get( iMap ).containsKey( cell ) )
//			{
//				cellFileMaps.get( iMap ).put( cell, file );
//				cellCouldBePlaceInExistingMap = true;
//				break;
//			}
//		}
//
//		if ( ! cellCouldBePlaceInExistingMap )
//		{
//			// new channel
//			cellFileMaps.add( new HashMap<>() );
//			cellFileMaps.get( cellFileMaps.size() - 1 ).put( cell, file );
//		}
//	}
//
//	public static String getPattern( File file )
//	{
//		String filePath = file.getAbsolutePath();
//
//		if ( Pattern.compile( Utils.PATTERN_MD_A01_WAVELENGTH ).matcher( filePath ).matches() ) return Utils.PATTERN_MD_A01_WAVELENGTH;
//		if ( Pattern.compile( Utils.PATTERN_ALMF_SCREENING_TREAT1_TREAT2_WELLNUM ).matcher( filePath ).matches() ) return Utils.PATTERN_ALMF_SCREENING_TREAT1_TREAT2_WELLNUM;
//
//		return Utils.PATTERN_NO_MATCH;
//	}
//
//
//	public int[] getCell( File file, String pattern, int numWellColumns, int numSiteColumns )
//	{
//		String filePath = file.getAbsolutePath();
//
//		final Matcher matcher = Pattern.compile( pattern ).matcher( filePath );
//
//		if ( matcher.matches() )
//		{
//			int[] wellPosition = new int[ 2 ];
//			int[] sitePosition = new int[ 2 ];
//
//			if ( pattern.equals( Utils.PATTERN_MD_A01_WAVELENGTH ) )
//			{
//				String well = matcher.group( 1 );
//
//				wellPosition[ 0 ] = Integer.parseInt( well.substring( 1, 3 ) ) - 1;
//				wellPosition[ 1 ] = Utils.CAPITAL_ALPHABET.indexOf( well.substring( 0, 1 ) );
//
//			}
//			else if ( pattern.equals( Utils.PATTERN_ALMF_SCREENING_TREAT1_TREAT2_WELLNUM ) )
//			{
//
//				int wellNum = Integer.parseInt( matcher.group( 1 ) );
//				int siteNum = Integer.parseInt( matcher.group( 2 ) );
//
//				wellPosition[ 1 ] = wellNum / numWellColumns * numSiteColumns;
//				wellPosition[ 0 ] = wellNum % numWellColumns * numSiteColumns;
//
//				sitePosition[ 1 ] = siteNum / numSiteColumns;
//				sitePosition[ 0 ] = siteNum % numSiteColumns;
//
//			}
//
////			updateMaxWellDimensionInData( wellPosition );
////			updateMaxSiteDimensionInData( sitePosition );
//
//			final int[] cellPosition = computeCellPosition( wellPosition, sitePosition );
//
//			return cellPosition;
//
//		}
//		else
//		{
//			return null;
//		}
//
//	}

	public int[] computeCellPosition( int[] wellPosition, int[] sitePosition )
	{
		final int[] cellPosition = new int[ 2 ];

		for ( int d = 0; d < 2; ++d )
		{
			cellPosition[ d ] = wellPosition[ d ] + sitePosition[ d ];
		}
		return cellPosition;
	}

//	public void updateMaxWellDimensionInData( int[] wellPosition )
//	{
//		for ( int d = 0; d < 2; ++d )
//		{
//			if ( wellPosition[ d ] >= maxWellDimensionsInData[ d ] )
//			{
//				maxWellDimensionsInData[ d ] = wellPosition[ d ];
//			}
//		}
//	}
//
//	public void updateMaxSiteDimensionInData( int[] sitePosition )
//	{
//		for ( int d = 0; d < 2; ++d )
//		{
//			if ( sitePosition[ d ] >= maxSiteDimensionsInData[ d ] )
//			{
//				maxSiteDimensionsInData[ d ] = sitePosition[ d ];
//			}
//		}
//	}


	private static long[] getDimensions( String plateType )
	{
		long[] dimensions = new long[ 2 ];

		switch ( plateType )
		{
			case Utils.WELL_PLATE_96:
				dimensions[ 0 ] = 12;
				dimensions[ 1 ] = 8;
				break;
			default:
				dimensions[ 0 ] = 12;
				dimensions[ 1 ] = 8;
		}

		return dimensions;
	}

	public Metadata.Type getType()
	{
		return this.type;
	}
}
