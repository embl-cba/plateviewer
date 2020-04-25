package explore;

import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.plot.TableRowsScatterPlotView;
import de.embl.cba.plateviewer.table.DefaultSiteNameTableRow;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.DefaultSelectionModel;

import java.io.File;
import java.util.List;

import static de.embl.cba.plateviewer.table.ImageNameTableRows.createSiteNameTableRowsFromFilePath;

public class ExploreTableRowsScatterPlotView
{
	public static void main( String[] args )
	{
		final File file = new File( "/Users/tischer/Desktop/analysis.csv" );

		final DefaultSelectionModel< DefaultSiteNameTableRow > selectionModel = new DefaultSelectionModel<>();

		final LazyCategoryColoringModel< DefaultSiteNameTableRow > coloringModel = new LazyCategoryColoringModel<>( new GlasbeyARGBLut( 255 ) );

		final SelectionColoringModel< DefaultSiteNameTableRow > selectionColoringModel = new SelectionColoringModel(
				coloringModel,
				selectionModel );

		final List< DefaultSiteNameTableRow > tableRows = createSiteNameTableRowsFromFilePath(
				file.getAbsolutePath(),
				NamingSchemes.PATTERN_NIKON_TI2_HDF5 );

		final TableRowsScatterPlotView< DefaultSiteNameTableRow > scatterPlotView = new TableRowsScatterPlotView( tableRows, selectionColoringModel, selectionModel, "infected_median", "not_infected_median" );

		scatterPlotView.setColumns( "infected_median", "not_infected_median" );
	}
}
