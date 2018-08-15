import de.embl.cba.plateviewer.CellFileMapsGenerator;
import de.embl.cba.plateviewer.CachedPlateViewImg;
import de.embl.cba.plateviewer.PlateView;
import ij.ImageJ;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;


public class PlateViewerTest
{

	// TODO
	// Naming schemes
	// - ALMF_SCHEME: /Volumes/almfscreen/Sabine/CrispRCoating/Experiment_2018_08_07/24h/20180807_CrispR_20xd1_Dapi
	// - SCAN_R: movies: /Volumes/almfscreen/Sabine/Temp_test_38gC_002/data


	public static void main(String[] args) throws Exception
	{
		ImageJ.main( args );

//		String directoryName = "/Users/tischer/Documents/andrea-callegari-stitching--data/MolDev/2018-08-10-raw-test--processed/";
//		String directoryName = "/Users/tischer/Documents/andrea-callegari-stitching--data/MolDev/2018-08-10-raw-test--processed--subset/";
		String directoryName = "/Users/tischer/Desktop/tmp3";

		String fileNameRegExp = ".*.tif";

		final CellFileMapsGenerator cellFileMapsGenerator = new CellFileMapsGenerator( directoryName, fileNameRegExp );
		final ArrayList< Map< String, File > > cellFileMaps = cellFileMapsGenerator.getCellFileMaps();
		final int[] siteDimensions = cellFileMapsGenerator.getSiteDimensions();
		final int[] wellDimensions = cellFileMapsGenerator.getWellDimensions();

		final ArrayList< CachedPlateViewImg > cachedCellImgs = new ArrayList<>( );

		PlateView plateView = null;

		for ( int channel = 0; channel < cellFileMaps.size(); ++channel )
		{
			final CachedPlateViewImg cachedPlateViewImg = new CachedPlateViewImg( cellFileMaps.get( channel ), wellDimensions, siteDimensions );

			if ( channel == 0 )
			{
				plateView = new PlateView( cachedPlateViewImg );
			}
			else
			{
				plateView.addChannel( cachedPlateViewImg );
			}
		}


	}

}
