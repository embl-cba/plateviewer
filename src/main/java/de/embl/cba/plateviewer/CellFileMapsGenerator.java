package de.embl.cba.plateviewer;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CellFileMapsGenerator
{

	final String directoryName;
	final String fileNameRegExp;

	int numSites, numWells;
	int[] siteDimensions;
	int[] wellDimensions;

	final ArrayList< Map< String, File > > cellFileMaps;

	public CellFileMapsGenerator( String directoryName, String fileNameRegExp )
	{
		this.directoryName = directoryName;
		this.fileNameRegExp = fileNameRegExp;
		this.cellFileMaps =  new ArrayList<>(  );
		createCellFileMaps();
	}

	public ArrayList< Map< String, File > > getCellFileMaps()
	{
		return cellFileMaps;
	}

	public void createCellFileMaps()
	{
		cellFileMaps.add( new HashMap<>() );

		final ArrayList< File > files = Utils.getFiles( directoryName, fileNameRegExp );

		configWells( files );
		configSites( files );

		for ( File file : files )
		{
			final String pattern = getPattern( file );

			final String cell = Utils.getCellString( getCell( file, pattern, wellDimensions[ 0 ], siteDimensions[ 0 ] ) );

			putCellToMaps( cellFileMaps, cell, file );

		}
	}

	private void configWells( ArrayList< File > files )
	{
		numWells = getNumWells( files );
		wellDimensions = new int[ 2 ];

		if ( numWells < 24 )
		{
			wellDimensions[ 0 ] = 6;
			wellDimensions[ 1 ] = 4;
		}
		else if ( numWells < 96  )
		{
			wellDimensions[ 0 ] = 12;
			wellDimensions[ 1 ] = 8;
		}

		Utils.log( "Distinct wells: " +  numWells );
		Utils.log( "Well dimensions [ 0 ] : " +  wellDimensions[ 0 ] );
		Utils.log( "Well dimensions [ 1 ] : " +  wellDimensions[ 1 ] );
	}

	private void configSites( ArrayList< File > files )
	{
		numSites = getNumSites( files );
		siteDimensions = new int[ 2 ];
		siteDimensions[ 0 ] = (int) Math.ceil( Math.sqrt( numSites ) );
		siteDimensions[ 1 ] = (int) Math.ceil( Math.sqrt( numSites ) );
		Utils.log( "Distinct sites: " +  numSites );
		Utils.log( "Site dimensions [ 0 ] : " +  siteDimensions[ 0 ] );
		Utils.log( "Site dimensions [ 1 ] : " +  siteDimensions[ 1 ] );
	}

	public int[] getSiteDimensions()
	{
		return siteDimensions;
	}

	public int[] getWellDimensions()
	{
		return wellDimensions;
	}

	public static int getNumSites( ArrayList< File > files )
	{
		Set< String > sites = new HashSet<>( );

		for ( File file : files )
		{
			final String pattern = getPattern( file );

			final Matcher matcher = Pattern.compile( pattern ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				if ( pattern.equals( Utils.PATTERN_W0001_P000 ) )
				{
					sites.add( matcher.group( 2 ) );
				}
			}
		}

		if ( sites.size() == 0 )
		{
			return 1;
		}
		else
		{
			return sites.size();
		}

	}

	public static int getNumWells( ArrayList< File > files )
	{
		Set< String > wells = new HashSet<>( );

		for ( File file : files )
		{
			final String pattern = getPattern( file );

			final Matcher matcher = Pattern.compile( pattern ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				wells.add( matcher.group( 1 ) );
			}
		}

		return wells.size();
	}

	public static void putCellToMaps( ArrayList< Map< String, File > > cellFileMaps,
									  String cell,
									  File file )
	{
		boolean cellCouldBePlaceInExistingMap = false;

		for( int iMap = 0; iMap < cellFileMaps.size(); ++iMap )
		{
			if ( !cellFileMaps.get( iMap ).containsKey( cell ) )
			{
				cellFileMaps.get( iMap ).put( cell, file );
				cellCouldBePlaceInExistingMap = true;
				break;
			}
		}

		if ( ! cellCouldBePlaceInExistingMap )
		{
			// new channel
			cellFileMaps.add( new HashMap<>() );
			cellFileMaps.get( cellFileMaps.size() - 1 ).put( cell, file );
		}
	}

	public static String getPattern( File file )
	{
		String filePath = file.getAbsolutePath();

		if ( Pattern.compile( Utils.PATTERN_A01 ).matcher( filePath ).matches() ) return Utils.PATTERN_A01;
		if ( Pattern.compile( Utils.PATTERN_W0001_P000 ).matcher( filePath ).matches() ) return Utils.PATTERN_W0001_P000;

		return Utils.PATTERN_NO_MATCH;
	}


	public static int[] getCell( File file, String pattern, int numWellColumns, int numSiteColumns )
	{
		String filePath = file.getAbsolutePath();

		final Matcher matcher = Pattern.compile( pattern ).matcher( filePath );

		if ( matcher.matches() )
		{
			final int[] xy = new int[ 2 ];

			if ( pattern.equals( Utils.PATTERN_A01 ) )
			{
				String well = matcher.group( 1 );
				xy[ 0 ] = Integer.parseInt( well.substring( 1, 3 ) ) - 1;
				xy[ 1 ] = Utils.CAPITAL_ALPHABET.indexOf( well.substring( 0, 1 ) );
			}
			else if ( pattern.equals( Utils.PATTERN_W0001_P000 ) )
			{

				int wellNum = Integer.parseInt( matcher.group( 1 ) );
				int siteNum = Integer.parseInt( matcher.group( 2 ) );

				int wellY = wellNum / numWellColumns * numSiteColumns;
				int wellX = wellNum % numWellColumns * numSiteColumns;
				int siteY = siteNum / numSiteColumns;
				int siteX = siteNum % numSiteColumns;

				xy[ 0 ] = wellX + siteX;
				xy[ 1 ] = wellY + siteY;

			}

			return xy;

		}
		else
		{
			return null;
		}

	}


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
}
