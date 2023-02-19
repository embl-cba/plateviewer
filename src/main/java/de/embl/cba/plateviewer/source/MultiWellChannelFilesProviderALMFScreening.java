package de.embl.cba.plateviewer.source;

import de.embl.cba.plateviewer.util.Utils;
import net.imglib2.FinalInterval;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiWellChannelFilesProviderALMFScreening implements MultiWellChannelFilesProvider
{
	final List< File > files;

	private int numSites, numWells;
	private int[] siteDimensions;
	private int[] wellDimensions;
	private int[] imageDimensions;

	final ArrayList< SingleSiteChannelFile > singleSiteChannelFiles;
	ArrayList< String > wellNames;

	public static String namingScheme = NamingSchemes.PATTERN_ALMF_TREAT1_TREAT2_WELLNUM_POSNUM_CHANNEL;

	private boolean zeroBasedSites;

	public MultiWellChannelFilesProviderALMFScreening( List< File > files, int[] imageDimensions )
	{
		this.files = files;
		this.singleSiteChannelFiles = new ArrayList<>();
		this.imageDimensions = imageDimensions;

		createImageSources();
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

	public static String getWellName( String fileName )
	{
		final Matcher matcher = Pattern.compile( namingScheme ).matcher( fileName );

		if ( matcher.matches() )
		{
			String wellName = matcher.group( 1 );
			wellName += "--" + matcher.group( 2 );
			wellName += "--W" + matcher.group( 3 );
			return wellName;
		}
		else
		{
			throw new RuntimeException( "Could not parse " + fileName );
		}
	}

	public static String getSiteName( String fileName )
	{
		final Matcher matcher = Pattern.compile( namingScheme ).matcher( fileName );

		if ( matcher.matches() )
		{
			String name = matcher.group( 1 );
			name += "--" + matcher.group( 2 );
			name += "--W" + matcher.group( NamingSchemes.WELL );
			name += "--P" + matcher.group( NamingSchemes.SITE );
			return name;
		}
		else
		{
			throw new RuntimeException( "Could not parse " + fileName );
		}
	}

	private void createImageSources()
	{
		configWells( files );
		configSites( files );

		for ( File file : files )
		{
			final String fileName = FilenameUtils.removeExtension( file.getName() );
			final SingleSiteChannelFile singleSiteChannelFile =
					new SingleSiteChannelFile(
						file,
						getInterval( fileName ),
						getSiteName( fileName ),
						getWellName( fileName ) );

			singleSiteChannelFiles.add( singleSiteChannelFile );
		}
	}


	private void configWells( List< File > files )
	{
		numWells = getNumWells( files );

		wellDimensions = Utils.guessWellDimensions( numWells );

		Utils.log( "Number of wells: " +  numWells );
		Utils.log( "Well layout [ 0 ] : " +  wellDimensions[ 0 ] );
		Utils.log( "Well layout [ 1 ] : " +  wellDimensions[ 1 ] );

		wellNames = getWellNames( files );
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

		siteDimensions = new int[ 2 ];
		siteDimensions[ 0 ] = ( int ) Math.ceil( Math.sqrt( numSites ) );
		siteDimensions[ 1 ] = ( int ) Math.ceil( numSites / siteDimensions[ 0 ]  );

		Utils.log( "Number of sites: " +  numSites );
		Utils.log( "Site numbering is zero based: " +  zeroBasedSites );
		Utils.log( "Site layout [ 0 ] : " +  siteDimensions[ 0 ] );
		Utils.log( "Site layout [ 1 ] : " +  siteDimensions[ 1 ] );
	}

	private Set< Integer > getSitesSet( List< File > files )
	{
		Set< Integer > sites = new HashSet<>( );

		for ( File file : files )
		{
			final Matcher matcher =
					Pattern.compile( namingScheme ).matcher( file.getName() );

			if ( matcher.matches() )
				sites.add( Integer.parseInt( matcher.group( NamingSchemes.SITE ) ) );
		}

		return sites;
	}

	private int getNumWells( List< File > files )
	{
		Set< String > wells = new HashSet<>( );
		int maxWellNum = 0;

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( namingScheme ).matcher( file.getName() );

			matcher.matches();

			wells.add( matcher.group( NamingSchemes.WELL ) );

			int wellNum = Integer.parseInt( matcher.group( NamingSchemes.WELL ) );

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

	private FinalInterval getInterval( String fileName )
	{
		final Matcher matcher = Pattern.compile( namingScheme ).matcher( fileName );

		if ( matcher.matches() )
		{
			int[] wellPosition = new int[ 2 ];
			int[] sitePosition = new int[ 2 ];

			int wellIndex = Integer.parseInt( matcher.group( NamingSchemes.WELL ) ) - 1;
			int siteIndex = Integer.parseInt( matcher.group( NamingSchemes.SITE ) );
			if ( ! zeroBasedSites ) siteIndex -= 1;

			wellPosition[ 1 ] = wellIndex / wellDimensions[ 0 ];
			wellPosition[ 0 ] = wellIndex % wellDimensions[ 0 ];

			sitePosition[ 1 ] = siteIndex / siteDimensions[ 0 ];
			sitePosition[ 0 ] = siteIndex % siteDimensions[ 0 ];

			final FinalInterval interval =
					Utils.createInterval(
							wellPosition,
							sitePosition,
							siteDimensions,
							imageDimensions );

			return interval;
		}
		else
		{
			return null;
		}

	}


}
