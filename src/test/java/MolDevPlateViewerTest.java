import bdv.util.Bdv;
import de.embl.cba.plateviewer.MolDevCellImgLoader;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import bdv.util.BdvFunctions;


public class MolDevPlateViewerTest
{

	public static void main(String[] args) throws Exception
	{

		ImageJ.main( args );

		String file = "/Users/tischer/Documents/andrea-callegari-stitching--data/MolDev/2018-08-10-raw-test--processed/180730-Nup93-mEGFP-clone79-imaging-pipeline_H02_w2.tif";

		final ImagePlus imp = IJ.openImage( file );

		// assuming we know it is a 3D, 16-bit stack...
		final long[] dimensions = new long[]{
				imp.getWidth() * 2,
				imp.getHeight() * 2
		};

		// set up cell size such that one cell is one plane
		final int[] cellDimensions = new int[]{
				imp.getWidth(),
				imp.getHeight(),
		};

		final MolDevCellImgLoader molDevLoader = new MolDevCellImgLoader( imp.getWidth(), imp.getHeight() );

		// create a CellImg with that CellLoader
		final Img< UnsignedShortType > img = new ReadOnlyCachedCellImgFactory().create(
				dimensions,
				new UnsignedByteType(),
				molDevLoader,
				ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions ) );


		BdvFunctions.show(
				img,
				"test",
				Bdv.options().is2D() );


	}

}
