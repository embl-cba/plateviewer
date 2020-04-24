package de.embl.cba.plateviewer;

import bdv.util.Bdv;
import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.image.table.TableImage;
import de.embl.cba.plateviewer.table.DefaultSiteNameTableRow;
import de.embl.cba.plateviewer.table.SiteName;
import de.embl.cba.plateviewer.view.ImagePlateViewer;
import de.embl.cba.tables.color.ColoringLuts;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.view.TableRowsTableView;
import ij.IJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.awt.*;
import java.io.File;
import java.util.List;

import static de.embl.cba.plateviewer.table.ImageNameTableRows.createSiteNameTableRowsFromFilePath;

public class PlateViewer < R extends NativeType< R > & RealType< R >, T extends SiteName >
{
	private final File imagesDirectory;
	private final String filePattern;
	private final boolean loadImageTable;
	private final int numIoThreads;
	private final boolean includeSubFolders;

	public PlateViewer( File imagesDirectory, String filePattern, boolean loadImageTable, int numIoThreads, boolean includeSubFolders )
	{
		this.imagesDirectory = imagesDirectory;
		this.filePattern = filePattern;
		this.loadImageTable = loadImageTable;
		this.numIoThreads = numIoThreads;
		this.includeSubFolders = includeSubFolders;

		final ImagePlateViewer< R, T > imagePlateView = new ImagePlateViewer( imagesDirectory.toString(), filePattern, numIoThreads, includeSubFolders );

		if ( loadImageTable )
		{
			showTable( imagePlateView );
		}
	}

	public void showTable( ImagePlateViewer imageView )
	{
		final String fileNamingScheme = imageView.getFileNamingScheme();

		File tableFile = getTableFile( fileNamingScheme );

		final DefaultSelectionModel< DefaultSiteNameTableRow > selectionModel = new DefaultSelectionModel<>();

		final LazyCategoryColoringModel< DefaultSiteNameTableRow > coloringModel = new LazyCategoryColoringModel<>( new GlasbeyARGBLut( 255 ) );

		final SelectionColoringModel< DefaultSiteNameTableRow > selectionColoringModel = new SelectionColoringModel(
				coloringModel,
				selectionModel );

		final List< DefaultSiteNameTableRow > tableRows = createSiteNameTableRowsFromFilePath(
				tableFile.getAbsolutePath(),
				fileNamingScheme );

		final TableRowsTableView< DefaultSiteNameTableRow > tableView =
				new TableRowsTableView<>( tableRows, selectionModel, selectionColoringModel );

		final Component parentComponent = imageView.getBdvHandle().getBdvHandle().getViewerPanel();
		tableView.showTableAndMenu( parentComponent );
		tableView.setSelectionMode( TableRowsTableView.SelectionMode.FocusOnly );
		imageView.installImageSelectionModel( tableRows, selectionModel );
		imageView.registerAsColoringListener( selectionColoringModel );

		final TableImage tableImage = new TableImage( tableRows, selectionColoringModel, imageView );

		imageView.registerTableView( tableView );
		imageView.addToPanelAndBdv( tableImage );

		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
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
