import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import de.embl.cba.plateviewer.PlateViewer;
import net.imagej.ImageJ;
import net.imglib2.Localizable;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.type.numeric.real.FloatType;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

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

		final PlateViewer plateViewer = new PlateViewer(
				new File( "/Users/tischer/Desktop/test3" ),
				".*.h5",
				new File( "/Users/tischer/Desktop/test3/analysis.csv" ),
				4 );
	}

}
