package de.embl.cba.plateviewer.table;

import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.image.table.TableRowsIntervalImage;
import de.embl.cba.plateviewer.plot.ScatterPlotOverlay;
import de.embl.cba.plateviewer.plot.TableRowsScatterPlotView;
import de.embl.cba.plateviewer.view.ImagePlateViewer;
import de.embl.cba.tables.color.ColoringLuts;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.NumericColoringModelDialog;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.view.TableRowsTableView;
import net.imglib2.util.Intervals;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Set;

import static de.embl.cba.plateviewer.table.Tables.createAnnotatedIntervalTableRowsFromFile;

public class AnnotatedIntervalCreatorAndAdder
{
	public enum IntervalType
	{
		Sites,
		Wells
	}

	private final ImagePlateViewer< ?, DefaultAnnotatedIntervalTableRow > imageView;
	private final String fileNamingScheme;
	private final File tableFile;
	private DefaultSelectionModel< DefaultAnnotatedIntervalTableRow > selectionModel;
	private LazyCategoryColoringModel< DefaultAnnotatedIntervalTableRow > coloringModel;
	private SelectionColoringModel< DefaultAnnotatedIntervalTableRow > selectionColoringModel;
	private List< DefaultAnnotatedIntervalTableRow > tableRows;

	public AnnotatedIntervalCreatorAndAdder(
			ImagePlateViewer< ?, DefaultAnnotatedIntervalTableRow > imageView,
			String fileNamingScheme,
			File tableFile )
	{
		this.imageView = imageView;
		this.fileNamingScheme = fileNamingScheme;
		this.tableFile = tableFile;
	}

	public void createAndAddAnnotatedIntervals( IntervalType intervalType, String hdf5Group )
	{
		tableRows = createAnnotatedIntervalTableRowsFromFile(
					tableFile.getAbsolutePath(),
					fileNamingScheme,
					imageView.getSiteNameToInterval(),
					hdf5Group );

		selectionModel = new DefaultSelectionModel<>();
		coloringModel = new LazyCategoryColoringModel<>( new GlasbeyARGBLut( 255 ) );
		selectionColoringModel = new SelectionColoringModel( coloringModel, selectionModel );

		if ( intervalType.equals( IntervalType.Sites ) )
			imageView.addAnnotatedSiteIntervals( tableRows, selectionModel, selectionColoringModel );
		else if ( intervalType.equals( IntervalType.Wells ) )
			imageView.addAnnotatedWellIntervals( tableRows, selectionModel, selectionColoringModel );

		final TableRowsTableView< DefaultAnnotatedIntervalTableRow > tableView
				= createTableView( imageView.getBdvHandle().getViewerPanel() );

		imageView.registerTableView( tableView );

		final TableRowsIntervalImage tableRowsIntervalImage =
				new TableRowsIntervalImage(
						tableRows,
						selectionColoringModel,
						tableView,
						imageView.getPlateInterval(),
						Intervals.dimensionsAsLongArray( tableRows.get( 0 ).getInterval() ) );

		imageView.addToPanelAndBdv( tableRowsIntervalImage );

		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			final TableRowsScatterPlotView< DefaultAnnotatedIntervalTableRow > scatterPlotView =
					new TableRowsScatterPlotView(
							tableRows,
							hdf5Group,
							selectionColoringModel,
							selectionModel,
							imageView.getPlateName(),
							NamingSchemes.ColumnNamesBatchLibHdf5.getDefaultColumnNameX( tableRows ),
							NamingSchemes.ColumnNamesBatchLibHdf5.getDefaultColumnNameY(),
							ScatterPlotOverlay.Y_NX );

			scatterPlotView.show( imageView.getBdvHandle().getViewerPanel() );
		}


		colorByDefaultColumn( tableView );
	}

	private void colorByDefaultColumn( TableRowsTableView< DefaultAnnotatedIntervalTableRow > tableView )
	{
		NumericColoringModelDialog.dialogLocation = new Point( 10, imageView.getMainPanel().getLocationOnScreen().y + imageView.getMainPanel().getHeight() + 80 );

		final Set< String > columnNames = tableView.getTableRows().get( 0 ).getColumnNames();
		if ( columnNames.contains( "ratio_of_median_of_sums" ) )
			tableView.colorByColumn( "ratio_of_median_of_sums", ColoringLuts.VIRIDIS );
		if ( columnNames.contains( "ratio_of_q0.5_of_sums" ) )
			tableView.colorByColumn( "ratio_of_q0.5_of_sums", ColoringLuts.VIRIDIS );
		else if ( columnNames.contains( "cell_based_score" ) )
			tableView.colorByColumn( "cell_based_score", ColoringLuts.VIRIDIS );
		else if ( columnNames.contains( "score1" ) )
			tableView.colorByColumn( "score1", ColoringLuts.VIRIDIS );
		else if ( columnNames.contains( "score" ) )
			tableView.colorByColumn( "score", ColoringLuts.VIRIDIS );
	}

	public TableRowsTableView< DefaultAnnotatedIntervalTableRow > createTableView( Component component )
	{
		final TableRowsTableView< DefaultAnnotatedIntervalTableRow > tableView =
				new TableRowsTableView<>( tableRows, selectionModel, selectionColoringModel );
		tableView.setSelectionMode( TableRowsTableView.SelectionMode.FocusOnly );
		tableView.showTableAndMenu( component );
		return tableView;
	}
}
