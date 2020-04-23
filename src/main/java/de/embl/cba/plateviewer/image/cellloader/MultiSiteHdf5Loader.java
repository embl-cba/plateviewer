package de.embl.cba.plateviewer.image.cellloader;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.image.SingleSiteChannelFile;
import net.imglib2.cache.img.SingleCellArrayImg;

import java.io.File;
import java.util.ArrayList;

public class MultiSiteHdf5Loader extends MultiSiteLoader
{
	public MultiSiteHdf5Loader( ArrayList< SingleSiteChannelFile > singleSiteChannelFiles )
	{
		super( singleSiteChannelFiles );
	}

	@Override
	public synchronized void load( final SingleCellArrayImg cell )
	{
		SingleSiteChannelFile singleSiteChannelFile = getChannelSource( cell );

		if ( singleSiteChannelFile != null )
		{
			loadHdf5IntoCell( cell, singleSiteChannelFile.getFile(), singleSiteChannelFile.getHdf5DataSetName() );
		}
	}

	private void loadHdf5IntoCell( SingleCellArrayImg cell, File file, String hdf5DataSetName )
	{
		final IHDF5Reader hdf5Reader = HDF5Factory.openForReading( file );
		final HDF5DataSetInformation information = hdf5Reader.getDataSetInformation( hdf5DataSetName );
		final String dataType = information.getTypeInformation().toString();
		final boolean signed = information.isSigned();

		if ( dataType.equals( Utils.H5_BYTE ) && ! signed )
		{
			final byte[] data = hdf5Reader.uint8().readArray( hdf5DataSetName );
			final byte[] celldata = ( byte[] ) cell.getStorageArray();
			System.arraycopy( data, 0, celldata, 0, celldata.length );
		}
		else if ( dataType.equals( Utils.H5_SHORT ) && ! signed  )
		{
			final short[] data = hdf5Reader.uint16().readArray( hdf5DataSetName );
			final short[] celldata = ( short[] ) cell.getStorageArray();
			System.arraycopy( data, 0, celldata, 0, celldata.length );
		}
		else if ( dataType.equals( Utils.H5_INT ) && ! signed )
		{
			final int[] data = hdf5Reader.uint32().readArray( hdf5DataSetName );
			final int[] celldata = ( int[] ) cell.getStorageArray();
			System.arraycopy( data, 0, celldata, 0, celldata.length );
		}
		else if ( dataType.equals( Utils.H5_FLOAT ) )
		{
			final float[] data = hdf5Reader.float32().readArray( hdf5DataSetName );
			final float[] celldata = ( float[] ) cell.getStorageArray();
			System.arraycopy( data, 0, celldata, 0, celldata.length );
		}
		else
		{
			throw new UnsupportedOperationException( "Hdf5 datatype not supported: " + dataType );
		}
	}
}
