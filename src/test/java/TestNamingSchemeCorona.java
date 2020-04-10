import de.embl.cba.plateviewer.PlateViewer;
import net.imagej.ImageJ;

public class TestNamingSchemeCorona
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		new PlateViewer(
				TestNamingSchemeCorona.class.getResource( "CORONA" ).getFile(),
				".*.h5",
				4 );

//		new PlateViewer(
//				"/Volumes/kreshuk/pape/Work/data/covid-antibodies/data-processed/20200405_test_images/",
//				".*.h5",
//				4 );

	}
}
