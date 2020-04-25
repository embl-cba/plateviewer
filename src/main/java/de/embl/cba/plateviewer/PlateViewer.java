package de.embl.cba.plateviewer;

import bdv.viewer.ViewerPanel;
import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.image.table.SitesImage;
import de.embl.cba.plateviewer.plot.TableRowsScatterPlotView;
import de.embl.cba.plateviewer.table.DefaultSiteNameTableRow;
import de.embl.cba.plateviewer.table.SiteName;
import de.embl.cba.plateviewer.view.ImagePlateViewer;
import de.embl.cba.tables.color.*;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.view.TableRowsTableView;
import ij.IJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Set;

import static de.embl.cba.plateviewer.table.ImageNameTableRows.createSiteNameTableRowsFromFilePath;

public class PlateViewer < R extends NativeType< R > & RealType< R >, T extends SiteName >
{
	private final File imagesDirectory;
	private final String filePattern;
	private final boolean loadImageTable;
	private final int numIoThreads;
	private final boolean includeSubFolders;
	private DefaultSelectionModel< DefaultSiteNameTableRow > selectionModel;
	private LazyCategoryColoringModel< DefaultSiteNameTableRow > coloringModel;
	private SelectionColoringModel< DefaultSiteNameTableRow > selectionColoringModel;
	private List< DefaultSiteNameTableRow > tableRows;

	public PlateViewer( File imagesDirectory, String filePattern, boolean loadImageTable, int numIoThreads, boolean includeSubFolders )
	{
		this.imagesDirectory = imagesDirectory;
		this.filePattern = filePattern;
		this.loadImageTable = loadImageTable;
		this.numIoThreads = numIoThreads;
		this.includeSubFolders = includeSubFolders;

		final ImagePlateViewer< R, T > imageView = new ImagePlateViewer( imagesDirectory.toString(), filePattern, numIoThreads, includeSubFolders );

		if ( loadImageTable )
		{
			showTable( imageView );
		}
	}

	public void showTable( ImagePlateViewer imageView )
	{
		final String fileNamingScheme = imageView.getFileNamingScheme();
		final ViewerPanel bdvViewerPanel = imageView.getBdvHandle().getViewerPanel();

		loadTable( fileNamingScheme );

		initColoringAndSelectionModels();

		imageView.installImageSelectionModel( tableRows, selectionModel );
		imageView.registerAsColoringListener( selectionColoringModel );

		final TableRowsTableView< DefaultSiteNameTableRow > tableView = showTable( bdvViewerPanel );

		imageView.registerTableView( tableView );

		final SitesImage sitesImage = createTableColoredSiteImage( imageView, fileNamingScheme, tableView );

		imageView.addToPanelAndBdv( sitesImage );

		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			final TableRowsScatterPlotView< DefaultSiteNameTableRow > scatterPlotView = new TableRowsScatterPlotView( tableRows, selectionColoringModel, selectionModel, imageView.getPlateName(), "infected_median", "not_infected_median" );
			scatterPlotView.show( bdvViewerPanel );

		}
	}

	public SitesImage createTableColoredSiteImage(
			ImagePlateViewer imageView,
			String fileNamingScheme,
			TableRowsTableView< DefaultSiteNameTableRow > tableView )
	{
		final SitesImage sitesImage = new SitesImage( tableRows, selectionColoringModel, imageView );

		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			NumericColoringModelDialog.dialogLocation = new Point( 10, imageView.getMainPanel().getLocationOnScreen().y + imageView.getMainPanel().getHeight() + 80 );

			final Set< String > columnNames = tableView.getTableRows().get( 0 ).getColumnNames();
			if ( columnNames.contains( "cell_based_score" ) )
				tableView.colorByColumn( "cell_based_score", ColoringLuts.VIRIDIS );
			else if ( columnNames.contains( "score1" ) )
				tableView.colorByColumn( "cell_based_score", ColoringLuts.VIRIDIS );
		}

		return sitesImage;
	}

	public TableRowsTableView< DefaultSiteNameTableRow > showTable( ViewerPanel bdvViewerPanel )
	{
		final TableRowsTableView< DefaultSiteNameTableRow > tableView =
				new TableRowsTableView<>( tableRows, selectionModel, selectionColoringModel );
		tableView.showTableAndMenu( bdvViewerPanel );
		tableView.setSelectionMode( TableRowsTableView.SelectionMode.FocusOnly );
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

	public void loadTable( String fileNamingScheme )
	{
		File tableFile = getTableFile( fileNamingScheme );

		tableRows = createSiteNameTableRowsFromFilePath(
				tableFile.getAbsolutePath(),
				fileNamingScheme );
	}

	public File getTableFile( String fileNamingScheme )
	{
		File tableFile = null;

		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			final String plateName = imagesDirectory.getName();
			tableFile = new File( imagesDirectory, plateName + "_analysis.csv" );
			if ( ! tableFile.exists() )
			{
				tableFile = new File( imagesDirectory, "analysis.csv" );
				if ( ! tableFile.exists() )
				{
					final String tableFilePath = IJ.getFilePath( "Please select table file" );
					if ( tableFilePath != null )
						tableFile = new File( tableFilePath );
				}
			}
		}

		return tableFile;
	}
}
