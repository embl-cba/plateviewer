package command;

import de.embl.cba.plateviewer.PlateViewerCommand;
import net.imagej.ImageJ;

public class RunPlateViewerCommand
{
	public static void main(final String... args)
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		ij.command().run( PlateViewerCommand.class, true );
	}
}
