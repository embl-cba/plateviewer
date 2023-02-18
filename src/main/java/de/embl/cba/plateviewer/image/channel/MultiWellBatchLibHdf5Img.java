package de.embl.cba.plateviewer.image.channel;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.util.Utils;
import de.embl.cba.plateviewer.image.cellloader.MultiSiteHdf5Loader;
import de.embl.cba.plateviewer.image.MultiWellChannelFilesProviderBatchLibHdf5;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import java.awt.*;
import java.io.File;
import java.util.List;

public class MultiWellBatchLibHdf5Img< T extends RealType< T > & NativeType< T > > extends MultiWellImg< T >
{
	public static final String LUT_MIN_MAX = "ContrastLimits";
	public static final String COLOR = "Color";
	public static final String SKIP = "Skip";
	public static final String VISIBLE = "Visible";
	public static final String SCALE_FACTORS = "ScaleFactors";

	private final String hdf5DataSetName;

	public MultiWellBatchLibHdf5Img( List< File > files, String namingScheme, String channelName, int resolutionLevel )
	{
		super( files, namingScheme, resolutionLevel, channelName );

		this.channelName = channelName;

		this.hdf5DataSetName = channelName + "/s" + resolutionLevel;

		setHdf5ImageProperties( files.get( 0 ) );

		multiWellChannelFilesProvider = new MultiWellChannelFilesProviderBatchLibHdf5( files, hdf5DataSetName, imageDimensions, resolutionLevel );

		singleSiteChannelFiles = multiWellChannelFilesProvider.getSingleSiteChannelFiles();

		if ( resolutionLevel == 0 )
			wellNames = multiWellChannelFilesProvider.getWellNames();

		loader = new MultiSiteHdf5Loader( singleSiteChannelFiles );

		createCachedCellImg();
	}

	public static int[] getScaleFactors( File file, String channel )
	{
		final IHDF5Reader hdf5Reader = HDF5Factory.openForReading( file );

		final int[] scaleFactors = hdf5Reader.int32().getArrayAttr( channel, SCALE_FACTORS );

		return scaleFactors;
	}

	private void setHdf5ImageProperties( File file )
	{
		final IHDF5Reader hdf5Reader = HDF5Factory.openForReading( file );

		setLut( hdf5Reader, channelName );

		setImageDataType( hdf5Reader, hdf5DataSetName );

		setImageDimensions( hdf5Reader, hdf5DataSetName );
	}

	private void setLut( IHDF5Reader hdf5Reader, String channelName )
	{
		String colorName = hdf5Reader.string().getAttr( channelName, COLOR );

		if ( colorName.equals( "Gray" ) )
			colorName = "White";

		if ( colorName.equals( "Glasbey" ) )
		{
			colorName = "Gray";
			this.type = Metadata.Type.Segmentation;
		}
		else
		{
			this.type = Metadata.Type.Image;
		}

		final Color color = Utils.getColor( colorName );
		argbType = Utils.getARGBType( color );

		isInitiallyVisible = hdf5Reader.bool().getAttr( this.channelName, VISIBLE );

		setLutMinMax( hdf5Reader );
	}

	private void setLutMinMax( IHDF5Reader hdf5Reader )
	{
		if ( ! hdf5Reader.object().hasAttribute( channelName, LUT_MIN_MAX ) ) return;

		try
		{
			final double[] lutMinMax = hdf5Reader.float64().getArrayAttr( channelName, LUT_MIN_MAX );
			this.contrastLimits[ 0 ] = lutMinMax[ 0 ];
			this.contrastLimits[ 1 ] = lutMinMax[ 1 ];
		}
		catch ( Exception e )
		{
			this.contrastLimits[ 0 ] = 0;
			this.contrastLimits[ 1 ] = 1;
		}
	}

	private void setImageDimensions( IHDF5Reader hdf5Reader, String hdf5DataSetName )
	{
		final long[] dimensions = hdf5Reader.getDataSetInformation( hdf5DataSetName ).getDimensions();
		imageDimensions = new int[ 2 ];
		imageDimensions[ 0 ] = (int) dimensions[ 1 ]; // in hdf5 it is y,x
		imageDimensions[ 1 ] = (int) dimensions[ 0 ];
	}

	private void setImageDataType( IHDF5Reader hdf5Reader, String hdf5DataSetName )
	{
		final HDF5DataSetInformation information = hdf5Reader.getDataSetInformation( hdf5DataSetName );
		final String dataType = information.getTypeInformation().toString();
		final boolean signed = information.isSigned();

		if( dataType.equals( Utils.H5_BYTE ) && ! signed )
			nativeType = new UnsignedByteType();
		else if( dataType.equals( Utils.H5_SHORT ) && ! signed )
			nativeType = new UnsignedShortType();
		else if( dataType.equals( Utils.H5_INT ) && ! signed )
			nativeType = new UnsignedIntType();
		else if( dataType.equals( Utils.H5_FLOAT ) )
			nativeType = new FloatType();
		else
			throw new UnsupportedOperationException( "Hdf5 datatype not supported: " + dataType );
	}
}
