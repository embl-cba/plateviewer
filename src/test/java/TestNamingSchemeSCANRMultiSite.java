import de.embl.cba.plateviewer.PlateViewer;
import net.imagej.ImageJ;

public class TestNamingSchemeSCANRMultiSite
{
	public static void main( String[] args )
	{
		new ImageJ().ui().showUI();

		new PlateViewer(
				TestNamingSchemeSCANRMultiSite.class.getResource( "SCANR-S9-C1-T1" ).getFile(),
				".*.tif",
				1);
	}
}
