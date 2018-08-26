import de.embl.cba.multipositionviewer.TemplateMatchingPlugin;
import ij.*;
import ij.measure.Calibration;
import ij.process.*;
import ij.gui.*;
import java.awt.Color;
import ij.plugin.*;
import ij.plugin.filter.MaximumFinder;
import ij.measure.ResultsTable;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static de.embl.cba.multipositionviewer.TemplateMatchingPlugin.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class TemplateMatchingTest
{
	public static void main ( String... args )
	{
		ImagePlus overview = IJ.openImage( "/Users/tischer/Documents/giulia-mizzon-CLEM--data/template-matching-development/2D_lowMag.tif" );

		ArrayList< ImagePlus > templates = new ArrayList<>(  );

		templates.add( IJ.openImage( "/Users/tischer/Documents/giulia-mizzon-CLEM--data/template-matching-development/tomogram_g22_t27_avgProjection.tif" ) );
		templates.add( IJ.openImage( "/Users/tischer/Documents/giulia-mizzon-CLEM--data/template-matching-development/tomogram_g22_t29_avgProjection.tif" ) );

		final double overviewPixelWidth = overview.getCalibration().pixelWidth;

		final Scaler scaler = new Scaler();

		for ( ImagePlus template : templates )
		{
			final double templatePixelWidth = template.getCalibration().pixelWidth;
			final double scale = templatePixelWidth / overviewPixelWidth;

			IJ.run(template, "Scale...", "x="+scale+" y="+scale+" interpolation=Bilinear average");

			template.show();
		}

		long startTime = System.currentTimeMillis();

//		FloatProcessor rFp = doMatch( img1, img2, method, showR );

		long elapsedTime = System.currentTimeMillis() - startTime;


	}
}
