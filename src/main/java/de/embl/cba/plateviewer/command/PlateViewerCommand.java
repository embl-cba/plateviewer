package de.embl.cba.plateviewer.command;

import de.embl.cba.plateviewer.PlateViewer;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, menuPath = "Plugins>Screening>PlateViewer..." )
public class PlateViewerCommand implements Command
{
	@Parameter (label = "Plate images directory", style = "directory" )
	public File imagesDirectory;

	@Parameter (label = "Only load image files matching" )
	public String filePattern = ".*.tif";

	@Parameter (label = "Plate images table (optional)", required = false)
	public File imagesTableFile;

	public void run()
	{
		new PlateViewer( imagesDirectory, filePattern, imagesTableFile, 4 );
	}

}

