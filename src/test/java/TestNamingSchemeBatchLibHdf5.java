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
//				new File( TestNamingSchemeBatchLibHdf5.class.getResource( "BATCHLIBHDF5" ).getFile() ),
//				".*.h5",
//				false,
//				1, false);

		final PlateViewer plateViewer = new PlateViewer(
				new File( "/Users/tischer/Documents/mnt/hci/data-processed-multiscale/20200420_164920_764_IgA" ),
				".*.h5",
				true,
				1,
				false );

//
	}

}
