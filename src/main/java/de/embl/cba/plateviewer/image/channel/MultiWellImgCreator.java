package de.embl.cba.plateviewer.image.channel;

import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.image.source.MultiResolutionBatchLibHdf5ChannelSourceCreator;
import de.embl.cba.plateviewer.io.FileUtils;

import java.io.File;
import java.util.List;

public class MultiWellImgCreator
{
	public static MultiWellImg create( List< File > fileList, String namingScheme, String channelPattern )
	{
		MultiWellImg wellImg;
		final String channelName = channelPattern;

		if ( ! channelName.equals( "nuclei" ) ) return null;

		Utils.log( "Adding channel: " + channelName );
		List< File > channelFiles = getChannelFiles( fileList, namingScheme, channelName );

		if ( namingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			final MultiResolutionBatchLibHdf5ChannelSourceCreator sourceCreator =
					new MultiResolutionBatchLibHdf5ChannelSourceCreator(
							namingScheme,
							channelName,
							channelFiles );

			sourceCreator.create();

			wellImg = sourceCreator.getMultiWellHdf5CachedCellImage();

			wellImg.setSource( sourceCreator.getVolatileSource() );

		}
		else
		{
			wellImg = new MultiWellImagePlusImg(
					channelFiles,
					channelName,
					namingScheme,
					0 );

		}
		return wellImg;
	}

	public static List< File > getChannelFiles( List < File > fileList, String namingScheme, String channelPattern )
	{
		List< File > channelFiles;
		if ( namingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			// each file contains all channels => we need all
			channelFiles = fileList;
		} else
		{
			// one channel per file => we need to filter the relevant files
			channelFiles = FileUtils.filterFiles( fileList, channelPattern );
		}

		return channelFiles;
	}
}
