package de.embl.cba.multipositionviewer;

import net.imglib2.FinalInterval;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageSourcesGeneratorALMFScreening implements ImageSourcesGenerator
{

	final ArrayList< File > files;

	int numSites, numWells;
	int[] siteDimensions;
	int[] wellDimensions;
	int[] imageDimensions;

	final ArrayList< ImageSource > imageSources;

	final ArrayList< String > wellNames;


	final String NAMING_SCHEME = Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00;
	public static final int NAMING_SCHEME_WELL_GROUP = 1;
	public static final int NAMING_SCHEME_SITE_GROUP = 2;


	public ImageSourcesGeneratorALMFScreening( ArrayList< File > files, int[] imageDimensions )
	{
		this.files = files;
		this.imageSources = new ArrayList<>();
		this.imageDimensions = imageDimensions;

		createImageSources();

		wellNames = Utils.getWellNames( files, NAMING_SCHEME, NAMING_SCHEME_WELL_GROUP );


	}
	@Override
	public ArrayList< ImageSource > getImageSources()
	{
		return imageSources;
	}

	@Override
	public ArrayList< String > getWellNames()
	{
		return wellNames;
	}

	private void createImageSources()
	{
		configWells( files );
		configSites( files );

		for ( File file : files )
		{

			final ImageSource imageSource = new ImageSource(
					file,
					getInterval( file, NAMING_SCHEME, wellDimensions[ 0 ], siteDimensions[ 0 ] ),
					file.getName());

			imageSources.add( imageSource );

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
				if ( NAMING_SCHEME.equals( Utils.PATTERN_ALMF_SCREENING_W0001_P000_C00 ) )
				{
					sites.add( matcher.group( NAMING_SCHEME_SITE_GROUP ) );
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
			final Matcher matcher = Pattern.compile( NAMING_SCHEME ).matcher( file.getName() );

			matcher.matches();

			wells.add( matcher.group( NAMING_SCHEME_WELL_GROUP ) );

			int wellNum = Integer.parseInt( matcher.group( NAMING_SCHEME_WELL_GROUP ) );

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

			int wellNum = Integer.parseInt( matcher.group( NAMING_SCHEME_WELL_GROUP ) ) - 1;
			int siteNum = Integer.parseInt( matcher.group( NAMING_SCHEME_SITE_GROUP ) );

			wellPosition[ 1 ] = wellNum / numWellColumns;
			wellPosition[ 0 ] = wellNum % numWellColumns;

			sitePosition[ 1 ] = siteNum / numSiteColumns;
			sitePosition[ 0 ] = siteNum % numSiteColumns;

			final FinalInterval interval = Utils.createInterval( wellPosition, sitePosition, siteDimensions, imageDimensions );

			return interval;
		}
		else
		{
			return null;
		}

	}


}
