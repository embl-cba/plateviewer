import de.embl.cba.plateviewer.view.ImagePlateViewer;

public class TestNamingSchemeALMFJPEG
{
	public static void main( String[] args )
	{
		new ImagePlateViewer(
				TestNamingSchemeALMFJPEG.class.getResource( "ALMF-EMBL-JPEG" ).getFile(),
				".*.jpeg",
				4 );

	}
}
