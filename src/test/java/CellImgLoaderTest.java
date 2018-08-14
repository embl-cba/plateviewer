
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

import net.imglib2.cache.img.*;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedShortType;

/**
 * Uses Bio-Formats to extract some basic standardized
 * (format-independent) plateviewer.
 */
public class CellImgLoaderTest
{

	public static void main(String[] args) throws Exception {

		ImageJ.main( args );

		String file = "/Users/tischer/Documents/andrea-callegari-stitching--data/MolDev/2018-08-10-raw-test--processed/180730-Nup93-mEGFP-clone79-imaging-pipeline_H02_w2.tif";

		final ImagePlus imp = IJ.openImage( file );

		// assuming we know it is a 3D, 16-bit stack...
		final long[] dimensions = new long[] {
				imp.getWidth() * 2 ,
				imp.getHeight() * 2
		};

		// set up cell size such that one cell is one plane
		final int[] cellDimensions = new int[] {
				imp.getWidth(),
				imp.getHeight(),
		};

		// make a CellLoader that copies one plane of data from the virtual stack
		final CellLoader< UnsignedShortType > loader = new CellLoader< UnsignedShortType >()
		{
			@Override
			public void load( final SingleCellArrayImg< UnsignedShortType, ? > cell ) throws Exception
			{
				final int z = ( int ) cell.min( 2 );
				final short[] impdata = ( short[] ) imp.getStack().getProcessor( 1 + z ).getPixels();
				final short[] celldata = ( short[] ) cell.getStorageArray();
				System.arraycopy( impdata, 0, celldata, 0, celldata.length );
			}
		};

		// create a CellImg with that CellLoader
		final CachedCellImg img = new ReadOnlyCachedCellImgFactory().create(
				dimensions,
				new UnsignedShortType(),
				loader,
				ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions ) );



	}

}