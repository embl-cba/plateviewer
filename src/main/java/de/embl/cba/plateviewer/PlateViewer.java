package de.embl.cba.plateviewer;

import bdv.util.Bdv;
import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.plateviewer.image.table.TableImage;
import de.embl.cba.plateviewer.table.DefaultSiteNameTableRow;
import de.embl.cba.plateviewer.table.SiteName;
import de.embl.cba.plateviewer.view.PlateViewerImageView;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.view.TableRowsTableView;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.awt.*;
import java.io.File;
import java.util.List;

import static de.embl.cba.plateviewer.table.ImageNameTableRows.createSiteNameTableRowsFromFilePath;

public class PlateViewer < R extends NativeType< R > & RealType< R >, T extends SiteName >
{
	private final Bdv bdv;

	public PlateViewer( File imagesDirectory, String filePattern, File imagesTableFile, int numIoThreads )
	{
		final PlateViewerImageView< R, T > imagePlateView = new PlateViewerImageView( imagesDirectory.toString(), filePattern, numIoThreads );

		bdv = imagePlateView.getBdv();

		if ( imagesTableFile != null )
		{
			showTable( imagesTableFile, imagePlateView );
		}
	}

	public void showTable( File imagesTableFile, PlateViewerImageView plateViewerImageView )
	{
		final DefaultSelectionModel< DefaultSiteNameTableRow > selectionModel = new DefaultSelectionModel<>();

		final LazyCategoryColoringModel< DefaultSiteNameTableRow > coloringModel = new LazyCategoryColoringModel<>( new GlasbeyARGBLut( 255 ) );

		final SelectionColoringModel< DefaultSiteNameTableRow > selectionColoringModel = new SelectionColoringModel(
				coloringModel,
				selectionModel );

		final List< DefaultSiteNameTableRow > tableRows = createSiteNameTableRowsFromFilePath(
				imagesTableFile.getAbsolutePath(),
				plateViewerImageView.getFileNamingScheme() );

		final TableRowsTableView< DefaultSiteNameTableRow > imageTableView =
				new TableRowsTableView<>( tableRows, selectionModel, selectionColoringModel );

		final Component parentComponent = plateViewerImageView.getBdv().getBdvHandle().getViewerPanel();
		imageTableView.showTableAndMenu( parentComponent );
		imageTableView.setSelectionMode( TableRowsTableView.SelectionMode.FocusOnly );
		plateViewerImageView.installImageSelectionModel( tableRows, selectionModel );

		final TableImage tableImage = new TableImage( tableRows, coloringModel, plateViewerImageView );
		tableImage.createImage( "score1" );

		plateViewerImageView.addToBdvAndPanel( tableImage );
	}

	public Bdv getBdv()
	{
		return bdv;
	}
}
