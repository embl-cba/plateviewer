package de.embl.cba.gridviewer;

import ij.IJ;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{

	public static final String WELL_PLATE_96 = "96 well plate";
	public static final String PATTERN_MD_A01_SITE_WAVELENGTH = ".*_([A-Z]{1}[0-9]{2})_s(.*)_w([0-9]{1}).*.tif";
	public static final String PATTERN_MD_A01_CHANNEL = ".*_([A-Z]{1}[0-9]{2})_(.*).tif";
	public static final String PATTERN_ALMF_SCREENING_W0001_P000_C00 = ".*--W([0-9]{4})--P([0-9]{3}).*--C([0-9]{2}).ome.tif";
	public static final String PATTERN_NO_MATCH = "PATTERN_NO_MATCH";
	public static final String CAPITAL_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static void log( String msg )
	{
		IJ.log( msg );
	}

	public static void debug( String msg )
	{
		IJ.log( "[DEBUG] " + msg );
	}

	public static int[] getCellPos( String cellPosString )
	{
		int[] cellPos = new int[ 2 ];
		final String[] split = cellPosString.split( "_" );

		for ( int d = 0; d < 2; ++d )
		{
			cellPos[ d ] = Integer.parseInt( split[ d ] );
		}

		return cellPos;
	}


	public static ArrayList< File > getFileList( File directory, String fileNameRegExp )
	{
		final ArrayList< File > files = new ArrayList<>();
		populateFileList( directory, fileNameRegExp,files );
		return files;
	}

	public static void populateFileList( File directory, String fileNameRegExp, List< File > files) {

		// Get all the files from a directory.
		File[] fList = directory.listFiles();

		if( fList != null )
		{
			for ( File file : fList )
			{
				if ( file.isFile() )
				{
					final Matcher matcher = Pattern.compile( fileNameRegExp ).matcher( file.getName() );

					if ( matcher.matches() )
					{
						files.add( file );
					}

				}
				else if ( file.isDirectory() )
				{
					populateFileList( file, fileNameRegExp, files );
				}
			}
		}
	}

	public static < T extends IntegerType >
	ImgLabeling< Integer, IntType > createImgLabeling( RandomAccessibleInterval< T > rai )
	{
		RandomAccessibleInterval< IntType > labelImg = ArrayImgs.ints( Intervals.dimensionsAsLongArray( rai ) );
		labelImg = Utils.getWithAdjustedOrigin( rai, labelImg );
		ImgLabeling< Integer, IntType > imgLabeling = new ImgLabeling<>( labelImg );

		final java.util.Iterator< Integer > labelCreator = new java.util.Iterator< Integer >()
		{
			int id = 0;

			@Override
			public boolean hasNext()
			{
				return true;
			}

			@Override
			public synchronized Integer next()
			{
				return id++;
			}
		};

		ConnectedComponents.labelAllConnectedComponents( Views.extendBorder( rai ), imgLabeling, labelCreator, ConnectedComponents.StructuringElement.EIGHT_CONNECTED );

		return imgLabeling;
	}

	public static < S extends NumericType< S >, T extends NumericType< T > >
	RandomAccessibleInterval< T > getWithAdjustedOrigin( RandomAccessibleInterval< S > source, RandomAccessibleInterval< T > target )
	{
		long[] offset = new long[ source.numDimensions() ];
		source.min( offset );
		target = Views.translate( target, offset );
		return target;
	}

	public static double[] getCenter( Interval interval )
	{
		int n = interval.numDimensions();
		final double[] center = new double[ n ];

		for ( int d = 0; d < n; ++d )
		{
			center[ d ] = interval.min( d ) + interval.dimension( d ) / 2.0;
		}

		return center;
	}

	public static ArrayList< String > getChannelPatterns( List< File > files, String namingScheme )
	{
		final Set< String > channelPatternSet = new HashSet<>( );

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( namingScheme ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				if ( namingScheme.equals( PATTERN_ALMF_SCREENING_W0001_P000_C00 ) )
				{
					channelPatternSet.add( ".*" + matcher.group( 3 ) + ".ome.tif" );
				}
				else if ( namingScheme.equals( PATTERN_MD_A01_SITE_WAVELENGTH ) )
				{
					channelPatternSet.add( ".*_s.*_w" + matcher.group( 3 ) + ".*" );
				}
				else if ( namingScheme.equals( PATTERN_MD_A01_CHANNEL ) )
				{
					channelPatternSet.add( ".*" + matcher.group( 2 ) + ".tif" );
				}
			}

		}

		ArrayList< String > channelPatterns = new ArrayList<>( channelPatternSet );

		return channelPatterns;
	}

	public static ArrayList< File > filterFiles( ArrayList< File > files, String filterPattern )
	{

		final ArrayList< File > filteredFiles = new ArrayList<>( );

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( filterPattern ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				filteredFiles.add( file );
			}

		}

		return filteredFiles;
	}


	public static String getNamingScheme( File file )
	{
		String filePath = file.getAbsolutePath();

		if ( Pattern.compile( PATTERN_MD_A01_SITE_WAVELENGTH ).matcher( filePath ).matches() ) return PATTERN_MD_A01_SITE_WAVELENGTH;
		if ( Pattern.compile( PATTERN_MD_A01_CHANNEL ).matcher( filePath ).matches() ) return PATTERN_MD_A01_CHANNEL;
		if ( Pattern.compile( PATTERN_ALMF_SCREENING_W0001_P000_C00 ).matcher( filePath ).matches() ) return PATTERN_ALMF_SCREENING_W0001_P000_C00;

		return PATTERN_NO_MATCH;
	}

	public static int[] guessWellDimensions( int[] maximalPositionsInData )
	{
		int[] wellDimensions = new int[ 2 ];

		if ( ( maximalPositionsInData[ 0 ] <= 6 ) && ( maximalPositionsInData[ 1 ] <= 4 ) )
		{
			wellDimensions[ 0 ] = 6;
			wellDimensions[ 1 ] = 4;
		}
		else if ( ( maximalPositionsInData[ 0 ] <= 12 ) && ( maximalPositionsInData[ 1 ] <= 8 )  )
		{
			wellDimensions[ 0 ] = 12;
			wellDimensions[ 1 ] = 8;
		}
		else if ( ( maximalPositionsInData[ 0 ] <= 24 ) && ( maximalPositionsInData[ 1 ] <= 16 )  )
		{
			wellDimensions[ 0 ] = 24;
			wellDimensions[ 1 ] = 16;
		}
		else
		{
			log( "ERROR: Could not figure out the correct number of wells...." );
		}

		return wellDimensions;
	}

	public static int[] guessWellDimensions( int numWells )
	{
		int[] wellDimensions = new int[ 2 ];

		if ( numWells <= 24 )
		{
			wellDimensions[ 0 ] = 6;
			wellDimensions[ 1 ] = 4;
		}
		else if ( numWells <= 96  )
		{
			wellDimensions[ 0 ] = 12;
			wellDimensions[ 1 ] = 8;
		}
		else if ( numWells <= 384  )
		{
			wellDimensions[ 0 ] = 24;
			wellDimensions[ 1 ] = 16;
		}
		else
		{
			log( "ERROR: Could not figure out the correct number of wells...." );
		}

		return wellDimensions;
	}

	public static long[] computeMinCoordinates( int[] imageDimensions, int[] wellPosition, int[] sitePosition, int[] siteDimensions )
	{
		final long[] min = new long[ 2 ];

		for ( int d = 0; d < 2; ++d )
		{
			min[ d ] = ( wellPosition[ d ] * siteDimensions[ d ] ) + sitePosition[ d ];
			min[ d ] *= imageDimensions[ d ];
		}

		return min;
	}

	public static FinalInterval createInterval( int[] wellPosition, int[] sitePosition, int[] siteDimensions, int[] imageDimensions )
	{
		final long[] min = computeMinCoordinates( imageDimensions, wellPosition, sitePosition, siteDimensions );
		final long[] max = new long[ min.length ];

		for ( int d = 0; d < min.length; ++d )
		{
			max[ d ] = min[ d ] + imageDimensions[ d ] - 1;
		}

		return new FinalInterval( min, max );
	}

	public static boolean areIntersecting( Interval requestedInterval, Interval imageInterval )
	{
		FinalInterval intersect = Intervals.intersect( requestedInterval, imageInterval );

		for ( int d = 0; d < intersect.numDimensions(); ++d )
		{
			if ( intersect.dimension( d ) <= 0 )
			{
				return false;
			}
		}

		return true;
	}

	public static LabelRegions< Integer > createLabelRegions( SingleCellArrayImg< UnsignedByteType, ? > cell )
	{
		final ImgLabeling< Integer, IntType > imgLabeling = createImgLabeling( cell );

//		RandomAccessibleInterval< IntType > labelImg = ArrayImgs.ints( Intervals.dimensionsAsLongArray( cell ) );
//		Utils.getWithAdjustedOrigin( cell, labelImg );

//		ConnectedComponents.labelAllConnectedComponents( cell, labelImg, ConnectedComponents.StructuringElement.FOUR_CONNECTED );
//		labelImg = Utils.getWithAdjustedOrigin( cell, labelImg );

		final LabelRegions< Integer > labelRegions = new LabelRegions( imgLabeling );

		return labelRegions;
	}

	public static ArrayList< String > getWellNames( ArrayList< File > files, String namingScheme, int wellGroup )
	{
		Set< String > wellNameSet = new HashSet<>(  );

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( namingScheme ).matcher( file.getName() );

			matcher.matches();

			wellNameSet.add(  matcher.group( wellGroup ) );
		}

		final ArrayList< String > wellNames = new ArrayList<>( wellNameSet );

		return wellNames;

	}
}
