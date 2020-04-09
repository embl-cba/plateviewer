import de.embl.cba.plateviewer.PlateViewer;

public class TestNamingSchemeCorona
{
	public static void main( String[] args )
	{
		new PlateViewer(
				TestNamingSchemeCorona.class.getResource( "CORONA" ).getFile(),
				".*.h5",
				4 );


	}
}
