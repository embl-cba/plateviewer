package de.embl.cba.multipositionviewer;

import net.imglib2.FinalInterval;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageSourcesGeneratorALMFScreening
{

	final ArrayList< File > files;

	int numSites, numWells;
	int[] siteDimensions;
	int[] wellDimensions;
	int[] maxWellDimensionsInData;
	int[] maxSiteDimensionsInData;
	int[] imageDimensions;

	final ArrayList< ImageSource > list;

	final String filenamePattern = Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00;

	public ImageSourcesGeneratorALMFScreening( ArrayList< File > files, int[] imageDimensions )
	{
		this.files = files;
		this.list = new ArrayList<>();
		this.imageDimensions = imageDimensions;

		this.maxWellDimensionsInData = new int[ 2 ];
		this.maxSiteDimensionsInData = new int[ 2 ];

		createList();

	}

	public ArrayList< ImageSource > getFileList()
	{
		return list;
	}

	private void createList()
	{

		configWells( files );
		configSites( files );

		for ( File file : files )
		{

			final ImageSource imageSource = new ImageSource(
					file,
					getInterval( file, filenamePattern, wellDimensions[ 0 ], siteDimensions[ 0 ] ),
					file.getName());

			list.add( imageSource );

		}
	}

	private void configWells( ArrayList< File > files )
	{
		numWells = getNumWells( files );

		wellDimensions = Utils.guessWellDimensions( numWells );

		Utils.log( "Distinct wells: " +  numWells );
		Utils.log( "Well dimensions [ 0 ] : " +  wellDimensions[ 0 ] );
		Utils.log( "Well dimensions [ 1 ] : " +  wellDimensions[ 1 ] );
	}

	private void configSites( ArrayList< File > files )
	{
		numSites = getNumSites( files );
		siteDimensions = new int[ 2 ];

		for ( int d = 0; d < siteDimensions.length; ++d )
		{
			siteDimensions[ d ] = ( int ) Math.ceil( Math.sqrt( numSites ) );
			siteDimensions[ d ] = Math.max( 1, siteDimensions[ d ] );
		}

		Utils.log( "Distinct sites: " +  numSites );
		Utils.log( "Site dimensions [ 0 ] : " +  siteDimensions[ 0 ] );
		Utils.log( "Site dimensions [ 1 ] : " +  siteDimensions[ 1 ] );
	}

	private int getNumSites( ArrayList< File > files )
	{
		Set< String > sites = new HashSet<>( );

		for ( File file : files )
		{
			final String pattern = Utils.getNamingScheme( file );

			final Matcher matcher = Pattern.compile( pattern ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				if ( filenamePattern.equals( Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00 ) )
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

	private int getNumWells( ArrayList< File > files )
	{
		Set< String > wells = new HashSet<>( );
		int maxWellNum = 0;

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( filenamePattern ).matcher( file.getName() );

			matcher.matches();

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

	private FinalInterval getInterval( File file, String pattern, int numWellColumns, int numSiteColumns )
	{
		String filePath = file.getAbsolutePath();

		final Matcher matcher = Pattern.compile( pattern ).matcher( filePath );

		if ( matcher.matches() )
		{
			int[] wellPosition = new int[ 2 ];
			int[] sitePosition = new int[ 2 ];

			int wellNum = Integer.parseInt( matcher.group( 1 ) ) - 1;
			int siteNum = Integer.parseInt( matcher.group( 2 ) );

			wellPosition[ 1 ] = wellNum / numWellColumns;
			wellPosition[ 0 ] = wellNum % numWellColumns;

			sitePosition[ 1 ] = siteNum / numSiteColumns;
			sitePosition[ 0 ] = siteNum % numSiteColumns;

			// TODO: maybe below can be removed?
			updateMaxWellDimensionInData( wellPosition );
			updateMaxSiteDimensionInData( sitePosition );

			final FinalInterval interval = Utils.createInterval( wellPosition, sitePosition, siteDimensions, imageDimensions );

			return interval;
		}
		else
		{
			return null;
		}

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
