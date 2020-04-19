import de.embl.cba.plateviewer.PlateViewer;
import net.imagej.ImageJ;

import java.io.File;

public class TestNamingSchemeCorona
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

//		new PlateViewer(
//				new File( TestNamingSchemeCorona.class.getResource( "CORONA" ).getFile() ),
//				".*.h5",
//				new File( TestNamingSchemeCorona.class.getResource( "CORONA/default.csv" ).getFile() ),
//				1);
//
//		final PlateViewer plateViewer = new PlateViewer(
//				new File( "/Users/tischer/Desktop/test4" ),
//				".*.h5",
//				null, //new File( "/Users/tischer/Desktop/test4/analysis.csv" ),
//				1 );

		final PlateViewer plateViewer = new PlateViewer(
				new File( "/Volumes/kreshuk/pape/Work/data/covid-antibodies/data-processed/20200406_164555_328" ),
				".*.h5",
				null, //new File( "/Users/tischer/Desktop/test4/analysis.csv" ),
				1, false );

	}

}
