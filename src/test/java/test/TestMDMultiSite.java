package test;

import de.embl.cba.plateviewer.PlateViewer;
import org.junit.jupiter.api.Test;

public class TestMDMultiSite
{
	public static void main( String[] args )
	{
		new TestMDMultiSite().run();
	}

	@Test
	public void run()
	{
		final PlateViewer plateViewer = new PlateViewer(
				"src/test/resources/MD-P2-S4-C1-T1",
				".*",
				4 );
		plateViewer.run();
	}
}
