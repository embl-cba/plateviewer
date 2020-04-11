import de.embl.cba.plateviewer.view.PlateViewerImageView;
import net.imagej.ImageJ;

public class TestNamingSchemeSCANRMultiSite
{
	public static void main( String[] args )
	{
		new ImageJ().ui().showUI();

		new PlateViewerImageView(
				TestNamingSchemeSCANRMultiSite.class.getResource( "SCANR-S9-C1-T1" ).getFile(),
				".*.tif",
				1);
	}
}
