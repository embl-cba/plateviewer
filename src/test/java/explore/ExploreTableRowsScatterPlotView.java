package explore;

import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.plot.ScatterPlotOverlay;
import de.embl.cba.plateviewer.plot.TableRowsScatterPlotView;
import de.embl.cba.plateviewer.table.DefaultAnnotatedIntervalTableRow;
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

		final DefaultSelectionModel< DefaultAnnotatedIntervalTableRow > selectionModel = new DefaultSelectionModel<>();

		final LazyCategoryColoringModel< DefaultAnnotatedIntervalTableRow > coloringModel = new LazyCategoryColoringModel<>( new GlasbeyARGBLut( 255 ) );

		final SelectionColoringModel< DefaultAnnotatedIntervalTableRow > selectionColoringModel = new SelectionColoringModel(
				coloringModel,
				selectionModel );

		final List< DefaultAnnotatedIntervalTableRow > tableRows = createSiteTableRowsFromFile(
				file.getAbsolutePath(),
				NamingSchemes.PATTERN_NIKON_TI2_HDF5, null );

		final TableRowsScatterPlotView< DefaultAnnotatedIntervalTableRow > scatterPlotView = new TableRowsScatterPlotView( tableRows, "sites scatter plot", selectionColoringModel, selectionModel, "title", "not_infected_median", "infected_median", ScatterPlotOverlay.Y_1_2 );

		scatterPlotView.show( null );
	}
}
