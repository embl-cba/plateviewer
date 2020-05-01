package explore;

import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.plot.ScatterPlotOverlay;
import de.embl.cba.plateviewer.plot.TableRowsScatterPlotView;
import de.embl.cba.plateviewer.table.DefaultSiteTableRow;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.DefaultSelectionModel;

import java.io.File;
import java.util.List;

import static de.embl.cba.plateviewer.table.Tables.createSiteTableRowsFromFile;

public class ExploreTableRowsScatterPlotView
{
	public static void main( String[] args )
	{
		final File file = new File( "/Users/tischer/Desktop/analysis.csv" );

		final DefaultSelectionModel< DefaultSiteTableRow > selectionModel = new DefaultSelectionModel<>();

		final LazyCategoryColoringModel< DefaultSiteTableRow > coloringModel = new LazyCategoryColoringModel<>( new GlasbeyARGBLut( 255 ) );

		final SelectionColoringModel< DefaultSiteTableRow > selectionColoringModel = new SelectionColoringModel(
				coloringModel,
				selectionModel );

		final List< DefaultSiteTableRow > tableRows = createSiteTableRowsFromFile(
				file.getAbsolutePath(),
				NamingSchemes.PATTERN_NIKON_TI2_HDF5 );

		final TableRowsScatterPlotView< DefaultSiteTableRow > scatterPlotView = new TableRowsScatterPlotView( tableRows, selectionColoringModel, selectionModel, "title", "not_infected_median", "infected_median", null, ScatterPlotOverlay.Y_1_2 );

		scatterPlotView.show( null );
	}
}
