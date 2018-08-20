package de.embl.cba.multipositionviewer;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiPositionImagesSource
{

	final String directoryName;


	int numSites, numWells;
	int[] siteDimensions;
	int[] wellDimensions;
	int[] maxWellDimensionsInData;
	int[] maxSiteDimensionsInData;

	final ArrayList< Map< String, File > > fileMap;
	final ArrayList< File > files;
	final String fileNamePattern;

	public MultiPositionImagesSource( ArrayList< File > files, String filenamePattern  )
	{
		this.files = files;
		this.fileNamePattern = filenamePattern;

		this.directoryName = null;
		this.fileMap =  new ArrayList<>();
		this.maxWellDimensionsInData = new int[ 2 ];
		this.maxSiteDimensionsInData = new int[ 2 ];
		createCellFileMaps();
	}

	public ArrayList< Map< String, File > > getFileMap()
	{
		return fileMap;
	}

	public void createCellFileMaps()
	{
		fileMap.add( new HashMap<>() );

		final ArrayList< File > files = Utils.getFileList( directoryName, fileNameRegExp );

		configWells( files );

		configSites( files );

		for ( File file : files )
		{
			final String pattern = getPattern( file );

			final String cell = Utils.getCellString( getCell( file, pattern, wellDimensions[ 0 ], siteDimensions[ 0 ] ) );

			putCellToMaps( fileMap, cell, file );

		}
	}

	private void configWells( ArrayList< File > files )
	{
		numWells = getNumWells( files );
		wellDimensions = new int[ 2 ];

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
				if ( pattern.equals( Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00 ) )
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
		int maxWellNum = 0;

		for ( File file : files )
		{
			final String pattern = getPattern( file );

			final Matcher matcher = Pattern.compile( pattern ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				wells.add( matcher.group( 1 ) );

				if ( pattern.equals( Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00 ) )
				{
					int wellNum = Integer.parseInt( matcher.group( 1 ) );

					if ( wellNum > maxWellNum )
					{
						maxWellNum = wellNum;
					}
				}
			}
		}


		if ( maxWellNum > wells.size() )
		{
			return maxWellNum;
		}
		else
		{
			return wells.size();
		}

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
		if ( Pattern.compile( Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00 ).matcher( filePath ).matches() ) return Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00;

		return Utils.PATTERN_NO_MATCH;
	}


	public int[] getCell( File file, String pattern, int numWellColumns, int numSiteColumns )
	{
		String filePath = file.getAbsolutePath();

		final Matcher matcher = Pattern.compile( pattern ).matcher( filePath );

		if ( matcher.matches() )
		{
			int[] wellPosition = new int[ 2 ];
			int[] sitePosition = new int[ 2 ];

			if ( pattern.equals( Utils.PATTERN_A01 ) )
			{
				String well = matcher.group( 1 );

				wellPosition[ 0 ] = Integer.parseInt( well.substring( 1, 3 ) ) - 1;
				wellPosition[ 1 ] = Utils.CAPITAL_ALPHABET.indexOf( well.substring( 0, 1 ) );

			}
			else if ( pattern.equals( Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00 ) )
			{

				int wellNum = Integer.parseInt( matcher.group( 1 ) );
				int siteNum = Integer.parseInt( matcher.group( 2 ) );

				wellPosition[ 1 ] = wellNum / numWellColumns * numSiteColumns;
				wellPosition[ 0 ] = wellNum % numWellColumns * numSiteColumns;

				sitePosition[ 1 ] = siteNum / numSiteColumns;
				sitePosition[ 0 ] = siteNum % numSiteColumns;

			}

			updateMaxWellDimensionInData( wellPosition );
			updateMaxSiteDimensionInData( sitePosition );

			final int[] cellPosition = computeCellPosition( wellPosition, sitePosition );

			return cellPosition;

		}
		else
		{
			return null;
		}

	}

	public int[] computeCellPosition( int[] wellPosition, int[] sitePosition )
	{
		final int[] cellPosition = new int[ 2 ];

		for ( int d = 0; d < 2; ++d )
		{
			cellPosition[ d ] = wellPosition[ d ] + sitePosition[ d ];
		}
		return cellPosition;
	}

	public void updateMaxWellDimensionInData( int[] wellPosition )
	{
		for ( int d = 0; d < 2; ++d )
		{
			if ( wellPosition[ d ] >= maxWellDimensionsInData[ d ] )
			{
				maxWellDimensionsInData[ d ] = wellPosition[ d ];
			}
		}
	}

	public void updateMaxSiteDimensionInData( int[] sitePosition )
	{
		for ( int d = 0; d < 2; ++d )
		{
			if ( sitePosition[ d ] >= maxSiteDimensionsInData[ d ] )
			{
				maxSiteDimensionsInData[ d ] = sitePosition[ d ];
			}
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
