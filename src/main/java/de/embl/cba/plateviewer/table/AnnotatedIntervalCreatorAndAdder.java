package de.embl.cba.plateviewer.table;

import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.image.table.TableRowsIntervalImage;
import de.embl.cba.plateviewer.mongodb.AssayMetadataRepository;
import de.embl.cba.plateviewer.PlateViewer;
import de.embl.cba.plateviewer.util.Utils;
import de.embl.cba.tables.color.ColoringLuts;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.NumericColoringModelDialog;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.view.TableRowsTableView;
import net.imglib2.Interval;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.embl.cba.plateviewer.table.Tables.createAnnotatedIntervalTableRowsFromFileAndRepository;

public class AnnotatedIntervalCreatorAndAdder < T extends AnnotatedIntervalTableRow >
{
	private final PlateViewer< ?, T > imageView;
	private final String namingScheme;
	private final TableSource tableSource;
	private final AssayMetadataRepository repository;
	private DefaultSelectionModel< T > selectionModel;
	private LazyCategoryColoringModel< T > coloringModel;
	private SelectionColoringModel< T > selectionColoringModel;
	private List< T > tableRows;

	public AnnotatedIntervalCreatorAndAdder(
			PlateViewer imageView,
			String namingScheme,
			TableSource tableSource )
	{
		this.imageView = imageView;
		this.namingScheme = namingScheme;
		this.tableSource = tableSource;
		this.repository = null;
	}

	public AnnotatedIntervalCreatorAndAdder(
			PlateViewer imageView,
			String namingScheme,
			TableSource tableSource,
			AssayMetadataRepository repository )
	{
		this.imageView = imageView;
		this.namingScheme = namingScheme;
		this.tableSource = tableSource;
		this.repository = repository;
	}

	public void createAndAddAnnotatedIntervals()
	{
		Map< String, Interval > nameToInterval = getNameToInterval( tableSource.intervalType );

		if ( repository != null )
			repository.setIntervalType( tableSource.intervalType );

		tableRows = ( List< T > ) createAnnotatedIntervalTableRowsFromFileAndRepository(
				tableSource,
				namingScheme,
				nameToInterval,
				repository // optional
		);

		selectionModel = new DefaultSelectionModel<>();// TODO: below code only uses the repository it is not null. clean this up to make it more obvious
		coloringModel = new LazyCategoryColoringModel<>( new GlasbeyARGBLut( 255 ) );
		selectionColoringModel = new SelectionColoringModel( coloringModel, selectionModel );

		if ( tableSource.intervalType.equals( IntervalType.Sites ) )
			imageView.addAnnotatedSiteIntervals( tableRows, selectionModel, selectionColoringModel );
		else if ( tableSource.intervalType.equals( IntervalType.Wells ) )
			imageView.addAnnotatedWellIntervals( tableRows, selectionModel, selectionColoringModel );

		final TableRowsTableView< T > tableView
				= createTableView( imageView.getBdvHandle().getViewerPanel() );

		final TableRowsIntervalImage tableRowsIntervalImage =
				new TableRowsIntervalImage(
						tableRows,
						selectionColoringModel,
						tableView,
						imageView.getPlateInterval(),
						tableSource.intervalType.toString().toLowerCase() + " table values" );

		imageView.addToPanelAndBdv( tableRowsIntervalImage );

		final TableRowsScatterPlot< DefaultAnnotatedIntervalTableRow > scatterPlotView =
				new TableRowsScatterPlot(
						tableRows,
						selectionColoringModel,
						new String[]{ NamingSchemes.getDefaultColumnNameX( tableRows ),
						NamingSchemes.getDefaultColumnNameY( tableRows ) },
						new double[]{1,1},
						Utils.bdvTextOverlayFontSize );

		scatterPlotView.show( imageView.getBdvHandle().getViewerPanel() );

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
