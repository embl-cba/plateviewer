package command;

import de.embl.cba.plateviewer.ui.PlateViewerCommand;
import net.imagej.ImageJ;

public class RunPlateViewerCommand
{
	public static void main(final String... args)
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// invoke the plugin
		ij.command().run( PlateViewerCommand.class, true );

		// test data
		// /Volumes/almfscreen/Gbekor/ATAT1/Nikon/PlateATAT1_pilot_4000_cells
		// /Users/tischer/Documents/fiji-plugin-plateViewer/src/test/resources/Eugene
		// /Volumes/cba/exchange/Andrea Callegari/non-renamed files/
	}
}
