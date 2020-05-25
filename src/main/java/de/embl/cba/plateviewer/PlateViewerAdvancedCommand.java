package de.embl.cba.plateviewer;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, menuPath = "Plugins>Screening>PlateViewer Advanced..." )
public class PlateViewerAdvancedCommand extends PlateViewerCommand
{
	@Parameter (label = "Additional images directory", style = "directory" )
	public File additionalImagesDirectory;

	public void run()
	{
		final PlateViewerInitializer plateViewerInitializer = new PlateViewerInitializer( imagesDirectory, filePattern, loadImageTable, loadWellTable, connectToDatabase, 4, includeSubFolders );
		plateViewerInitializer.addInputImagesDirectory( additionalImagesDirectory.getAbsolutePath() );
		plateViewerInitializer.run();
	}

}

