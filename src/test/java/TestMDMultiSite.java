import de.embl.cba.plateviewer.view.ImagePlateViewer;

public class TestMDMultiSite
{
	public static void main( String[] args )
	{
//		new PlateViewer(
//				"/Volumes/almfscreen/mkhan/FDAdrugscreenRep01/D0004BS000000007-1uM",
//				".*.jpeg",
//				4);

		new ImagePlateViewer(
				TestMDMultiSite.class.getResource( "MD-P2-S4-C1-T1" ).getFile(),
				".*",
				4 );

	}
}
