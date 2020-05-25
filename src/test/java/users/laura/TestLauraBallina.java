package users.laura;

import de.embl.cba.plateviewer.PlateViewerInitializer;
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
		rawImagesFolder = "/Volumes/almfscreen/ballina/BigDataViewer_test_dataset/images_BaseFileName";
		rawImagesFolder = "/Volumes/almfscreen/ballina/BigDataViewer_test_dataset/Simonsen-LBP-plate02-batch1-01";

		final String tablePath = "/Volumes/almfscreen/ballina/BigDataViewer_test_dataset/image_table_with_mean_object_measurements_QC.txt";

		final PlateViewerInitializer plateViewerInitializer = new PlateViewerInitializer(
				new File( rawImagesFolder ),
				".*.tif.*",
				true,
				false,
				false,
				4,
				true );

		final TableSource tableSource = new TableSource();
		tableSource.filePath = tablePath;
		tableSource.intervalType = IntervalType.Sites;
		plateViewerInitializer.setSiteTableSource( tableSource );

		plateViewerInitializer.addInputImagesDirectory( "/Volumes/almfscreen/ballina/BigDataViewer_test_dataset/images_BaseFileName" );

		plateViewerInitializer.run();
	}

}
