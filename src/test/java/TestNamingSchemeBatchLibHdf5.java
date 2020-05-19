import de.embl.cba.plateviewer.PlateViewer;
import net.imagej.ImageJ;

import java.io.File;

public class TestNamingSchemeBatchLibHdf5
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		/**
		 * H01 containing plates
		 *
		 * plates K17 to K21 + plate1rep3 + plate9_2rep1 + plate2rep3 + plate5rep3
		 */


		String pathname;

		pathname = "/Users/tischer/Documents/mnt/hci/data-processed/plateK14rep1_20200430_194338_941_IgG";



//		pathname ="/g/kreshuk/data/covid/data-processed/20200417_152052_943"; // publication

//		pathname = "/g/kreshuk/data/covid/sandbox/merged_table2/plateK12rep1_20200430_155932_313";

		pathname = "/g/kreshuk/data/covid/data-processed/plateK17rep1_20200505_115526_825"; // H1

		pathname = "/g/kreshuk/data/covid/data-processed/plateK18rep1_20200505_134507_072"; // H1

		pathname ="/g/kreshuk/data/covid/data-processed/PlateK21rep1_20200506_132517_049"; // H1

		pathname ="/g/kreshuk/data/covid/data-processed/PlateK19rep1_20200506_095722_264"; // H1

		pathname ="/g/kreshuk/data/covid/data-processed/plate1rep3_20200505_100837_821"; // H1

		//pathname = "/g/kreshuk/data/covid/data-processed/20200417_132123_311";

		//pathname = "/g/kreshuk/data/covid/data-processed/plate9_2rep1_20200506_163349_413";

		pathname = "/g/kreshuk/data/covid/sandbox/for_tischi/PlateK19rep1_20200506_095722_264";

		//pathname = "/g/kreshuk/data/covid/sandbox/for_tischi/plateK12rep1_20200430_155932_313";

		pathname = "/Users/tischer/Downloads/test_tischi";

		pathname ="/g/kreshuk/data/covid/data-processed/plate1rep3_20200505_100837_821";

		final PlateViewer plateViewer = new PlateViewer(
				new File( pathname ),
				".*.h5",
				true,
				true,
				true,
				1,
				false );

		plateViewer.run();
	}

}
