package de.embl.cba.plateviewer.channel;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.image.channel.MultiWellBatchLibHdf5Img;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Channels
{
	public static Map< String, ChannelProperties > getChannels( List< File > files, String namingScheme )
	{
		final HashMap< String, ChannelProperties > channelNameToProperties = new HashMap<>();

		if ( namingScheme.equals( NamingSchemes.PATTERN_MD_A01_SITE ) )
		{
			return fetchChannelsMDA01( channelNameToProperties );
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			return fetchChannelsNikonTi2Hdf5( files, channelNameToProperties );
		}
		else
		{
			return fetchChannels( files, namingScheme, channelNameToProperties );
		}
	}

	public static Map< String, ChannelProperties > fetchChannels( List< File > files, String namingScheme, HashMap< String, ChannelProperties > channelNameToProperties )
	{
		Set< String > channelPatternSet = new HashSet<>(  );
		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( namingScheme ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				if ( namingScheme.equals( NamingSchemes.PATTERN_ALMF_SCREENING_WELL_SITE_CHANNEL ) )
				{
					channelPatternSet.add( ".*" + matcher.group( 3 ) + "\\..*" );
				}
				else if ( namingScheme.equals( NamingSchemes.PATTERN_SCANR_WELLNUM_SITENUM_CHANNEL ) )
				{
					channelPatternSet.add( ".*" + matcher.group( 3 ) + "\\..*"  );
				}
				else if ( namingScheme.equals( NamingSchemes.PATTERN_MD_A01_SITE_WAVELENGTH ) )
				{
					channelPatternSet.add( ".*_s.*_w" + matcher.group( 3 ) + ".*" );
				}
				else if ( namingScheme.equals( NamingSchemes.PATTERN_MD_A01_WAVELENGTH ) )
				{
					channelPatternSet.add( ".*" + matcher.group( 2 ) + "\\..*" );
				}
			}
		}

		ArrayList< String > channelPatterns = new ArrayList<>( channelPatternSet );

		for ( String channelPattern : channelPatterns )
		{
			final ChannelProperties properties = new ChannelProperties( channelPattern, channelPattern, true );
			channelNameToProperties.put( channelPattern, properties );
		}

		return channelNameToProperties;
	}

	public static Map< String, ChannelProperties > fetchChannelsNikonTi2Hdf5( List< File > files, HashMap< String, ChannelProperties > channelNameToProperties )
	{
		final IHDF5Reader hdf5Reader = HDF5Factory.openForReading( files.get( 0 ) );
		final List< String > groupMembers = hdf5Reader.getGroupMembers( "/" );
		for ( String groupMember : groupMembers )
		{
			if ( ! hdf5Reader.hasAttribute( groupMember, MultiWellBatchLibHdf5Img.SKIP ) )
			{
				continue;
			}

			final boolean skip = hdf5Reader.bool().getAttr( groupMember, MultiWellBatchLibHdf5Img.SKIP );

			if ( skip )
			{
				continue;
			}


			final boolean visible = hdf5Reader.bool().getAttr( groupMember, MultiWellBatchLibHdf5Img.VISIBLE );

			final ChannelProperties properties = new ChannelProperties( groupMember, groupMember, visible );

			channelNameToProperties.put( groupMember, properties );
		}

		return channelNameToProperties;
	}

	public static Map< String, ChannelProperties > fetchChannelsMDA01( HashMap< String, ChannelProperties > channelNameToProperties )
	{
		final ChannelProperties properties = new ChannelProperties( ".*", ".*", true );
		channelNameToProperties.put(".*", properties);
		return channelNameToProperties;
	}
}
