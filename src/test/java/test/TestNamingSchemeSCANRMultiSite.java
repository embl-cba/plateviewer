package test;

import de.embl.cba.plateviewer.PlateViewer;
import net.imagej.ImageJ;
import org.junit.jupiter.api.Test;

public class TestNamingSchemeSCANRMultiSite
{
	public static void main( String[] args )
	{
		new TestNamingSchemeSCANRMultiSite().run();
	}

	@Test
	public void run()
	{
		final PlateViewer plateViewer = new PlateViewer(
				"src/test/resources/SCANR-S9-C1-T1",
				".*.tif",
				1 );
		plateViewer.run();
	}
}
