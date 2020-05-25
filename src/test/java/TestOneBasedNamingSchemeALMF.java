import de.embl.cba.plateviewer.PlateViewer;
import net.imagej.ImageJ;

public class TestOneBasedNamingSchemeALMF
{
	public static void main( String[] args )
	{
		new ImageJ().ui().showUI();

		new PlateViewer(
				"src/test/resources/ALMF-EMBL-OneBased-P2-S16-C3-T1",
				".*.tif",
				1);
	}
}
