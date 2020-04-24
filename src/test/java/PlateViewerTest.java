import de.embl.cba.plateviewer.view.ImagePlateViewer;

public class PlateViewerTest
{
	public static void main( String[] args )
	{
//		new PlateViewer(
//				PlateViewerTest.class.getResource( "ALMF-EMBL-ZeroBased-P2-S4-C2-T1" ).getFile(),
//				".*.tif",
//				1);

		new ImagePlateViewer(
				"/Volumes/pepperkok/mkhan/FDAscreen/testplate04/20181203_175618_645/",
				".*.tif",
				1);
	}
}
