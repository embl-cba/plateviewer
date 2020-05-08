package de.embl.cba.plateviewer.image;

import de.embl.cba.plateviewer.util.Utils;
import net.imglib2.FinalInterval;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiWellChannelFilesProviderALMFScreening implements MultiWellChannelFilesProvider
{
	final List< File > files;

	int numSites, numWells;
	int[] numSitesPerWell;
	int[] numWellsPerPlate;
	int[] imageDimensions;

	final ArrayList< SingleSiteChannelFile > singleSiteChannelFiles;
	final ArrayList< String > wellNames;

	final String WELL_SITE_CHANNEL_PATTERN = NamingSchemes.PATTERN_ALMF_SCREENING_WELL_SITE_CHANNEL;
	public static final int WELL_GROUP = 1;
	public static final int SITE_GROUP = 2;
	private boolean zeroBasedSites;

	public MultiWellChannelFilesProviderALMFScreening( List< File > files, int[] imageDimensions )
	{
		this.files = files;
		this.singleSiteChannelFiles = new ArrayList<>();
		this.imageDimensions = imageDimensions;

		createImageSources();

		this.wellNames = getWellNames( files );
	}

	@Override
	public ArrayList< SingleSiteChannelFile > getSingleSiteChannelFiles()
	{
		return singleSiteChannelFiles;
	}

	@Override
	public ArrayList< String > getWellNames()
	{
		return wellNames;
	}

	private static ArrayList< String > getWellNames( List< File > files )
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
		final Matcher matcher = Pattern.compile(  NamingSchemes.PATTERN_ALMF_SCREENING_TREAT1_TREAT2_WELLNUM ).matcher( fileName );

		if ( matcher.matches() )
		{
			String wellName = matcher.group( 1 );
			wellName += "--" + matcher.group( 2 );
			wellName += "--W" + matcher.group( 3 );
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
			final SingleSiteChannelFile singleSiteChannelFile = new SingleSiteChannelFile(
					file,
					getInterval( file, WELL_SITE_CHANNEL_PATTERN, numWellsPerPlate[ 0 ], numSitesPerWell[ 0 ] ),
					file.getName(),
					getWellName( file.getName() ) );

			singleSiteChannelFiles.add( singleSiteChannelFile );
		}
	}


	private void configWells( List< File > files )
	{
		numWells = getNumWells( files );

		numWellsPerPlate = Utils.guessWellDimensions( numWells );

		Utils.log( "Distinct wells: " +  numWells );
		Utils.log( "Well dimensions [ 0 ] : " +  numWellsPerPlate[ 0 ] );
		Utils.log( "Well dimensions [ 1 ] : " +  numWellsPerPlate[ 1 ] );
	}

	private void configSites( List< File > files )
	{
		final Set< Integer > sites = getSitesSet( files );

		if ( sites.size() == 0 )
			numSites = 1;
		else
			numSites = sites.size();

		for ( Integer site : sites )
			if ( site == 0 )
			{
				zeroBasedSites = true;
				break;
			}

		numSitesPerWell = new int[ 2 ];
		for ( int d = 0; d < numSitesPerWell.length; ++d )
		{
			numSitesPerWell[ d ] = ( int ) Math.ceil( Math.sqrt( numSites ) );
			numSitesPerWell[ d ] = Math.max( 1, numSitesPerWell[ d ] );
		}

		Utils.log( "Distinct sites: " +  numSites );
		Utils.log( "Sites are zero based: " +  zeroBasedSites );
		Utils.log( "Site dimensions [ 0 ] : " +  numSitesPerWell[ 0 ] );
		Utils.log( "Site dimensions [ 1 ] : " +  numSitesPerWell[ 1 ] );

	}

	private Set< Integer > getSitesSet( List< File > files )
	{
		Set< Integer > sites = new HashSet<>( );

		for ( File file : files )
		{
			final Matcher matcher =
					Pattern.compile( WELL_SITE_CHANNEL_PATTERN ).matcher( file.getName() );

			if ( matcher.matches() )
				sites.add( Integer.parseInt( matcher.group( SITE_GROUP ) ) );
		}

		return sites;
	}

	private int getNumWells( List< File > files )
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

	private FinalInterval getInterval(
			File file,
			String pattern,
			int numWellColumns,
			int numSiteColumns )
	{
		String filePath = file.getAbsolutePath();

		final Matcher matcher = Pattern.compile( pattern ).matcher( filePath );

		if ( matcher.matches() )
		{
			int[] wellPosition = new int[ 2 ];
			int[] sitePosition = new int[ 2 ];

			int wellNum = Integer.parseInt( matcher.group( WELL_GROUP ) ) - 1;

			int siteNum = Integer.parseInt( matcher.group( SITE_GROUP ) );
			if ( ! zeroBasedSites ) siteNum -= 1;

			wellPosition[ 1 ] = wellNum / numWellColumns;
			wellPosition[ 0 ] = wellNum % numWellColumns;

			sitePosition[ 1 ] = siteNum / numSiteColumns;
			sitePosition[ 0 ] = siteNum % numSiteColumns;

			final FinalInterval interval =
					Utils.createInterval(
							wellPosition,
							sitePosition,
							numSitesPerWell,
							imageDimensions );

			return interval;
		}
		else
		{
			return null;
		}

	}


}
