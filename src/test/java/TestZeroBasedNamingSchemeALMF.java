import de.embl.cba.plateviewer.view.PlateViewerImageView;
import net.imagej.ImageJ;

public class TestZeroBasedNamingSchemeALMF
{
	public static void main( String[] args )
	{
		new ImageJ().ui().showUI();

		new PlateViewerImageView(
				"src/test/resources/ALMF-EMBL-ZeroBased-P2-S4-C2-T1",
				".*.tif",
				1);
	}
}
