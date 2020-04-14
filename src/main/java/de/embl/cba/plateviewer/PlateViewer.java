package de.embl.cba.plateviewer;

import de.embl.cba.plateviewer.table.DefaultSiteNameTableRow;
import de.embl.cba.plateviewer.view.PlateViewerImageView;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.view.TableRowsTableView;

import java.awt.*;
import java.io.File;
import java.util.List;

import static de.embl.cba.plateviewer.table.ImageNameTableRows.createSiteNameTableRowsFromFilePath;

public class PlateViewer
{
	public PlateViewer( File imagesDirectory, String filePattern, File imagesTableFile, int numIoThreads )
	{
		final PlateViewerImageView imagePlateView = new PlateViewerImageView( imagesDirectory.toString(), filePattern, numIoThreads );

		if ( imagesTableFile != null )
		{
			final DefaultSelectionModel< DefaultSiteNameTableRow > selectionModel = new DefaultSelectionModel<>();

			final List< DefaultSiteNameTableRow > tableRows = createSiteNameTableRowsFromFilePath(
					imagesTableFile.getAbsolutePath(),
					imagePlateView.getFileNamingScheme() );

			final TableRowsTableView< DefaultSiteNameTableRow > imageTableView = new TableRowsTableView<>( tableRows, selectionModel );
			final Component parentComponent = imagePlateView.getBdv().getBdvHandle().getViewerPanel();
			imageTableView.showTableAndMenu( parentComponent );
			imageTableView.setSelectionMode( TableRowsTableView.SelectionMode.FocusOnly );
			imagePlateView.installImageSelectionModel( tableRows, selectionModel );
		}
	}
}
