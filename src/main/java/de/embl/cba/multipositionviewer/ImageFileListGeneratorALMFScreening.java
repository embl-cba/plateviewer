package de.embl.cba.multipositionviewer;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageFileListGeneratorALMFScreening
{

	final ArrayList< File > files;

	int numSites, numWells;
	int[] siteDimensions;
	int[] wellDimensions;
	int[] maxWellDimensionsInData;
	int[] maxSiteDimensionsInData;
	int[] imageDimensions;

	final ArrayList< ImageFile > list;

	final String multipositionFilenamePattern = Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00;

	public ImageFileListGeneratorALMFScreening( ArrayList< File > files, int[] imageDimensions )
	{
		this.files = files;
		this.list = new ArrayList<>();
		this.imageDimensions = imageDimensions;

		this.maxWellDimensionsInData = new int[ 2 ];
		this.maxSiteDimensionsInData = new int[ 2 ];

		createList();

	}

	public static String getKey( int[] cellPos )
	{
		return "" + cellPos[ 0] + "_" + cellPos[ 1 ];
	}

	public ArrayList< ImageFile > getList()
	{
		return list;
	}

	public void createList()
	{

		configWells( files );
		configSites( files );

		for ( File file : files )
		{

			final ImageFile imageFile = new ImageFile();

			imageFile.file = file;
			imageFile.centerCoordinates = getCenterCoordinates( file, multipositionFilenamePattern, wellDimensions[ 0 ], siteDimensions[ 0 ] );
			imageFile.dimensions = imageDimensions;

			list.add( imageFile );

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

	public int getNumSites( ArrayList< File > files )
	{
		Set< String > sites = new HashSet<>( );

		for ( File file : files )
		{
			final String pattern = Utils.getMultiPositionFilenamePattern( file );

			final Matcher matcher = Pattern.compile( pattern ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				if ( multipositionFilenamePattern.equals( Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00 ) )
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

	public int getNumWells( ArrayList< File > files )
	{
		Set< String > wells = new HashSet<>( );
		int maxWellNum = 0;

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( multipositionFilenamePattern ).matcher( file.getName() );

			wells.add( matcher.group( 1 ) );

			int wellNum = Integer.parseInt( matcher.group( 1 ) );

			if ( wellNum > maxWellNum )
			{
				maxWellNum = wellNum;
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


	public long[] getCenterCoordinates( File file, String pattern, int numWellColumns, int numSiteColumns )
	{
		String filePath = file.getAbsolutePath();

		final Matcher matcher = Pattern.compile( pattern ).matcher( filePath );

		if ( matcher.matches() )
		{
			int[] wellPosition = new int[ 2 ];
			int[] sitePosition = new int[ 2 ];

			int wellNum = Integer.parseInt( matcher.group( 1 ) );
			int siteNum = Integer.parseInt( matcher.group( 2 ) );

			wellPosition[ 1 ] = wellNum / numWellColumns * numSiteColumns;
			wellPosition[ 0 ] = wellNum % numWellColumns * numSiteColumns;

			sitePosition[ 1 ] = siteNum / numSiteColumns;
			sitePosition[ 0 ] = siteNum % numSiteColumns;

			updateMaxWellDimensionInData( wellPosition );
			updateMaxSiteDimensionInData( sitePosition );

			final long[] centerCoordinates = computeCenterCoordinates( wellPosition, sitePosition );

			return centerCoordinates;

		}
		else
		{
			return null;
		}

	}

	public long[] computeCenterCoordinates( int[] wellPosition, int[] sitePosition )
	{
		final long[] center = new long[ 2 ];

		for ( int d = 0; d < 2; ++d )
		{
			center[ d ] = wellPosition[ d ] + sitePosition[ d ];
			center[ d ] *= imageDimensions[ d ];
		}

		return center;
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
