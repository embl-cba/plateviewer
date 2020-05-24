import de.embl.cba.plateviewer.PlateViewer;
import de.embl.cba.plateviewer.table.IntervalType;
import de.embl.cba.plateviewer.table.TableSource;
import net.imagej.ImageJ;

import java.io.File;

public class TestLauraBallina
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		String rawImagesFolder;
		rawImagesFolder = "/Users/tischer/Downloads/Laura";
		rawImagesFolder = "/g/almfscreen/ballina/BigDataViewer_test_dataset";

		final String tablePath = rawImagesFolder + "/image_table_with_mean_object_measurements_QC.txt";

		final PlateViewer plateViewer = new PlateViewer(
				new File( rawImagesFolder ),
				".*.tif",
				true,
				false,
				false,
				4,
				true );

		final TableSource tableSource = new TableSource();
		tableSource.filePath = tablePath;
		tableSource.intervalType = IntervalType.Sites;
		plateViewer.setSiteTableSource( tableSource );

		plateViewer.run();
	}

}
