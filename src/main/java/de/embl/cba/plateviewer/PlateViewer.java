package de.embl.cba.plateviewer;

import bdv.util.Bdv;
import de.embl.cba.bdv.utils.Logger;
import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.image.table.TableImage;
import de.embl.cba.plateviewer.table.DefaultSiteNameTableRow;
import de.embl.cba.plateviewer.table.SiteName;
import de.embl.cba.plateviewer.view.PlateViewerImageView;
import de.embl.cba.tables.TableColumns;
import de.embl.cba.tables.Tables;
import de.embl.cba.tables.color.ColoringLuts;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.view.TableRowsTableView;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

import static de.embl.cba.plateviewer.table.ImageNameTableRows.createSiteNameTableRowsFromFilePath;

public class PlateViewer < R extends NativeType< R > & RealType< R >, T extends SiteName >
{
	private final Bdv bdv;

	public PlateViewer( File imagesDirectory, String filePattern, File imagesTableFile, int numIoThreads, boolean includeSubFolders )
	{
		final PlateViewerImageView< R, T > imagePlateView =
				new PlateViewerImageView( imagesDirectory.toString(), filePattern, numIoThreads, includeSubFolders );

		bdv = imagePlateView.getBdv();

		if ( imagesTableFile != null )
		{
			showTable( imagesTableFile, imagePlateView );
		}
	}

	public void showTable( File imagesTableFile, PlateViewerImageView imageView )
	{
		final DefaultSelectionModel< DefaultSiteNameTableRow > selectionModel = new DefaultSelectionModel<>();

		final LazyCategoryColoringModel< DefaultSiteNameTableRow > coloringModel = new LazyCategoryColoringModel<>( new GlasbeyARGBLut( 255 ) );

		final SelectionColoringModel< DefaultSiteNameTableRow > selectionColoringModel = new SelectionColoringModel(
				coloringModel,
				selectionModel );

		final List< DefaultSiteNameTableRow > tableRows = createSiteNameTableRowsFromFilePath(
				imagesTableFile.getAbsolutePath(),
				imageView.getFileNamingScheme() );

		final TableRowsTableView< DefaultSiteNameTableRow > tableView =
				new TableRowsTableView<>( tableRows, selectionModel, selectionColoringModel );

		final Component parentComponent = imageView.getBdv().getBdvHandle().getViewerPanel();
		tableView.showTableAndMenu( parentComponent );
		tableView.setSelectionMode( TableRowsTableView.SelectionMode.FocusOnly );
		imageView.installImageSelectionModel( tableRows, selectionModel );
		imageView.registerAsColoringListener( selectionColoringModel );

		final TableImage tableImage = new TableImage( tableRows, selectionColoringModel, imageView );

		imageView.registerTableView( tableView );
		imageView.addToBdvAndPanel( tableImage );

		if ( imageView.getFileNamingScheme().equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			try
			{
				tableView.colorByColumn( "score1", ColoringLuts.VIRIDIS );
			}
			catch ( Exception e )
			{
				Utils.log("[WARN] Default table column for coloring not found: " + "score1" );
			}
		}
	}

	public Bdv getBdv()
	{
		return bdv;
	}
}
