package de.embl.cba.multipositionviewer;

import net.imglib2.FinalInterval;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageFileListGeneratorMDSingleSite
{
	final ArrayList< File > files;

	int numSites, numWells;
	int[] siteDimensions;
	int[] wellDimensions;
	int[] maxWellDimensionsInData;
	int[] maxSiteDimensionsInData;
	int[] imageDimensions;

	final ArrayList< ImageFile > list;

	final static String namingScheme = Utils.PATTERN_MD_A01_CHANNEL;

	public ImageFileListGeneratorMDSingleSite( ArrayList< File > files, int[] imageDimensions )
	{
		this.files = files;
		this.list = new ArrayList<>();
		this.imageDimensions = imageDimensions;

		this.maxWellDimensionsInData = new int[ 2 ];
		this.maxSiteDimensionsInData = new int[ 2 ];

		createImageFileList();

	}

	public ArrayList< ImageFile > getFileList()
	{
		return list;
	}

	private void createImageFileList()
	{

		configWells( files );
		configSites( files );

		for ( File file : files )
		{

			final ImageFile imageFile = new ImageFile(
					file,
					getInterval( file ),
					file.getName());

			list.add( imageFile );
		}
	}

	private void configWells( ArrayList< File > files )
	{
		int[] maximalWellPositionsInData = getMaximalWellPositionsInData( files );

		wellDimensions = Utils.guessWellDimensions( maximalWellPositionsInData );

		Utils.log( "Distinct wells: " +  numWells );
		Utils.log( "Well dimensions [ 0 ] : " +  wellDimensions[ 0 ] );
		Utils.log( "Well dimensions [ 1 ] : " +  wellDimensions[ 1 ] );
	}

	private void configSites( ArrayList< File > files )
	{
		numSites = 1; //getNumSites( files );
		siteDimensions = new int[ 2 ];
		Arrays.fill( siteDimensions, 1 );

		Utils.log( "Distinct sites: " +  numSites );
		Utils.log( "Site dimensions [ 0 ] : " +  siteDimensions[ 0 ] );
		Utils.log( "Site dimensions [ 1 ] : " +  siteDimensions[ 1 ] );
	}

	private int getNumSites( ArrayList< File > files )
	{
		Set< String > sites = new HashSet<>( );

		for ( File file : files )
		{
			final String pattern = Utils.getMultiPositionNamingScheme( file );

			final Matcher matcher = Pattern.compile( pattern ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				if ( namingScheme.equals( Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00 ) )
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

	private int[] getMaximalWellPositionsInData( ArrayList< File > files )
	{
		int[] maximalWellPosition = new int[ 2 ];

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( namingScheme ).matcher( file.getName() );

			matcher.matches();

			int[] wellPosition = getWellPositionFromA01( matcher.group( 1 ) );

			for ( int d = 0; d < wellPosition.length; ++d )
			{
				if ( wellPosition[ d ] > maximalWellPosition[ d ] )
				{
					maximalWellPosition[ d ] = wellPosition[ d ];
				}
			}
		}

		return maximalWellPosition;

	}


	private FinalInterval getInterval( File file )
	{
		String filePath = file.getAbsolutePath();

		final Matcher matcher = Pattern.compile( namingScheme ).matcher( filePath );

		if ( matcher.matches() )
		{

			int[] sitePosition = new int[ ]{ 1, 1};
			int[] wellPosition = getWellPositionFromA01( matcher.group( 1 ) );

			final FinalInterval interval = Utils.createInterval( wellPosition, sitePosition, imageDimensions );

			return interval;

		}
		else
		{
			return null;
		}

	}

	private int[] getWellPositionFromA01( String well )
	{
		int[] wellPosition = new int[ 2 ];
		wellPosition[ 0 ] = Integer.parseInt( well.substring( 1, 3 ) ) - 1;
		wellPosition[ 1 ] = Utils.CAPITAL_ALPHABET.indexOf( well.substring( 0, 1 ) );
		return wellPosition;
	}

	private void updateMaxWellDimensionInData( int[] wellPosition )
	{
		for ( int d = 0; d < 2; ++d )
		{
			if ( wellPosition[ d ] >= maxWellDimensionsInData[ d ] )
			{
				maxWellDimensionsInData[ d ] = wellPosition[ d ];
			}
		}
	}

	private void updateMaxSiteDimensionInData( int[] sitePosition )
	{
		for ( int d = 0; d < 2; ++d )
		{
			if ( sitePosition[ d ] >= maxSiteDimensionsInData[ d ] )
			{
				maxSiteDimensionsInData[ d ] = sitePosition[ d ];
			}
		}
	}

}
