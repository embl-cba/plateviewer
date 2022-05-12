package test;

import de.embl.cba.plateviewer.PlateViewer;
import org.junit.jupiter.api.Test;

public class TestMDMultiSite
{
	public static void main( String[] args )
	{
		new TestMDMultiSite().test2();
	}

	@Test
	public void test1()
	{
		final PlateViewer plateViewer = new PlateViewer(
				"src/test/resources/MD-P2-S4-C1-T1",
				".*.tif",
				4 );
		plateViewer.run();
	}

	@Test
	public void test2()
	{
		final PlateViewer plateViewer = new PlateViewer(
				"src/test/resources/MD-MULTISITE-TIF",
				".*.TIF",
				4 );
		plateViewer.run();
	}
}
