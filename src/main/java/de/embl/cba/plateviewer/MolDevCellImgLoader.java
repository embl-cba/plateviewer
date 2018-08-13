package de.embl.cba.plateviewer;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;

public class MolDevCellImgLoader implements CellLoader
{
	final long cellWidth;
	final long cellHeight;

	public MolDevCellImgLoader( long cellWidth, long cellHeight )
	{
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
	}

	@Override
	public void load( final SingleCellArrayImg cell ) throws Exception
	{
		long xPos = cell.min( 0 );
		long yPos = cell.min( 1 );

		String file = "/Users/tischer/Documents/andrea-callegari-stitching--data/MolDev/2018-08-10-raw-test--processed/180730-Nup93-mEGFP-clone79-imaging-pipeline_H02_w2.tif";
		final ImagePlus imp = IJ.openImage( file );
		final byte[] impdata = ( byte[] ) imp.getProcessor().getPixels();
		final byte[] celldata = ( byte[] ) cell.getStorageArray();
		System.arraycopy( impdata, 0, celldata, 0, celldata.length );
	}

}
