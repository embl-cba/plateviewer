package explore;

import de.embl.cba.plateviewer.github.IssueRaiser;
import de.embl.cba.plateviewer.github.LocationInformation;
import ij.IJ;
import ij.ImagePlus;

import java.io.IOException;
import java.net.URISyntaxException;

public class ExploreRaiseIssueWithImageAndDialog
{
	public static void main( String[] args ) throws IOException, URISyntaxException
	{
		final IssueRaiser issueRaiser = new IssueRaiser();

		final ImagePlus imp = IJ.openImage( "/Users/tischer/Documents/fiji-plugin-plateViewer/src/test/resources/ALMF-EMBL-JPEG/P001--A1--A1/D0004BS000000007-1uM--A1--A1--W0001--P001--T00001--Z001--C01.ome.jpeg" );

		final LocationInformation locationInformation = new LocationInformation( "Plate01", "Site01", new double[]{ 10, 10, 0 } );
		issueRaiser.showPlateIssueDialogAndCreateIssue( locationInformation, imp );
	}
}
