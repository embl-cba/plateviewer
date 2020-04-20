package de.embl.cba.plateviewer.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.embl.cba.tables.Logger;
import ij.Prefs;
import ij.gui.GenericDialog;


import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class IssueRaiser
{

	public static final String USER_NAME = "PlateViewer.GitHub user name";
	public static final String REPOSITORY = "PlateViewer.GitHub repository";
	public static final String ACCESS_TOKEN = "PlateViewer.GitHub access token";
	public static final String ERROR = "Something went wrong raising the issue:\n";
	private String userName;
	private String repository;
	private String accessToken;
	private String issueBody;
	private String plateName;
	private String siteName;
	private int x;
	private int y;

	public void showDialogAndCreateIssue( String plateName, String siteName, int x, int y )
	{
		this.plateName = plateName;
		this.siteName = siteName;
		this.x = x;
		this.y = y;

		if ( ! showDialog() ) return;

		postIssue();
	}

	public void postIssue()
	{
		String issueJson = null;
		try
		{
			issueJson = createIssueJson();
		} catch ( JsonProcessingException e )
		{
			e.printStackTrace();
		}

		String url = getApiUrl();

		try
		{
			URL obj = new URL( url );
			HttpURLConnection con = ( HttpURLConnection ) obj.openConnection();

			con.setRequestMethod( "POST" );
			con.setRequestProperty( "Content-Type", "application/json" );
			con.setRequestProperty( "Authorization", "Token " + accessToken );

			con.setDoOutput( true );
			DataOutputStream wr = new DataOutputStream( con.getOutputStream() );
			wr.writeBytes( issueJson );
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			if ( responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED )
			{
				// worked fine
			}
			else
			{
				Logger.error( ERROR + "Unexpected response code: " + responseCode );
			}
		}
		catch( Exception e )
		{
			Logger.error( ERROR + "Please see the error in the console" );
			System.err.println( e );
		}

//		final HttpResponse< String  > response =
//				Unirest.post( url )
//						.header( "A", "a" )
//						.header( "accept", "application/json" )
//						.basicAuth( userName, accessToken )
//						.body( issueJson )
//				.asString();
//
//		System.out.println( response.getBody() );
	}

	public String getApiUrl()
	{
		String url = repository.replace( "github.com", "api.github.com/repos" );
		if ( url.endsWith( "/" ) ) url += "issues";
		else url += "/issues";
		return url;
	}

	public String createIssueJson() throws JsonProcessingException
	{
		final Issue issue = new Issue();
		issue.title = "Plate " + plateName + " Site " + siteName;

		final ObjectMapper objectMapper = new ObjectMapper();

		final PlateLocation plateLocation = new PlateLocation();
		plateLocation.plateName = plateName;
		plateLocation.siteName = siteName;
		plateLocation.xyPixelLocation = new int[]{x, y};
		issue.body = objectMapper.writeValueAsString( plateLocation );
		issue.body += "\n\n" + issueBody;
		return objectMapper.writeValueAsString( issue );
	}

	public boolean showDialog()
	{
		final GenericDialog gd = new GenericDialog( "Raise GitHub issue" );
		final int columns = 80;

		gd.addStringField( "GitHub user name", Prefs.get( USER_NAME, "tischi" ), columns );
		gd.addStringField( "GitHub repository", Prefs.get( REPOSITORY, "https://github.com/hci-unihd/antibodies-analysis-issues" ), columns );
		gd.addStringField( "GitHub access token", Prefs.get( ACCESS_TOKEN, "1234567890" ), columns );
		gd.addTextAreas( "", null, 10, columns );

		gd.showDialog();

		if ( gd.wasCanceled() ) return false;

		userName = gd.getNextString();
		repository = gd.getNextString();
		accessToken = gd.getNextString();
		issueBody = gd.getNextText();

		Prefs.set( USER_NAME, userName );
		Prefs.set( REPOSITORY, repository );
		Prefs.set( ACCESS_TOKEN, accessToken );

		return true;
	}

}
