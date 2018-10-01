package de.embl.cba.gridviewer.imagesources;

import de.embl.cba.gridviewer.Utils;
import net.imglib2.FinalInterval;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageSourcesGeneratorMDMultiSite implements ImageSourcesGenerator
{
	final ArrayList< File > files;

	int numSites;
	int[] siteDimensions;
	int[] wellDimensions;
	int[] imageDimensions;

	final private ArrayList< ImageSource > imageSources;

	final private ArrayList< String > wellNames;

	final static String NAMING_SCHEME = Utils.PATTERN_MD_A01_SITE_WAVELENGTH;
	public static final int NAMING_SCHEME_WELL_GROUP = 1;
	public static final int NAMING_SCHEME_SITE_GROUP = 2;


	public ImageSourcesGeneratorMDMultiSite( ArrayList< File > files, int[] imageDimensions )
	{
		this.files = files;
		this.imageDimensions = imageDimensions;

		this.imageSources = new ArrayList<>();

		setImageSources();

		wellNames = Utils.getWellNames( files, NAMING_SCHEME, NAMING_SCHEME_WELL_GROUP );
	}

	public ArrayList< ImageSource > getImageSources()
	{
		return imageSources;
	}

	private void setImageSources()
	{
		configWells( files );
		configSites( files );

		for ( File file : files )
		{
			final ImageSource imageSource = new ImageSource(
					file,
					getInterval( file ),
					getPositionName(file.getName() ),
					getWellName(file.getName() ) );

			imageSources.add( imageSource );
		}
	}

	private String getPositionName( String fileName )
	{
		return fileName;
	}

	private void configWells( ArrayList< File > files )
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


	private void configSites( ArrayList< File > files )
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

	private int getNumSites( ArrayList< File > files )
	{
		Set< String > sites = new HashSet<>( );

		for ( File file : files )
		{
			final String pattern = Utils.getNamingScheme( file );

			final Matcher matcher = Pattern.compile( pattern ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				sites.add( matcher.group( NAMING_SCHEME_SITE_GROUP ) );
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
			final Matcher matcher = Pattern.compile( NAMING_SCHEME ).matcher( file.getName() );

			matcher.matches();

			int[] wellPosition = getWellPositionFromA01( matcher.group( NAMING_SCHEME_WELL_GROUP ) );

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
			int[] wellPosition = getWellPositionFromA01( matcher.group( NAMING_SCHEME_WELL_GROUP ) );
			int[] sitePosition = getSitePositionFromSiteIndex( matcher.group( NAMING_SCHEME_SITE_GROUP ) );

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

	private int[] getWellPositionFromA01( String well )
	{
		int[] wellPosition = new int[ 2 ];
		wellPosition[ 0 ] = Integer.parseInt( well.substring( 1, 3 ) ) - 1;
		wellPosition[ 1 ] = Utils.CAPITAL_ALPHABET.indexOf( well.substring( 0, 1 ) );
		return wellPosition;
	}


}
