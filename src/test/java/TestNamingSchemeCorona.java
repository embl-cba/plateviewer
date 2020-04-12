import de.embl.cba.plateviewer.PlateViewer;
import net.imagej.ImageJ;

import java.io.File;

public class TestNamingSchemeCorona
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		new PlateViewer(
				new File( TestNamingSchemeCorona.class.getResource( "CORONA" ).getFile() ),
				".*.h5",
				new File( TestNamingSchemeCorona.class.getResource( "CORONA/default.csv" ).getFile() ),
				1);

//		new PlateViewer(
//				"/Volumes/kreshuk/pape/Work/data/covid-antibodies/data-processed/20200405_test_images/",
//				".*.h5",
//       		...
//				4 );

	}

}
