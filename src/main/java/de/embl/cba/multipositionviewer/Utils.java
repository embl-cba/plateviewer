package de.embl.cba.multipositionviewer;

import ij.IJ;

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
	public static final String PATTERN_MD_A01_CHANNEL = ".*_([A-Z]{1}[0-9]{2})_(.*).tif";
	public static final String PATTERN_ALMF_SCREENING_W0001_P000_C00 = ".*--W([0-9]{4})--P([0-9]{3}).*--C([0-9]{2}).ome.tif";
	public static final String PATTERN_NO_MATCH = "PATTERN_NO_MATCH";
	final static String CAPITAL_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

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

	public static Set< String > getChannelPatterns( List< File > files, String namingScheme )
	{
		final Set< String > channelPatterns = new HashSet<>( );

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( namingScheme ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				if ( namingScheme.equals( PATTERN_ALMF_SCREENING_W0001_P000_C00 ) )
				{
					channelPatterns.add( ".*" + matcher.group( 3 ) + ".ome.tif" );
				}
				else if ( namingScheme.equals( PATTERN_MD_A01_CHANNEL ) )
				{
					channelPatterns.add( ".*" + matcher.group( 2 ) + ".tif" );
				}
			}

		}

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


	public static String getMultiPositionNamingScheme( File file )
	{
		String filePath = file.getAbsolutePath();

		if ( Pattern.compile( PATTERN_MD_A01_CHANNEL ).matcher( filePath ).matches() ) return PATTERN_MD_A01_CHANNEL;
		if ( Pattern.compile( PATTERN_ALMF_SCREENING_W0001_P000_C00 ).matcher( filePath ).matches() ) return PATTERN_ALMF_SCREENING_W0001_P000_C00;

		return PATTERN_NO_MATCH;
	}
}
