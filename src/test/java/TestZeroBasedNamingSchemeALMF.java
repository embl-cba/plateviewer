import de.embl.cba.plateviewer.view.ImagePlateViewer;
import net.imagej.ImageJ;

public class TestZeroBasedNamingSchemeALMF
{
	public static void main( String[] args )
	{
		new ImageJ().ui().showUI();

		new ImagePlateViewer(
				"src/test/resources/ALMF-EMBL-ZeroBased-P2-S4-C2-T1",
				".*.tif",
				1);
	}
}
