package de.embl.cba.plateviewer.image;

import de.embl.cba.plateviewer.util.Utils;
import net.imglib2.FinalInterval;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.embl.cba.plateviewer.image.NamingSchemes.WELL;

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
		setImageSources();
		wellNames = Utils.getWellNames( files, namingScheme );
	}

	public ArrayList< SingleSiteChannelFile > getSingleSiteChannelFiles()
	{
		return singleSiteChannelFiles;
	}

	private void setImageSources()
	{
		configWells( files );
		configSites( files );

		for ( File file : files )
		{
			final SingleSiteChannelFile singleSiteChannelFile = new SingleSiteChannelFile(
					file,
					getInterval( file ),
					getPositionName(file.getName() ),
					getWellName(file.getName() ) );

			singleSiteChannelFiles.add( singleSiteChannelFile );
		}
	}

	private String getPositionName( String fileName )
	{
		return fileName;
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
		siteDimensions[ 0 ] = (int) Math.sqrt( numSites );
		siteDimensions[ 1 ] = (int) Math.sqrt( numSites );

		Utils.log( "Site dimensions [ 0 ] : " +  siteDimensions[ 0 ] );
		Utils.log( "Site dimensions [ 1 ] : " +  siteDimensions[ 1 ] );
	}

	private String getWellName( String fileName )
	{
		final Matcher matcher = Pattern.compile( namingScheme ).matcher( fileName );

		if ( matcher.matches() )
		{
			final String well = matcher.group( WELL );
			return well;
		}
		else
		{
			return null;
		}
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
		int[] maximalWellPosition = new int[ 2 ];

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( namingScheme ).matcher( file.getName() );

			matcher.matches();

			final String well = matcher.group( WELL );
			int[] wellPosition = Utils.getWellPositionFromA01( well );

			for ( int d = 0; d < wellPosition.length; ++d )
				if ( wellPosition[ d ] > maximalWellPosition[ d ] )
					maximalWellPosition[ d ] = wellPosition[ d ];
		}

		return maximalWellPosition;
	}

	private FinalInterval getInterval( File file )
	{
		String filePath = file.getAbsolutePath();

		final Matcher matcher = Pattern.compile( namingScheme ).matcher( filePath );

		if ( matcher.matches() )
		{
			int[] wellPosition = Utils.getWellPositionFromA01( matcher.group( WELL ) );
			int[] sitePosition = getSitePositionFromSiteIndex( matcher.group( NamingSchemes.SITE ) );

			final FinalInterval interval = Utils.createInterval( wellPosition, sitePosition, siteDimensions, imageDimensions );

			return interval;
		}
		else
		{
			return null;
		}
	}

	private int[] getSitePositionFromSiteIndex( String site )
	{
		int[] sitePosition = new int[ 2 ];
		int siteIndex = Integer.parseInt( site ) - 1;

		sitePosition[ 0 ] = siteIndex % siteDimensions[ 1 ];
		sitePosition[ 1 ] = siteIndex / siteDimensions[ 1 ];

		return sitePosition;
	}
}
