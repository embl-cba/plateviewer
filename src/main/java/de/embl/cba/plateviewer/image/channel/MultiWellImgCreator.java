package de.embl.cba.plateviewer.image.channel;

import de.embl.cba.plateviewer.util.Utils;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.image.source.MultiResolutionBatchLibHdf5ChannelSourceCreator;
import de.embl.cba.plateviewer.io.FileUtils;

import java.io.File;
import java.util.List;

public class MultiWellImgCreator
{
	public static MultiWellImg create( List< File > fileList, String namingScheme, String channelPattern )
	{
		List< File > channelFiles = getChannelFiles( fileList, namingScheme, channelPattern );
		return createFromChannelFiles( channelFiles, namingScheme, channelPattern );
	}

	public static MultiWellImg createFromChannelFiles( List< File > channelFiles, String namingScheme, String channelPattern )
	{
		MultiWellImg wellImg;

		final String channelName = channelPattern;

		Utils.log( "Creating channel: " + channelName );

		if ( namingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			final MultiResolutionBatchLibHdf5ChannelSourceCreator sourceCreator =
					new MultiResolutionBatchLibHdf5ChannelSourceCreator(
							namingScheme,
							channelName,
							channelFiles );

			sourceCreator.create();

			wellImg = sourceCreator.getMultiWellHdf5CachedCellImage();

			wellImg.setSource( sourceCreator.getSource() );

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


	private static List< File > getChannelFiles( List < File > fileList, String namingScheme, String channelPattern )
	{
		Utils.log( "Fetching channel files..." );
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

		if ( channelFiles.size() == 0 )
			throw new UnsupportedOperationException( "Could not find files for channel: " + channelPattern );

		return channelFiles;
	}
}
