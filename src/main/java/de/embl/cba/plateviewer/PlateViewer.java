package de.embl.cba.plateviewer;

import bdv.viewer.ViewerPanel;
import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.image.table.TableRowsSitesImage;
import de.embl.cba.plateviewer.plot.ScatterPlotOverlay;
import de.embl.cba.plateviewer.plot.TableRowsScatterPlotView;
import de.embl.cba.plateviewer.table.AnnotatedInterval;
import de.embl.cba.plateviewer.table.DefaultAnnotatedIntervalTableRow;
import de.embl.cba.plateviewer.view.ImagePlateViewer;
import de.embl.cba.tables.color.ColoringLuts;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.NumericColoringModelDialog;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.view.TableRowsTableView;
import ij.IJ;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.embl.cba.plateviewer.table.Tables.createSiteTableRowsFromFile;

public class PlateViewer < R extends NativeType< R > & RealType< R >, T extends AnnotatedInterval >
{
	private final File imagesDirectory;
	private DefaultSelectionModel< DefaultAnnotatedIntervalTableRow > selectionModel;
	private LazyCategoryColoringModel< DefaultAnnotatedIntervalTableRow > coloringModel;
	private SelectionColoringModel< DefaultAnnotatedIntervalTableRow > selectionColoringModel;
	private List< DefaultAnnotatedIntervalTableRow > siteTableRows;

	public PlateViewer( File imagesDirectory, String filePattern, boolean loadSiteTable, int numIoThreads, boolean includeSubFolders )
	{
		this.imagesDirectory = imagesDirectory;

		final ImagePlateViewer< R, DefaultAnnotatedIntervalTableRow > imageView =
				new ImagePlateViewer(
						imagesDirectory.toString(),
						filePattern,
						numIoThreads,
						includeSubFolders );

		if ( loadSiteTable )
		{
			addSiteTable( imageView );
		}
	}

	public void addSiteTable( ImagePlateViewer< R, DefaultAnnotatedIntervalTableRow > imageView )
	{
		final String fileNamingScheme = imageView.getFileNamingScheme();
		final ViewerPanel bdvViewerPanel = imageView.getBdvHandle().getViewerPanel();

		createSiteTableRows( fileNamingScheme, imageView.getSiteNameToInterval() );

		initColoringAndSelectionModels();

		imageView.addSiteImageIntervals( siteTableRows, selectionModel );
		imageView.registerAsColoringListener( selectionColoringModel );

		final TableRowsTableView< DefaultAnnotatedIntervalTableRow > tableView = createTableView( bdvViewerPanel );

		imageView.registerTableView( tableView );

		final TableRowsSitesImage tableRowsSitesImage = createTableColoredSiteImage(
					imageView,
					fileNamingScheme,
					tableView );

		imageView.addToPanelAndBdv( tableRowsSitesImage );

		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			final TableRowsScatterPlotView< DefaultAnnotatedIntervalTableRow > scatterPlotView =
					new TableRowsScatterPlotView(
							siteTableRows,
							"sites scatter plot",
							selectionColoringModel,
							selectionModel,
							imageView.getPlateName(),
							NamingSchemes.ColumnNamesBatchLibHdf5.getDefaultColumnNameX( siteTableRows ),
							NamingSchemes.ColumnNamesBatchLibHdf5.getDefaultColumnNameY(),
							NamingSchemes.ColumnNamesBatchLibHdf5.COLUMN_NAME_OUTLIER,
							ScatterPlotOverlay.Y_NX );

			scatterPlotView.show( bdvViewerPanel );
		}
	}

	public TableRowsSitesImage createTableColoredSiteImage(
			ImagePlateViewer imageView,
			String fileNamingScheme,
			TableRowsTableView< DefaultAnnotatedIntervalTableRow > tableView )
	{
		final TableRowsSitesImage tableRowsSitesImage =
				new TableRowsSitesImage(
						siteTableRows,
						selectionColoringModel,
						imageView );

		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			NumericColoringModelDialog.dialogLocation = new Point( 10, imageView.getMainPanel().getLocationOnScreen().y + imageView.getMainPanel().getHeight() + 80 );

			final Set< String > columnNames = tableView.getTableRows().get( 0 ).getColumnNames();
			if ( columnNames.contains( "cell_based_score" ) )
				tableView.colorByColumn( "cell_based_score", ColoringLuts.VIRIDIS );
			else if ( columnNames.contains( "score1" ) )
				tableView.colorByColumn( "score1", ColoringLuts.VIRIDIS );
			else if ( columnNames.contains( "score" ) )
				tableView.colorByColumn( "score", ColoringLuts.VIRIDIS );

		}

		return tableRowsSitesImage;
	}

	public TableRowsTableView< DefaultAnnotatedIntervalTableRow > createTableView( Component component )
	{
		final TableRowsTableView< DefaultAnnotatedIntervalTableRow > tableView =
				new TableRowsTableView<>( siteTableRows, selectionModel, selectionColoringModel );
		tableView.setSelectionMode( TableRowsTableView.SelectionMode.FocusOnly );
		tableView.showTableAndMenu( component );
		return tableView;
	}

	public void initColoringAndSelectionModels()
	{
		selectionModel = new DefaultSelectionModel<>();

		coloringModel = new LazyCategoryColoringModel<>( new GlasbeyARGBLut( 255 ) );

		selectionColoringModel = new SelectionColoringModel(
				coloringModel,
				selectionModel );
	}

	public void createSiteTableRows( String fileNamingScheme, Map< String, Interval > siteNameToInterval )
	{
		File tableFile = getTableFile( fileNamingScheme );

		siteTableRows = createSiteTableRowsFromFile(
						tableFile.getAbsolutePath(),
						fileNamingScheme,
						siteNameToInterval);
	}

	public File getTableFile( String fileNamingScheme )
	{
		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			final String plateName = imagesDirectory.getName();
			File tableFile;

			// try all the different conventions
			//
			tableFile = new File( imagesDirectory, plateName + "_table_serum_IgG_corrected.hdf5" );
			if ( tableFile.exists() ) return tableFile;

			tableFile = new File( imagesDirectory, plateName + "_analysis.csv" );
			if ( tableFile.exists() ) return tableFile;

			tableFile = new File( imagesDirectory, "analysis.csv" );
			if ( tableFile.exists() ) return tableFile;

			tableFile = new File( imagesDirectory, plateName + "_table_serum_corrected.hdf5" );
			if ( tableFile.exists() ) return tableFile;

			// nothing worked => ask user
			//
			final String tableFilePath = IJ.getFilePath( "Please select table file" );
			if ( tableFilePath != null )
				return new File( tableFilePath );
			else
				return null;
		}
		else
		{
			throw new UnsupportedOperationException( "Cannot yet load tables for naming scheme: " + fileNamingScheme );
		}

	}
}
