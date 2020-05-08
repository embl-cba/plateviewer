package explore;

import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.plot.ScatterPlotGridLinesOverlay;
import de.embl.cba.plateviewer.plot.TableRowsScatterPlotView;
import de.embl.cba.plateviewer.table.AnnotatedIntervalTableRow;
import de.embl.cba.plateviewer.table.DefaultAnnotatedIntervalTableRow;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.DefaultSelectionModel;

import java.io.File;
import java.util.List;

import static de.embl.cba.plateviewer.mongo.AssayMetadataRepository.getCovid19AssayMetadataRepository;
import static de.embl.cba.plateviewer.table.Tables.createAnnotatedIntervalTableRowsFromFileAndRepository;

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

		final List< ? extends AnnotatedIntervalTableRow > tableRows = createAnnotatedIntervalTableRowsFromFileAndRepository(
				file.getAbsolutePath(),
				NamingSchemes.PATTERN_NIKON_TI2_HDF5, null, "tables/images/default", getCovid19AssayMetadataRepository( "covid" + (2500 + 81 ) ) );

		final TableRowsScatterPlotView< DefaultAnnotatedIntervalTableRow > scatterPlotView = new TableRowsScatterPlotView( tableRows, "sites scatter plot", selectionColoringModel, selectionModel, "title", "not_infected_median", "infected_median", ScatterPlotGridLinesOverlay.Y_N );

		scatterPlotView.show( null );
	}
}
