package explore;

import de.embl.cba.plateviewer.io.JpegOutputStreamWriter;
import de.embl.cba.tables.github.GitHubFileCommitter;
import ij.IJ;
import ij.ImagePlus;

public class ExploreImagePlusToJpegByteStream
{
	public static void main( String[] args )
	{
		final ImagePlus imp = IJ.openImage( "/Users/tischer/Documents/fiji-plugin-plateViewer/src/test/resources/ALMF-EMBL-JPEG/P001--A1--A1/D0004BS000000007-1uM--A1--A1--W0001--P001--T00001--Z001--C01.ome.jpeg" );

		final String base64String = JpegOutputStreamWriter.createBase64String( imp,1.0F );

		final String a0 = "2309df0a4fbbf32b9b3";
		final String accessToken = a0 + "cdbb4ca946162378ee4a5";

		final GitHubFileCommitter fileCommitter =
				new GitHubFileCommitter(
						"https://github.com/hci-unihd/antibodies-analysis-issues",
						accessToken,
						"screenshots/test3.jpg" );

		fileCommitter.commitStringAsFile( "Test commit an image", base64String );


//		final FileSaver saver = new FileSaver( imp );
//		saver.saveAsJpeg( "/Users/tischer/Desktop/test.jpg" );
	}
}
