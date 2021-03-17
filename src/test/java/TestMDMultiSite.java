import de.embl.cba.plateviewer.PlateViewer;

public class TestMDMultiSite
{
	public static void main( String[] args )
	{
		new PlateViewer(
				TestMDMultiSite.class.getResource( "MD-P2-S4-C1-T1" ).getFile(),
				".*",
				4 ).run();

	}
}
