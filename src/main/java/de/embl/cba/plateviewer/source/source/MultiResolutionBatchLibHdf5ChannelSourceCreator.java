package de.embl.cba.plateviewer.source.source;

import de.embl.cba.plateviewer.source.channel.MultiWellBatchLibHdf5Source;
import de.embl.cba.plateviewer.util.Utils;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.io.File;
import java.util.List;

public class MultiResolutionBatchLibHdf5ChannelSourceCreator< R extends NativeType< R > & RealType< R > >
{
	public static final int NUM_DIMENSIONS = 3;
	private final String namingScheme;
	private final String channelName;
	private final List< File > channelFiles;
	private RandomAccessibleIntervalPlateViewerSource< R > source;
	private MultiWellBatchLibHdf5Source< R > multiWellHdf5CachedCellImage;

	public MultiResolutionBatchLibHdf5ChannelSourceCreator( String namingScheme,
															String channelName,
															List< File > channelFiles)
	{
		this.namingScheme = namingScheme;
		this.channelName = channelName;
		this.channelFiles = channelFiles;
	}

	public MultiWellBatchLibHdf5Source< R > createMultiWellHdf5CachedCellImage()
	{
		final int[] scaleFactors = MultiWellBatchLibHdf5Source.getScaleFactors( channelFiles.get( 0 ), channelName );

		RandomAccessibleInterval< R >[] rais = new RandomAccessibleInterval[ scaleFactors.length ];
		double[][] mipmapScales = new double[ scaleFactors.length ][ NUM_DIMENSIONS ];

		Utils.log( "Number of resolution levels: " + scaleFactors.length);

		for ( int resolutionLevel = 0; resolutionLevel < scaleFactors.length; resolutionLevel++ )
		{
			final MultiWellBatchLibHdf5Source< R > cachedCellImage
					= new MultiWellBatchLibHdf5Source<>(
						channelFiles,
						namingScheme,
						channelName,
						resolutionLevel );

			rais[ resolutionLevel ] = Views.addDimension( cachedCellImage.getRAI(), 0, 0);

			for ( int d = 0; d < NUM_DIMENSIONS; d++ )
			{
				mipmapScales[ resolutionLevel ][ d ] = scaleFactors[ resolutionLevel ];
			}

			if ( resolutionLevel == 0 )
			{
				this.multiWellHdf5CachedCellImage = cachedCellImage;
			}
		}

		final VoxelDimensions voxelDimensions =
				new FinalVoxelDimensions("pixel", 1, 1, 1 );

		for ( int resolutionLevel = 1; resolutionLevel < scaleFactors.length; resolutionLevel++ )
		{
			for ( int d = 0; d < NUM_DIMENSIONS; d++ )
			{
				final double scale = 1.0 * rais[ 0 ].dimension( d ) / rais[ resolutionLevel ].dimension( d );
				mipmapScales[ resolutionLevel ][ d ] = scale;
			}
		}

		source = new RandomAccessibleIntervalPlateViewerSource<>(
				rais,
				Util.getTypeFromInterval( rais[ 0 ] ),
				mipmapScales,
				voxelDimensions,
				channelName );

		multiWellHdf5CachedCellImage.setSource( source );

		return multiWellHdf5CachedCellImage;
	}
}
