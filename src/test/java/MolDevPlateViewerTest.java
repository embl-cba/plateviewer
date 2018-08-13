import bdv.util.Bdv;
import de.embl.cba.plateviewer.PlateImgLoader;
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

		String file = "/Users/tischer/Documents/andrea-callegari-stitching--data/MolDev/2018-08-10-raw-test--processed/“";

		final ImagePlus imp = IJ.openImage( file );

		// assuming we know it is a 3D, 16-bit stack...
		final long[] dimensions = new long[]{
				imp.getWidth() * 12,
				imp.getHeight() * 8
		};

		// set up cell size such that one cell is one plane
		final int[] cellDimensions = new int[]{
				imp.getWidth(),
				imp.getHeight(),
		};

		final PlateImgLoader molDevLoader = new PlateImgLoader( imp.getWidth(), imp.getHeight(), imp.getBitDepth(), cellFileMap );

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
