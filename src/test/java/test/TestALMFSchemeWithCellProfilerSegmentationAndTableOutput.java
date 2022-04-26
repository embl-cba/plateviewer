package test;

import de.embl.cba.plateviewer.PlateViewerInitializer;
import de.embl.cba.plateviewer.table.IntervalType;
import de.embl.cba.plateviewer.table.TableSource;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TestALMFSchemeWithCellProfilerSegmentationAndTableOutput
{
	public static void main( String[] args )
	{
		new TestALMFSchemeWithCellProfilerSegmentationAndTableOutput().run();
	}

	@Test
	public void run()
	{
		final PlateViewerInitializer plateViewerInitializer = new PlateViewerInitializer( new File("src/test/resources/ALMF-CellProfiler-Segmentations-And-Table/input"), ".*.tif.*", 4, true );

		final File additionalImagesDirectory = new File("src/test/resources/ALMF-CellProfiler-Segmentations-And-Table/CP_output/images");

		if ( additionalImagesDirectory != null & additionalImagesDirectory.exists() )
		{
			plateViewerInitializer.addInputImagesDirectory( additionalImagesDirectory.getAbsolutePath() );
		}

		File imageTableFile = new File("src/test/resources/ALMF-CellProfiler-Segmentations-And-Table/CP_output/table/test_data_image_table_with_mean_object_measurements_QC.txt" );

		if ( imageTableFile != null && imageTableFile.exists() )
		{
			final TableSource tableSource = new TableSource();
			tableSource.filePath = imageTableFile.getAbsolutePath();
			tableSource.intervalType = IntervalType.Sites;
			plateViewerInitializer.setSiteTableSource( tableSource );
		}

		plateViewerInitializer.run();
	}
}
