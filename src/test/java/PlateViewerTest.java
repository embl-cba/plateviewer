import de.embl.cba.plateviewer.CellFileMaps;
import de.embl.cba.plateviewer.CachedCellImgs;
import de.embl.cba.plateviewer.PlateView;
import de.embl.cba.plateviewer.Utils;
import ij.ImageJ;
import net.imglib2.img.Img;

import java.io.File;
import java.util.Map;


public class PlateViewerTest
{

	public static void main(String[] args) throws Exception
	{

		ImageJ.main( args );

		String directoryName = "/Users/tischer/Documents/andrea-callegari-stitching--data/MolDev/2018-08-10-raw-test--processed/";

		final Map< String, File > cellFileMap = CellFileMaps.create( Utils.WELL_PLATE_96, directoryName );

		final CachedCellImgs cachedCellImgs = new CachedCellImgs( cellFileMap, Utils.WELL_PLATE_96 );
		final Img img = cachedCellImgs.create( );
		final int[] cellDimensions = cachedCellImgs.getCellDimensions();

		final PlateView plateView = new PlateView( img, cellDimensions, cellFileMap );
		plateView.show();


	}

}