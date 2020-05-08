package users.daja;

import de.embl.cba.plateviewer.PlateViewerCommand;
import net.imagej.ImageJ;

import java.io.File;

public class RunPlateViewer384Fluo
{
	public static void main( String[] args )
	{

		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		final PlateViewerCommand command = new PlateViewerCommand();

		command.imagesDirectory = new File( "/Volumes/cuylen/01_Share/Filemaker/01_Experiments/0112/02_raw_data/renamed/Plate130918B_48h" );
		command.filePattern = ".*.tif";

		command.run();

	}

}
