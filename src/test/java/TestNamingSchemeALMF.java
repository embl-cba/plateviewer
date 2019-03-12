import de.embl.cba.plateviewer.PlateViewer;

public class TestNamingSchemeALMF
{
	public static void main( String[] args )
	{
		new PlateViewer(
				TestNamingSchemeALMF.class.getResource( "ALMF-EMBL-P2-S4-C2-T1" ).getFile(),
				".*.tif",
				1);
	}
}
