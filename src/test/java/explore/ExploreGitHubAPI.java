package explore;

import de.embl.cba.plateviewer.github.GitHubFileCommitter;

import java.util.Base64;

public class ExploreGitHubAPI
{
	public static void main( String[] args )
	{
		final String a0 = "2309df0a4fbbf32b9b3";
		final String accessToken = a0 + "cdbb4ca946162378ee4a5";

		final GitHubFileCommitter fileCommitter =
				new GitHubFileCommitter(
						"https://github.com/hci-unihd/antibodies-analysis-issues",
						accessToken,
						"screenshots/test.txt" );

		fileCommitter.commitStringAsFile(
				"test commit",
				Base64.getEncoder().encodeToString( "Hello World".getBytes() ) );
	}
}
