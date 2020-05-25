import de.embl.cba.plateviewer.PlateViewer;

public class TestNamingSchemeALMFJPEG
{
	public static void main( String[] args )
	{
		new PlateViewer(
				TestNamingSchemeALMFJPEG.class.getResource( "ALMF-EMBL-JPEG" ).getFile(),
				".*.jpeg",
				4 );

	}
}
