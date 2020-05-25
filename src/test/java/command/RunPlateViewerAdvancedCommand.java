package command;

import de.embl.cba.plateviewer.PlateViewerAdvancedCommand;
import de.embl.cba.plateviewer.PlateViewerCommand;
import net.imagej.ImageJ;

public class RunPlateViewerAdvancedCommand
{
	public static void main(final String... args)
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		ij.command().run( PlateViewerAdvancedCommand.class, true );
	}
}
