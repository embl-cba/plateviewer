package de.embl.cba.plateviewer.github;

import ij.Prefs;
import ij.gui.GenericDialog;

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
	private PlateLocation plateLocation;

	public void showDialogAndCreateIssue( PlateLocation plateLocation )
	{
		this.plateLocation = plateLocation;

		if ( ! showDialog() ) return;

		postIssue();
	}

	public void postIssue()
	{
		String issueJson = createIssueJson();
		String url = getIssueApiUrl();

		RESTCaller.call( url, "POST", issueJson, accessToken );
	}

	public String getIssueApiUrl()
	{
		String url = repository.replace( "github.com", "api.github.com/repos" );
		if ( url.endsWith( "/" ) ) url += "issues";
		else url += "/issues";
		return url;
	}

	public String createIssueJson()
	{
		final GitHubIssue gitHubIssue = new GitHubIssue();
		gitHubIssue.title = "Plate " + plateLocation.plateName + " Site " + plateLocation.siteName;
		gitHubIssue.body = plateLocation.toString();
		gitHubIssue.body += "\n\n" + issueBody;
		return gitHubIssue.toString();
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
