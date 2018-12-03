import de.embl.cba.gridviewer.viewer.PlateViewerCommand;
import net.imagej.ImageJ;

public class PlateViewerCommandTest
{

	public static void main(final String... args) throws Exception
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// invoke the plugin
		ij.command().run( PlateViewerCommand.class, true );

		// test data
		// /Volumes/almfscreen/Gbekor/ATAT1/Nikon/PlateATAT1_pilot_4000_cells
		// /Users/tischer/Documents/fiji-plugin-plateViewer/src/test/resources/Eugene
	}

}
