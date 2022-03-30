import de.embl.cba.plateviewer.PlateViewer;
import net.imagej.ImageJ;

public class TestZeroBasedNamingSchemeALMF
{
	public static void main( String[] args )
	{
		new ImageJ().ui().showUI();

		final PlateViewer plateViewer = new PlateViewer(
				"src/test/resources/ALMF-EMBL-ZeroBased-P2-S4-C2-T1",
				".*.tif",
				1 );
		plateViewer.run();
	}
}
