package explore;

import de.embl.cba.plateviewer.github.GitHubIssue;
import de.embl.cba.plateviewer.github.IssueRaiser;
import de.embl.cba.plateviewer.github.PlateLocation;
import de.embl.cba.plateviewer.github.UrlOpener;
import ij.IJ;
import ij.ImagePlus;

import java.io.IOException;
import java.net.URISyntaxException;

public class ExploreRaiseIssueWithImage
{
	public static void main( String[] args ) throws IOException, URISyntaxException
	{
		final IssueRaiser issueRaiser = new IssueRaiser();

		final ImagePlus imp = IJ.openImage( "/Users/tischer/Documents/fiji-plugin-plateViewer/src/test/resources/ALMF-EMBL-JPEG/P001--A1--A1/D0004BS000000007-1uM--A1--A1--W0001--P001--T00001--Z001--C01.ome.jpeg" );

		final String a0 = "2309df0a4fbbf32b9b3";
		final String accessToken = a0 + "cdbb4ca946162378ee4a5";
		final String repository = "https://github.com/hci-unihd/antibodies-analysis-issues";

		final GitHubIssue gitHubIssue = new GitHubIssue( "Test upload image", "Test", new String[]{ "test" } );

		final int issue = issueRaiser.postIssue( repository, accessToken, gitHubIssue, imp );

		UrlOpener.openUrl( repository + "/issues/" + issue );
	}
}
