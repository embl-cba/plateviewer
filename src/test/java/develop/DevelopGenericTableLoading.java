package develop;

import de.embl.cba.plateviewer.PlateViewerInitializer;

import java.io.File;

public class DevelopGenericTableLoading
{
	public static void main( String[] args )
	{
		final PlateViewerInitializer plateViewerInitializer = new PlateViewerInitializer(
				new File( "/g/kreshuk/data/covid/telesto/data-processed/training_set_plateviewer" ),
				".*.h5",
				1,
				false );

		plateViewerInitializer.run();
	}
}
