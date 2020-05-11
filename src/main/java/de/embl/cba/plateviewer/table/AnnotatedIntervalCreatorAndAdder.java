package de.embl.cba.plateviewer.table;

import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.image.table.TableRowsIntervalImage;
import de.embl.cba.plateviewer.mongo.AssayMetadataRepository;
import de.embl.cba.plateviewer.plot.ScatterPlotGridLinesOverlay;
import de.embl.cba.plateviewer.plot.TableRowsScatterPlotView;
import de.embl.cba.plateviewer.view.ImagePlateViewer;
import de.embl.cba.tables.color.ColoringLuts;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.NumericColoringModelDialog;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.view.TableRowsTableView;
import net.imglib2.Interval;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.embl.cba.plateviewer.table.Tables.createAnnotatedIntervalTableRowsFromFileAndRepository;

public class AnnotatedIntervalCreatorAndAdder < T extends AnnotatedIntervalTableRow >
{
	public enum IntervalType
	{
		Sites,
		Wells
	}

	private final ImagePlateViewer< ?, T > imageView;
	private final String fileNamingScheme;
	private final File tableFile;
	private final AssayMetadataRepository repository;
	private DefaultSelectionModel< T > selectionModel;
	private LazyCategoryColoringModel< T > coloringModel;
	private SelectionColoringModel< T > selectionColoringModel;
	private List< T > tableRows;

	public AnnotatedIntervalCreatorAndAdder(
			ImagePlateViewer< ?, T > imageView,
			String fileNamingScheme,
			File tableFile )
	{
		this.imageView = imageView;
		this.fileNamingScheme = fileNamingScheme;
		this.tableFile = tableFile;
		this.repository = null;
	}

	public AnnotatedIntervalCreatorAndAdder(
			ImagePlateViewer< ?, T > imageView,
			String fileNamingScheme,
			File tableFile,
			AssayMetadataRepository repository )
	{
		this.imageView = imageView;
		this.fileNamingScheme = fileNamingScheme;
		this.tableFile = tableFile;
		this.repository = repository;
	}

	public void createAndAddAnnotatedIntervals( IntervalType intervalType, String hdf5Group )
	{
		Map< String, Interval > nameToInterval = getNameToInterval( intervalType );

		if ( repository != null )
			repository.setIntervalType( intervalType );

		// TODO: below code only uses the repository it is not null. clean this up to make it more obvious
		tableRows = ( List< T > ) createAnnotatedIntervalTableRowsFromFileAndRepository(
				tableFile.getAbsolutePath(),
				fileNamingScheme,
				nameToInterval,
				hdf5Group,
				repository );

		selectionModel = new DefaultSelectionModel<>();
		coloringModel = new LazyCategoryColoringModel<>( new GlasbeyARGBLut( 255 ) );
		selectionColoringModel = new SelectionColoringModel( coloringModel, selectionModel );

		if ( intervalType.equals( IntervalType.Sites ) )
			imageView.addAnnotatedSiteIntervals( tableRows, selectionModel, selectionColoringModel );
		else if ( intervalType.equals( IntervalType.Wells ) )
			imageView.addAnnotatedWellIntervals( tableRows, selectionModel, selectionColoringModel );

		final TableRowsTableView< T > tableView
				= createTableView( imageView.getBdvHandle().getViewerPanel() );

		final TableRowsIntervalImage tableRowsIntervalImage =
				new TableRowsIntervalImage(
						tableRows,
						selectionColoringModel,
						tableView,
						imageView.getPlateInterval(),
						intervalType.toString().toLowerCase() + " table values" );

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
							NamingSchemes.BatchLibHdf5.getDefaultColumnNameX( tableRows ),
							NamingSchemes.BatchLibHdf5.getDefaultColumnNameY( tableRows ),
							ScatterPlotGridLinesOverlay.Y_N );

			scatterPlotView.show( imageView.getBdvHandle().getViewerPanel() );
		}

		colorByDefaultColumn( tableView );
	}

	private Map< String, Interval > getNameToInterval( IntervalType intervalType )
	{
		Map< String, Interval > nameToInterval = null;
		if ( intervalType.equals( IntervalType.Sites ))
			nameToInterval= imageView.getSiteNameToInterval();
		else if ( intervalType.equals( IntervalType.Wells ))
			nameToInterval= imageView.getWellNameToInterval();
		return nameToInterval;
	}

	private void colorByDefaultColumn( TableRowsTableView< T > tableView )
	{
		NumericColoringModelDialog.dialogLocation = new Point( 10, imageView.getMainPanel().getLocationOnScreen().y + imageView.getMainPanel().getHeight() + 80 );

		final Set< String > columnNames = tableView.getTableRows().get( 0 ).getColumnNames();
		if ( columnNames.contains( "score" ) )
			tableView.colorByColumn( "score", ColoringLuts.VIRIDIS );
		else
			tableView.colorByColumn( new ArrayList<>( columnNames ).get( 0 ), ColoringLuts.VIRIDIS );
	}

	public TableRowsTableView< T > createTableView( Component component )
	{
		final TableRowsTableView< T > tableView =
				new TableRowsTableView<>( tableRows, selectionModel, selectionColoringModel );
		tableView.setSelectionMode( TableRowsTableView.SelectionMode.FocusOnly );
		tableView.showTableAndMenu( component );
		return tableView;
	}
}
