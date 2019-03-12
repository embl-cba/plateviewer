import de.embl.cba.plateviewer.PlateViewer;

public class TestNamingSchemeALMFJPEG
{
	public static void main( String[] args )
	{
//		new PlateViewer(
//				"/Volumes/almfscreen/mkhan/FDAdrugscreenRep01/D0004BS000000007-1uM",
//				".*.jpeg",
//				4);

		new PlateViewer(
				TestNamingSchemeALMFJPEG.class.getResource( "ALMF-EMBL-JPEG" ).getFile(),
				".*.jpeg",
				4 );

	}
}
