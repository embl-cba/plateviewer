package de.embl.cba.plateviewer;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;

import java.util.Map;

public class PlateImgLoader implements CellLoader
{
	final long cellWidth;
	final long cellHeight;
	final int bitDepth;
	final Map< long[], String > cellFileMap;

	public PlateImgLoader( long cellWidth, long cellHeight, int bitDepth, Map< long[], String > cellFileMap )
	{
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.bitDepth = bitDepth;
		this.cellFileMap = cellFileMap;
	}

	@Override
	public void load( final SingleCellArrayImg cell ) throws Exception
	{
		final long[] position = { cell.min( 0 ) / cellWidth, cell.min( 1 ) / cellHeight };

		if ( cellFileMap.containsKey( position ) )
		{
			loadImageIntoCell( cell, cellFileMap.get( position ) );
		}

	}

	private void loadImageIntoCell( SingleCellArrayImg cell, String file )
	{
		if ( bitDepth == 8 )
		{
			final ImagePlus imp = IJ.openImage( file );
			final byte[] impdata = ( byte[] ) imp.getProcessor().getPixels();
			final byte[] celldata = ( byte[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
		else if ( bitDepth == 16 )
		{
			final ImagePlus imp = IJ.openImage( file );
			final short[] impdata = ( short[] ) imp.getProcessor().getPixels();
			final short[] celldata = ( short[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
		else if ( bitDepth == 32 )
		{
			final ImagePlus imp = IJ.openImage( file );
			final float[] impdata = ( float[] ) imp.getProcessor().getPixels();
			final float[] celldata = ( float[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
	}

}
