package develop;

import de.embl.cba.plateviewer.PlateViewer;

import java.util.regex.Pattern;

import static de.embl.cba.plateviewer.source.NamingSchemes.PATTERN_OPERETTA;

public class DevelopNamingSchemeOperetta
{
	public static void main( String[] args )
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
