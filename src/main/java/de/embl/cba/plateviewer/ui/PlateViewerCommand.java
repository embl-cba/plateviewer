package de.embl.cba.plateviewer.ui;

import de.embl.cba.plateviewer.PlateViewer;
import de.embl.cba.plateviewer.table.JTableView;
import de.embl.cba.tables.Tables;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;
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
		if ( imagesTableFile != null )
		{
			final JTable jTable = Tables.loadTable( imagesTableFile.getAbsolutePath() );
			new JTableView( jTable ).showTable();
		}

		new PlateViewer( imagesDirectory.toString(), filePattern, 1 );
	}
}

