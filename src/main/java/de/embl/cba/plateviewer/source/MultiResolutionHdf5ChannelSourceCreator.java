package de.embl.cba.plateviewer.source;

import bdv.util.RandomAccessibleIntervalMipmapSource;
import bdv.viewer.Source;
import de.embl.cba.plateviewer.source.cachedcellimg.MultiWellHdf5CachedCellImage;
import itc.utilities.IntervalUtils;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;

import java.io.File;
import java.util.List;

public class MultiResolutionHdf5ChannelSourceCreator < R extends NativeType< R > & RealType< R > >
{
	private final String namingScheme;
	private final String channelName;
	private final List< File > channelFiles;
	private RandomAccessibleIntervalMipmapSource< R > source;
	private MultiWellHdf5CachedCellImage< R > multiWellHdf5CachedCellImage;

	public MultiResolutionHdf5ChannelSourceCreator( String namingScheme,
													String channelName,
													List< File > channelFiles)
	{
		this.namingScheme = namingScheme;
		this.channelName = channelName;
		this.channelFiles = channelFiles;
	}

	public void create()
	{
		final int[] scaleFactors = MultiWellHdf5CachedCellImage.getScaleFactors( channelFiles.get( 0 ), channelName );

		RandomAccessibleInterval< R >[] rais = new RandomAccessibleInterval[ scaleFactors.length ];
		double[][] mipmapScales = new double[ scaleFactors.length ][ 3 ];

		for ( int resolutionLevel = 0; resolutionLevel < scaleFactors.length; resolutionLevel++ )
		{
			final MultiWellHdf5CachedCellImage< R > cachedCellImage
					= new MultiWellHdf5CachedCellImage<>(
						channelFiles,
						namingScheme,
						channelName,
						resolutionLevel );

			rais[ resolutionLevel ] = cachedCellImage.getCachedCellImg();
			mipmapScales[ resolutionLevel ][ 0 ] = scaleFactors[ resolutionLevel ];
			mipmapScales[ resolutionLevel ][ 1 ] = scaleFactors[ resolutionLevel ];
			mipmapScales[ resolutionLevel ][ 2 ] = scaleFactors[ resolutionLevel ];

			if ( resolutionLevel == 0 )
			{
				this.multiWellHdf5CachedCellImage = cachedCellImage;
			}
		}


		final VoxelDimensions voxelDimensions = new FinalVoxelDimensions("pixel", 1, 1, 1 );


		source = new RandomAccessibleIntervalMipmapSource<>(
				rais,
				Util.getTypeFromInterval( rais[ 0 ] ),
				mipmapScales,
				voxelDimensions,
				channelName );
	}

	public Source< R > getSource()
	{
		return source;
	}

	public MultiWellHdf5CachedCellImage< R > getMultiWellHdf5CachedCellImage()
	{
		return multiWellHdf5CachedCellImage;
	}
}
