package de.embl.cba.plateviewer;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;

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

	@Parameter (label = "Load well table")
	public boolean loadWellTable;

	@Parameter (label = "Connect to database")
	public boolean connectToDatabase;

	public void run()
	{
		final PlateViewerInitializer plateViewerInitializer = new PlateViewerInitializer( imagesDirectory, filePattern, loadImageTable, loadWellTable, connectToDatabase, 4, includeSubFolders );
		plateViewerInitializer.run();
	}

}

