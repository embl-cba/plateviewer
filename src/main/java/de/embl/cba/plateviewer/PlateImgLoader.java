package de.embl.cba.plateviewer;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class PlateImgLoader implements CellLoader
{
	final int[] cellDimensions;
	final int bitDepth;
	final Map< String, File > cellFileMap;

	public PlateImgLoader( int[] cellDimensions, int bitDepth, Map< String, File > cellFileMap )
	{
		this.cellDimensions = cellDimensions;
		this.bitDepth = bitDepth;
		this.cellFileMap = cellFileMap;
	}

	@Override
	public void load( final SingleCellArrayImg cell ) throws Exception
	{
		final int[] position = new int[ 2 ];

		for ( int d = 0; d < 2; ++d )
		{
			position[ d ] = (int) cell.min( d ) / cellDimensions[ d ];
		}

		String key = Utils.getCellString( position );

		if ( cellFileMap.containsKey( key ) )
		{
			loadImageIntoCell( cell, cellFileMap.get( key ) );
		}

	}

	private void loadImageIntoCell( SingleCellArrayImg cell, File file )
	{
		final ImagePlus imp = IJ.openImage( file.getAbsolutePath() );

		if ( bitDepth == 8 )
		{
			final byte[] impdata = ( byte[] ) imp.getProcessor().getPixels();
			final byte[] celldata = ( byte[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
		else if ( bitDepth == 16 )
		{
			final short[] impdata = ( short[] ) imp.getProcessor().getPixels();
			final short[] celldata = ( short[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
		else if ( bitDepth == 32 )
		{
			final float[] impdata = ( float[] ) imp.getProcessor().getPixels();
			final float[] celldata = ( float[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
	}

}
