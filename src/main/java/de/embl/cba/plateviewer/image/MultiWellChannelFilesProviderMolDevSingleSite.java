package de.embl.cba.plateviewer.image;

import de.embl.cba.plateviewer.util.Utils;
import net.imglib2.FinalInterval;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiWellChannelFilesProviderMolDevSingleSite implements MultiWellChannelFilesProvider
{
	final List< File > files;

	int numSites, numWells;
	int[] siteDimensions;
	int[] wellDimensions;
	int[] maxWellDimensionsInData;
	int[] maxSiteDimensionsInData;
	int[] imageDimensions;

	final ArrayList< SingleSiteChannelFile > singleSiteChannelFiles;

	final static String NAMING_SCHEME = NamingSchemes.PATTERN_MD_A01_WAVELENGTH;
	public static final int NAMING_SCHEME_WELL_GROUP = 1;

	final ArrayList< String > wellNames;

	public MultiWellChannelFilesProviderMolDevSingleSite( List< File > files, int[] imageDimensions )
	{
		this.files = files;
		this.singleSiteChannelFiles = new ArrayList<>();
		this.imageDimensions = imageDimensions;

		this.maxWellDimensionsInData = new int[ 2 ];
		this.maxSiteDimensionsInData = new int[ 2 ];

		createImageFileList();

		wellNames = Utils.getWellNames( files, NAMING_SCHEME, NAMING_SCHEME_WELL_GROUP );
	}

	public ArrayList< SingleSiteChannelFile > getSingleSiteChannelFiles()
	{
		return singleSiteChannelFiles;
	}

	public ArrayList< String > getWellNames()
	{
		return wellNames;
	}

	private void createImageFileList()
	{

		configWells( files );
		configSites( files );

		for ( File file : files )
		{

			final SingleSiteChannelFile singleSiteChannelFile = new SingleSiteChannelFile(
					file,
					getInterval( file ),
					getPositionName(file.getName() ),
					getWellName( file.getName() ));

			singleSiteChannelFiles.add( singleSiteChannelFile );
		}
	}

	private String getPositionName( String fileName )
	{
		return fileName;
	}

	private String getWellName( String fileName )
	{
		final Matcher matcher = Pattern.compile( NAMING_SCHEME ).matcher( fileName );

		if ( matcher.matches() )
		{
			final String well = matcher.group( NAMING_SCHEME_WELL_GROUP );
			return well;
		}
		else
		{
			return null;
		}
	}

	private void configWells( List< File > files )
	{
		int[] maximalWellPositionsInData = getMaximalWellPositionsInData( files );

		wellDimensions = Utils.guessWellDimensions( maximalWellPositionsInData );

		Utils.log( "Distinct wells: " +  numWells );
		Utils.log( "Well dimensions [ 0 ] : " +  wellDimensions[ 0 ] );
		Utils.log( "Well dimensions [ 1 ] : " +  wellDimensions[ 1 ] );
	}

	private void configSites( List< File > files )
	{
		numSites = 1; //getNumSites( files );
		siteDimensions = new int[ 2 ];
		Arrays.fill( siteDimensions, 1 );

		Utils.log( "Distinct sites: " +  numSites );
		Utils.log( "Site dimensions [ 0 ] : " +  siteDimensions[ 0 ] );
		Utils.log( "Site dimensions [ 1 ] : " +  siteDimensions[ 1 ] );
	}

	private int[] getMaximalWellPositionsInData( List< File > files )
	{
		int[] maximalWellPosition = new int[ 2 ];

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( NAMING_SCHEME ).matcher( file.getName() );

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

		final Matcher matcher = Pattern.compile( NAMING_SCHEME ).matcher( filePath );

		if ( matcher.matches() )
		{

			int[] sitePosition = new int[ ]{ 1, 1};
			int[] wellPosition = getWellPositionFromA01( matcher.group( 1 ) );

			final FinalInterval interval = Utils.createInterval( wellPosition, sitePosition, siteDimensions, imageDimensions );

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
