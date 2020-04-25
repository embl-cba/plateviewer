import de.embl.cba.plateviewer.PlateViewer;
import net.imagej.ImageJ;

import java.io.File;

public class TestNamingSchemeBatchLibHdf5
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

//		new PlateViewer(
//				new File( TestNamingSchemeCorona.class.getResource( "BATCHLIBHDF5" ).getFile() ),
//				".*.h5",
//				new File( TestNamingSchemeCorona.class.getResource( "BATCHLIBHDF5/default.csv" ).getFile() ),
//				1, false);

		final PlateViewer plateViewer = new PlateViewer(
				new File( "/Users/tischer/Documents/mnt/hci/data-processed-multiscale/20200415_150710_683" ),
				".*.h5",
				true,
				1,
				false );
	}

}
