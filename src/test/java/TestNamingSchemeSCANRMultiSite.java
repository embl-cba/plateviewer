import de.embl.cba.plateviewer.view.ImagePlateViewer;
import net.imagej.ImageJ;

public class TestNamingSchemeSCANRMultiSite
{
	public static void main( String[] args )
	{
		new ImageJ().ui().showUI();

		new ImagePlateViewer(
				TestNamingSchemeSCANRMultiSite.class.getResource( "SCANR-S9-C1-T1" ).getFile(),
				".*.tif",
				1);
	}
}
