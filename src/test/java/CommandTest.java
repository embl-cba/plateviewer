import de.embl.cba.multipositionviewer.MultiPositionViewerCommand;
import net.imagej.ImageJ;

public class CommandTest
{

	public static void main(final String... args) throws Exception
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// invoke the plugin
		ij.command().run( MultiPositionViewerCommand.class, true );
	}

}
