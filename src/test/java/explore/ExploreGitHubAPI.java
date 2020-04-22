package explore;

import de.embl.cba.plateviewer.github.GitHubFileCommit;
import de.embl.cba.plateviewer.github.GitHubFileCommitter;
import de.embl.cba.plateviewer.github.GitHubIssue;

import java.io.IOException;

public class ExploreGitHubAPI
{
	public static void main( String[] args ) throws IOException
	{
		final String a0 = "2309df0a4fbbf32b9b3";
		final String accessToken = a0 + "cdbb4ca946162378ee4a5";

		final GitHubFileCommitter fileCommitter =
				new GitHubFileCommitter(
						"tischi",
						"https://github.com/hci-unihd/antibodies-analysis-issues",
						accessToken,
						"screenshots/test.txt" );

		fileCommitter.commitFile();


		// curl -i -u tischi:ad99337fffaaf428069ca2e6a7761abe5791399c -d '{"title": "aaaA sample new issue", "body": "The user interface is upside down"}' https://api.github.com/repos/tischi/HTM_Explorer/issues

//		JsonObjectMapper mapper = new JsonObjectMapper();
//		String json = mapper.writeValue( new Issue() );
//		System.out.println("ResultingJSONstring = " + json);
//		//System.out.println(json);
//
//
//		URL obj = new URL("https://api.github.com/repos/tischi/HTM_Explorer/issues");
//		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//
//		con.setRequestMethod("POST");
//		con.setRequestProperty("Content-Type","application/json");
//		con.setRequestProperty("Authorization", "Token " + "???");
//
//		con.setDoOutput(true);
//		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//		wr.writeBytes(json);
//		wr.flush();
//		wr.close();
//
//		int responseCode = con.getResponseCode();
//		System.out.println("Response Code : " + responseCode);


		// NOTE: Once a token has been put here, it will be disabled!!

//
//		final HttpResponse< String  > response =
//				Unirest.post( "https://api.github.com/repos/tischi/HTM_Explorer/issues" )
//					.header( "accept", "application/json" )
//					.basicAuth( "tischi", "29b00ef73853f313e6aa54833a0ce44815fb1413" )
//					.body( json )
//				.asString();
//
//		System.out.println( response.getBody() );
	}
}
