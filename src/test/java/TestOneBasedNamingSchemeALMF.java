import de.embl.cba.plateviewer.view.PlateViewerImageView;
import net.imagej.ImageJ;

public class TestOneBasedNamingSchemeALMF
{
	public static void main( String[] args )
	{
		new ImageJ().ui().showUI();

		new PlateViewerImageView(
				"src/test/resources/ALMF-EMBL-OneBased-P2-S16-C3-T1",
				".*.tif",
				1);
	}
}
