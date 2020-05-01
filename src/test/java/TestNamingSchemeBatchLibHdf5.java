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
		pathname = "/Users/tischer/Documents/mnt/hci/data-processed-multiscale/plate7rep1_20200426_103425_693_IgA";
		pathname ="/g/kreshuk/data/covid/sandbox/h5-tables2/titration_plate_20200403_154849";

		final PlateViewer plateViewer = new PlateViewer(
				new File( pathname ),
				".*.h5",
				true,
				1,
				false );
	}

}
