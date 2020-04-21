import de.embl.cba.plateviewer.view.PlateViewerImageView;

public class TestNamingSchemeALMFJPEG
{
	public static void main( String[] args )
	{
		new PlateViewerImageView(
				TestNamingSchemeALMFJPEG.class.getResource( "ALMF-EMBL-JPEG" ).getFile(),
				".*.jpeg",
				4 );

	}
}
