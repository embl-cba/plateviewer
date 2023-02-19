package de.embl.cba.plateviewer.source;

import de.embl.cba.plateviewer.util.Utils;
import net.imglib2.FinalInterval;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.embl.cba.plateviewer.source.NamingSchemes.SITE;
import static de.embl.cba.plateviewer.source.NamingSchemes.WELL;

public class DefaultMultiWellMultiSiteChannelFilesProvider implements MultiWellChannelFilesProvider
{
	final List< File > files;

	int numSites;
	int[] siteDimensions;
	int[] wellDimensions;
	int[] imageDimensions;

	final private ArrayList< SingleSiteChannelFile > singleSiteChannelFiles;
	final private ArrayList< String > wellNames;
	final private String namingScheme;

	// This assumes an A01 naming scheme for the wells
	public DefaultMultiWellMultiSiteChannelFilesProvider( List< File > files, int[] imageDimensions, String namingScheme )
	{
		this.files = files;
		this.imageDimensions = imageDimensions;
		this.namingScheme = namingScheme;
		this.singleSiteChannelFiles = new ArrayList<>();
		init();
		wellNames = Utils.getWellNames( files, namingScheme );
	}

	public ArrayList< SingleSiteChannelFile > getSingleSiteChannelFiles()
	{
		return singleSiteChannelFiles;
	}

	private void init()
	{
		configWells( files );
		configSites( files );

		for ( File file : files )
		{
			final SingleSiteChannelFile singleSiteChannelFile = new SingleSiteChannelFile(
					file,
					getInterval( file ),
					getSiteName( file.getName() ),
					getWellName( file.getName() ) );

			singleSiteChannelFiles.add( singleSiteChannelFile );
		}
	}

	private String getSiteName( String fileName )
	{
		final Matcher matcher = Pattern.compile( namingScheme ).matcher( fileName );
		if ( ! matcher.matches() )
			throw new RuntimeException("Could not parse " + fileName );

		return matcher.group( WELL ) + "-" + matcher.group( SITE );
	}

	private void configWells( List< File > files )
	{
		int[] maximalWellPositionsInData = getMaximalWellPositionsInData( files );

		wellDimensions = Utils.guessWellDimensions( maximalWellPositionsInData );

		Utils.log( "Well dimensions [ 0 ] : " +  wellDimensions[ 0 ] );
		Utils.log( "Well dimensions [ 1 ] : " +  wellDimensions[ 1 ] );
	}

	public ArrayList< String > getWellNames()
	{
		return wellNames;
	}


	private void configSites( List< File > files )
	{
		numSites = getNumSites( files );
		siteDimensions = new int[ 2 ];
		siteDimensions[ 0 ] = (int) Math.ceil( Math.sqrt( numSites ) );
		siteDimensions[ 1 ] = (int) Math.ceil( 1.0 * numSites / siteDimensions[ 0 ] );

		Utils.log( "Site dimensions [ 0 ] : " +  siteDimensions[ 0 ] );
		Utils.log( "Site dimensions [ 1 ] : " +  siteDimensions[ 1 ] );
	}

	private String getWellName( String fileName )
	{
		final Matcher matcher = Pattern.compile( namingScheme ).matcher( fileName );
		if ( ! matcher.matches() )
			throw new RuntimeException("Could not parse " + fileName );
		return matcher.group( WELL );
	}

	private int getNumSites( List< File > files )
	{
		Set< String > sites = new HashSet<>( );

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( namingScheme ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				if ( matcher.group( NamingSchemes.SITE ) != null )
					sites.add( matcher.group( NamingSchemes.SITE ) );
				else
					break; // naming scheme does not have sites
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

	private int[] getMaximalWellPositionsInData( List< File > files )
	{
		int[] maximalWellPositions = new int[ 2 ];

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( namingScheme ).matcher( file.getName() );

			matcher.matches();

			final String well = matcher.group( WELL );
			int[] wellPosition = decodeWellPosition( well );

			for ( int d = 0; d < wellPosition.length; ++d )
				if ( wellPosition[ d ] > maximalWellPositions[ d ] )
					maximalWellPositions[ d ] = wellPosition[ d ];
		}

		return maximalWellPositions;
	}

	private FinalInterval getInterval( File file )
	{
		final Matcher matcher = Pattern.compile( namingScheme ).matcher( file.getName() );

		if ( ! matcher.matches() )
			throw new RuntimeException( "Could not parse " + file.getName() );

		int[] wellPosition = decodeWellPosition( matcher.group( WELL ) );
		int[] sitePosition = decodeSitePosition( matcher.group( NamingSchemes.SITE ) );

		final FinalInterval interval = Utils.createInterval( wellPosition, sitePosition, siteDimensions, imageDimensions );

		return interval;
	}

	private int[] decodeWellPosition( String well )
	{
		if ( namingScheme.equals( NamingSchemes.PATTERN_OPERETTA ) )
		{
			final Matcher matcher = Pattern.compile( "r(?<row>[0-9]{2})c(?<col>[0-9]{2})" ).matcher( well );
			if ( ! matcher.matches() )
				throw new RuntimeException( "Could not decode well " + well );

			final int row = Integer.parseInt( matcher.group( "row" ) ) - 1;
			final int col = Integer.parseInt( matcher.group( "col" ) ) - 1;
			return new int[]{ row, col };
		}
		else
		{
			return Utils.getWellPositionFromA01( well );
		}
	}

	private int[] decodeSitePosition( String site )
	{
		int[] sitePosition = new int[ 2 ];
		int siteIndex = Integer.parseInt( site ) - 1;

		sitePosition[ 0 ] = siteIndex % siteDimensions[ 0 ]; // column
		sitePosition[ 1 ] = siteIndex / siteDimensions[ 0 ]; // row

		// System.out.println( "Site index = " + siteIndex + ", x = " + sitePosition[ 0 ] + ", y = " + sitePosition[ 1 ]);

		return sitePosition;
	}
}
