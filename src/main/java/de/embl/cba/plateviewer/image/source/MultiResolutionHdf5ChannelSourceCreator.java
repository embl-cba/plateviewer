package de.embl.cba.plateviewer.image.source;

import bdv.util.RandomAccessibleIntervalMipmapSource;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.util.volatiles.SharedQueue;
import bdv.viewer.Source;
import de.embl.cba.plateviewer.image.img.MultiWellHdf5CachedCellImg;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.io.File;
import java.util.List;

public class MultiResolutionHdf5ChannelSourceCreator < R extends NativeType< R > & RealType< R > >
{
	public static final int NUM_DIMENSIONS = 3;
	private final String namingScheme;
	private final String channelName;
	private final List< File > channelFiles;
	private RandomAccessibleIntervalMipmapWithOffsetSource< R > source;
	private MultiWellHdf5CachedCellImg< R > multiWellHdf5CachedCellImage;

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
		final int[] scaleFactors = MultiWellHdf5CachedCellImg.getScaleFactors( channelFiles.get( 0 ), channelName );

		RandomAccessibleInterval< R >[] rais = new RandomAccessibleInterval[ scaleFactors.length ];
		double[][] mipmapScales = new double[ scaleFactors.length ][ NUM_DIMENSIONS ];

		for ( int resolutionLevel = 0; resolutionLevel < scaleFactors.length; resolutionLevel++ )
		{
			final MultiWellHdf5CachedCellImg< R > cachedCellImage
					= new MultiWellHdf5CachedCellImg<>(
						channelFiles,
						namingScheme,
						channelName,
						resolutionLevel );

			rais[ resolutionLevel ] = Views.addDimension( cachedCellImage.getCachedCellImg(), 0, 0);

			for ( int d = 0; d < NUM_DIMENSIONS; d++ )
			{
				mipmapScales[ resolutionLevel ][ d ] = scaleFactors[ resolutionLevel ];
			}

			if ( resolutionLevel == 0 )
			{
				this.multiWellHdf5CachedCellImage = cachedCellImage;
			}
		}

		final VoxelDimensions voxelDimensions = new FinalVoxelDimensions("pixel", 1, 1 );

		source = new RandomAccessibleIntervalMipmapWithOffsetSource<>(
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

	public Source< ? extends Volatile< R > > getVolatileSource()
	{
		return source.asVolatile( new SharedQueue( 1 ) );
	}

	public MultiWellHdf5CachedCellImg< R > getMultiWellHdf5CachedCellImage()
	{
		return multiWellHdf5CachedCellImage;
	}
}
