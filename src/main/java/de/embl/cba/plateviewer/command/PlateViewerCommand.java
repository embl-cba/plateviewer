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

	@Parameter (label = "Include sub-folders")
	public boolean includeSubFolders = false;

	@Parameter (label = "Only load image files matching" )
	public String filePattern = ".*.h5";

	@Parameter (label = "Load image table")
	public boolean loadImageTable;

	public void run()
	{
		new PlateViewer( imagesDirectory, filePattern, loadImageTable, 4, includeSubFolders );
	}

}

