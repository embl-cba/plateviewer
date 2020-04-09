package de.embl.cba.plateviewer.imagesources;

import de.embl.cba.plateviewer.Utils;
import net.imglib2.FinalInterval;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageSourcesGeneratorCoronaHdf5 implements ImageSourcesGenerator
{
	final List< File > files;
	private final String hdf5DataSetName;

	int numSites, numWells;
	int[] siteDimensions;
	int[] wellDimensions;
	int[] imageDimensions;

	final ArrayList< ImageSource > imageSources;

	final ArrayList< String > wellNames;

	final String WELL_SITE_CHANNEL_PATTERN = NamingSchemes.PATTERN_CORONA_HDF5;
	public static final int WELL_GROUP = 1;
	public static final int SITE_GROUP = 2;

	public ImageSourcesGeneratorCoronaHdf5( List< File > files, String hdf5DataSetName, int[] imageDimensions )
	{
		this.files = files;
		this.hdf5DataSetName = hdf5DataSetName;
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

	private ArrayList< String > getWellNames( List< File > files )
	{
		Set< String > wellNameSet = new HashSet<>(  );

		for ( File file : files )
		{
			wellNameSet.add( getWellName( file.getName() ) );
		}

		return new ArrayList<>( wellNameSet );
	}

	// TODO: make static utility method
	private String getWellName( String fileName )
	{
		final Matcher matcher = Pattern.compile( WELL_SITE_CHANNEL_PATTERN ).matcher( fileName );

		if ( matcher.matches() )
		{
			String wellName = matcher.group( 1 );
			return wellName;
		}
		else
		{
			throw new UnsupportedOperationException( "Could not match naming scheme pattern " + WELL_SITE_CHANNEL_PATTERN + " to file " + fileName );
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
					hdf5DataSetName,
					getInterval( file ),
					file.getName(),
					getWellName( file.getName() ) );

			imageSources.add( imageSource );
		}
	}

	private void configWells( List< File > files )
	{
		int[] maximalWellPositionsInData = getMaximalWellPositionsInData( files );

		wellDimensions = Utils.guessWellDimensions( maximalWellPositionsInData );

		Utils.log( "Well dimensions [ 0 ] : " +  wellDimensions[ 0 ] );
		Utils.log( "Well dimensions [ 1 ] : " +  wellDimensions[ 1 ] );
	}

	private void configSites( List< File > files )
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

	private int getNumSites( List< File > files )
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

	private int[] getMaximalWellPositionsInData( List< File > files )
	{
		int[] maximalWellPosition = new int[ 2 ];

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( WELL_SITE_CHANNEL_PATTERN ).matcher( file.getName() );

			matcher.matches();

			int[] wellPosition = Utils.getWellPositionFromA01( matcher.group( WELL_GROUP ) );

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

		final Matcher matcher = Pattern.compile( WELL_SITE_CHANNEL_PATTERN ).matcher( filePath );

		if ( matcher.matches() )
		{
			int[] wellPosition = Utils.getWellPositionFromA01( matcher.group( WELL_GROUP ) );
			int[] sitePosition = getSitePositionFromSiteIndex( matcher.group( SITE_GROUP ) );

			final FinalInterval interval = Utils.createInterval( wellPosition, sitePosition, siteDimensions, imageDimensions );

			return interval;

		}
		else
		{
			throw new UnsupportedOperationException( "Could not match file name: " + file );
		}
	}

	private int[] getSitePositionFromSiteIndex( String site )
	{
		int[] sitePosition = new int[ 2 ];
		int siteIndex = Integer.parseInt( site );

		sitePosition[ 0 ] = siteIndex % siteDimensions[ 1 ];
		sitePosition[ 1 ] = siteIndex / siteDimensions[ 1 ];

		return sitePosition;
	}

}
