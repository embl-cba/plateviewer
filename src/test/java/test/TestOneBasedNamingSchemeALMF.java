package test;

import de.embl.cba.plateviewer.PlateViewer;
import net.imagej.ImageJ;
import org.junit.jupiter.api.Test;

public class TestOneBasedNamingSchemeALMF
{
	public static void main( String[] args )
	{
		new ImageJ().ui().showUI();
		new TestOneBasedNamingSchemeALMF().run();
	}

	@Test
	public void run()
	{
		final PlateViewer plateViewer = new PlateViewer(
				"src/test/resources/ALMF-EMBL-OneBased-P2-S16-C3-T1",
				".*.tif",
				1 );
		plateViewer.run();
	}
}
