package develop;

import de.embl.cba.plateviewer.PlateViewer;
import org.junit.jupiter.api.Test;

public class DevelopNamingSchemeOperettaHarmony4
{
	public static void main( String[] args )
	{
		new DevelopNamingSchemeOperettaHarmony4().run();
	}

	@Test
	public void run()
	{
		final PlateViewer plateViewer = new PlateViewer(
				"/Users/tischer/Downloads/OperettaHarmony4_1_dataexport",
				".*.tif",
				1 );
		plateViewer.run();
	}
}
