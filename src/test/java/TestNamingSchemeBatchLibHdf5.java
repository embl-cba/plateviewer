import de.embl.cba.plateviewer.PlateViewer;
import net.imagej.ImageJ;

import java.io.File;

public class TestNamingSchemeBatchLibHdf5
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		String pathname;
		pathname = "/Users/tischer/Documents/mnt/hci/data-processed/plateK14rep1_20200430_194338_941_IgG";

		pathname ="/g/kreshuk/data/covid/data-processed/20200417_132123_311"; // publication

//		pathname ="/g/kreshuk/data/covid/data-processed/20200417_152052_943"; // publication


		final PlateViewer plateViewer = new PlateViewer(
				new File( pathname ),
				".*.h5",
				true,
				true,
				1,
				false );
	}

}
