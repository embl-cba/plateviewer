package test;

import de.embl.cba.plateviewer.PlateViewer;
import org.junit.jupiter.api.Test;

public class TestNamingSchemeALMFJPEG
{
	public static void main( String[] args )
	{
		new TestNamingSchemeALMFJPEG().run();
	}

	@Test
	public void run()
	{
		final PlateViewer plateViewer = new PlateViewer(
				"src/test/resources/ALMF-EMBL-JPEG",
				".*.jpeg",
				4 );
		plateViewer.run();
	}
}
