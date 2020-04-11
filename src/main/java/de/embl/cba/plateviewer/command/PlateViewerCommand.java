package de.embl.cba.plateviewer.command;

import bdv.viewer.ViewerPanel;
import de.embl.cba.plateviewer.view.PlateViewerImageView;
import de.embl.cba.plateviewer.table.DefaultImageNameTableRow;
import de.embl.cba.plateviewer.view.PlateViewerTableView;
import de.embl.cba.tables.view.TableRowsTableView;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.awt.*;
import java.io.File;
import java.util.List;

import static de.embl.cba.plateviewer.table.ImageNameTableRows.imageNameTableRowsFromFilePath;

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
		final PlateViewerImageView imageView = new PlateViewerImageView( imagesDirectory.toString(), filePattern, 1 );

		if ( imagesTableFile != null )
		{
			final List< DefaultImageNameTableRow > tableRows = imageNameTableRowsFromFilePath( imagesTableFile.getAbsolutePath() );
			final TableRowsTableView< DefaultImageNameTableRow > tableView = new TableRowsTableView<>( tableRows );
			final Component parentComponent = imageView.getBdv().getBdvHandle().getViewerPanel();
			tableView.showTableAndMenu( parentComponent );
		}
	}
}

