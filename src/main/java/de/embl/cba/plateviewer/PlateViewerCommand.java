package de.embl.cba.plateviewer;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvOverlay;
import de.embl.cba.plateviewer.bdv.BdvSiteAndWellNamesOverlay;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, menuPath = "Plugins>Screening>Plate viewer" )
public class PlateViewerCommand implements Command
{
	@Parameter
	public LogService logService;

	@Parameter
	public CommandService commandService;

	@Parameter (label = "Input directory", style = "directory" )
	public File inputDirectory;

	@Parameter (label = "Only load files matching" )
	public String filePattern = ".*.tif";

	PlateViewer plateViewer;

	public void run()
	{

		new PlateViewer( inputDirectory.toString(), filePattern, 1 );

		// addSiteNamesOverlay();

		// TODO: wellNames Overlay
	}




}

