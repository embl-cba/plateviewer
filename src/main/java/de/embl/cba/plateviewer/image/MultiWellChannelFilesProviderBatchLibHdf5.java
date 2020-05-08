package de.embl.cba.plateviewer.image;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.plateviewer.util.Utils;
import net.imglib2.FinalInterval;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiWellChannelFilesProviderBatchLibHdf5 implements MultiWellChannelFilesProvider
{
	final List< File > files;
	private final String hdf5DataSetName;
	private final int resolutionLevel;

	int numSites, numWells;
	int[] siteDimensions; // for example, 2x2 sites
	int[] wellDimensions; // for example, 10x4 wells
	int[] imageDimensions; // for example, 1000x1000 pixels

	final ArrayList< SingleSiteChannelFile > singleSiteChannelFiles;

	final ArrayList< String > wellNames;

	static final String WELL_SITE_CHANNEL_PATTERN = NamingSchemes.PATTERN_NIKON_TI2_HDF5;
	public static final int WELL_GROUP = 1;
	public static final int SITE_GROUP = 2;

	public MultiWellChannelFilesProviderBatchLibHdf5( List< File > files, String hdf5DataSetName, int[] imageDimensions, int resolutionLevel )
	{
		this.files = files;
		this.hdf5DataSetName = hdf5DataSetName;
		this.resolutionLevel = resolutionLevel;
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
	private static String getWellName( String fileName )
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
			final SingleSiteChannelFile singleSiteChannelFile =
					new SingleSiteChannelFile(
						file,
						hdf5DataSetName,
						createInterval( file.getName() ),
						createSiteName( file.getName() ),
						getWellName( file.getName() ) );

			// TODO: This is too slow, because one needs to go into all the files, one could set a String Supplier here that reads it on demand
//			singleSiteChannelFile.setSiteInformation( readSiteInformation( file ) );
//			singleSiteChannelFile.setWellInformation( readWellInformation( file ) );
			singleSiteChannelFiles.add( singleSiteChannelFile );
		}
	}

	private String readSiteInformation( File file )
	{
		try
		{
			final IHDF5Reader reader = HDF5Factory.openForReading( file );
			return reader.string().getAttr( "/", "ImageInformation" );
		}
		catch ( Exception e )
		{
			return "No ImageInformation found";
		}
	}

	private String readWellInformation( File file )
	{
		try
		{
			final IHDF5Reader reader = HDF5Factory.openForReading( file );
			return reader.string().getAttr( "/", "WellInformation" );
		}
		catch ( Exception e )
		{
			return "No WellInformation found";
		}
	}

	public static String createSiteName( String fileName )
	{
		return getWellName( fileName ) + "-" + getSiteName( fileName);
	}

	private static String getSiteName( String fileName )
	{
		final Matcher matcher = Pattern.compile( WELL_SITE_CHANNEL_PATTERN ).matcher( fileName );

		if ( matcher.matches() )
		{
			String siteName = matcher.group( 2 );
			return siteName;
		}
		else
		{
			throw new UnsupportedOperationException( "Could not match naming scheme pattern " + WELL_SITE_CHANNEL_PATTERN + " to file " + fileName );
		}
	}

	private void configWells( List< File > files )
	{
		int[] maximalWellPositionsInData = getMaximalWellPositionsInData( files );

		wellDimensions = Utils.guessWellDimensions( maximalWellPositionsInData );
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

	private FinalInterval createInterval( String filePath )
	{
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
			throw new UnsupportedOperationException( "Could not match file name: " + filePath );
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
