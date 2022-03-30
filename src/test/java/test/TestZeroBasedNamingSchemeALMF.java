package test;

import de.embl.cba.plateviewer.PlateViewer;
import org.junit.jupiter.api.Test;

public class TestZeroBasedNamingSchemeALMF
{
	public static void main( String[] args )
	{
		new TestZeroBasedNamingSchemeALMF().run();
	}

	@Test
	public void run()
	{
		final PlateViewer plateViewer = new PlateViewer(
				"src/test/resources/ALMF-EMBL-ZeroBased-P2-S4-C2-T1",
				".*.tif",
				1 );
		plateViewer.run();
	}
}
