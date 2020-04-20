package de.embl.cba.plateviewer.github;

import de.embl.cba.plateviewer.PlateViewer;
import ij.Prefs;
import ij.gui.GenericDialog;
import kong.unirest.HttpResponse;
import kong.unirest.JsonObjectMapper;
import kong.unirest.Unirest;

public class IssueRaiser
{

	public static final String USER_NAME = "PlateViewer.GitHub user name";
	public static final String REPOSITORY = "PlateViewer.GitHub repository";
	public static final String ACCESS_TOKEN = "PlateViewer.GitHub access token";
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
		String issueJson = createIssueJson();

		String url = getApiUrl();

		final HttpResponse< String  > response =
				Unirest.post( url )
					.header( "accept", "application/json" )
					.basicAuth( userName, accessToken )
					.body( issueJson )
				.asString();

		System.out.println( response.getBody() );
	}

	public String getApiUrl()
	{
		String url = repository.replace( "github.com", "api.github.com/repos" );
		if ( url.endsWith( "/" ) ) url += "issues";
		else url += "/issues";
		return url;
	}

	public String createIssueJson()
	{
		final Issue issue = new Issue();
		issue.title = "Issue in plate " + plateName;

		JsonObjectMapper mapper = new JsonObjectMapper();

		final PlateLocation plateLocation = new PlateLocation();
		plateLocation.plateName = plateName;
		plateLocation.siteName = siteName;
		plateLocation.xyPixelLocation = new int[]{x, y};
		issue.body = mapper.writeValue( plateLocation );
		issue.body += "\n" + issueBody;
		return mapper.writeValue( issue );
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
