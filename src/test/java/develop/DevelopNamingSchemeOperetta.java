package develop;

import de.embl.cba.plateviewer.PlateViewer;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static de.embl.cba.plateviewer.image.NamingSchemes.PATTERN_OPERETTA;

public class DevelopNamingSchemeOperetta
{
	public static void main( String[] args )
	{
		new DevelopNamingSchemeOperetta().run();
	}

	@Test
	public void run()
	{

		final boolean matches = Pattern.compile( PATTERN_OPERETTA ).matcher( "r01c02f05p01-ch2sk1fk1fl1.tiff" ).matches();

		if ( ! matches )
			throw new RuntimeException("This should match!");

		final PlateViewer plateViewer = new PlateViewer(
				"/Users/tischer/Downloads/OperettaHarmony4_1_dataexport",
				".*.tiff",
				1 );
		plateViewer.run();
	}
}
