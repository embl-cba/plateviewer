package de.embl.cba.gridviewer.imagesources;

import de.embl.cba.gridviewer.Utils;
import net.imglib2.FinalInterval;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageSourcesGeneratorScanR implements ImageSourcesGenerator
{

	final ArrayList< File > files;

	int numSites, numWells;
	int[] siteDimensions;
	int[] wellDimensions;
	int[] imageDimensions;

	final ArrayList< ImageSource > imageSources;

	final ArrayList< String > wellNames;

	final String WELL_SITE_CHANNEL_PATTERN = NamingSchemes.PATTERN_SCANR_WELL_SITE_CHANNEL;
	public static final int WELL_GROUP = 1;
	public static final int SITE_GROUP = 2;


	public ImageSourcesGeneratorScanR( ArrayList< File > files, int[] imageDimensions )
	{
		this.files = files;
		this.imageSources = new ArrayList<>();
		this.imageDimensions = imageDimensions;

		createImageSources();

		this.wellNames = getWellNames( files );

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

	private static ArrayList< String > getWellNames( ArrayList< File > files )
	{
		Set< String > wellNameSet = new HashSet<>(  );

		for ( File file : files )
		{
			wellNameSet.add( getWellName( file.getName() ) );
		}

		return new ArrayList<>( wellNameSet );

	}

	private static String getWellName( String fileName )
	{
		final Matcher matcher = Pattern.compile(  NamingSchemes.PATTERN_SCANR_WELLNAME_WELLNUM ).matcher( fileName );

		if ( matcher.matches() )
		{
			String wellName = matcher.group( 1 );
			return wellName;
		}
		else
		{
			return null;
		}
	}

	private void createImageSources()
	{
		configWells( files );
		configSites( files );

		for ( File file : files )
		{
			final ImageSource imageSource = new ImageSource(
					file,
					getInterval( file, WELL_SITE_CHANNEL_PATTERN, wellDimensions[ 0 ], siteDimensions[ 0 ] ),
					file.getName(),
					getWellName( file.getName() ) );

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

			final Matcher matcher = Pattern.compile( WELL_SITE_CHANNEL_PATTERN ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				sites.add( matcher.group( SITE_GROUP ) );
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
			final Matcher matcher = Pattern.compile( WELL_SITE_CHANNEL_PATTERN ).matcher( file.getName() );

			matcher.matches();

			wells.add( matcher.group( WELL_GROUP ) );

			int wellNum = Integer.parseInt( matcher.group( WELL_GROUP ) );

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

			int wellNum = Integer.parseInt( matcher.group( WELL_GROUP ) ) - 1;
			int siteNum = Integer.parseInt( matcher.group( SITE_GROUP ) );

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
