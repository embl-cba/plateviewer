package de.embl.cba.plateviewer.github;

import de.embl.cba.plateviewer.io.JpegOutputStreamWriter;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

public class IssueRaiser
{
	public static final String USER_NAME = "PlateViewer.GitHub user name";
	public static final String REPOSITORY = "PlateViewer.GitHub repository";
	public static final String ACCESS_TOKEN = "PlateViewer.GitHub access token";
	public static final String ERROR = "Something went wrong raising the issue:\n";
	public static final String SEP = " - ";
	private String userName;
	private String repository;
	private String accessToken;
	private String issueBody;
	private PlateLocation plateLocation;
	private ImagePlus screenShot;
	private String issueTitle;
	private String[] labels;
	private boolean isOpenIssue;

	public void showPlateIssueDialogAndCreateIssue( PlateLocation plateLocation )
	{
		showPlateIssueDialogAndCreateIssue( plateLocation, null );
	}

	public void openIssueInBrowser( int issue )
	{
		try
		{
			// TODO: Use my combineUrl utils from PlatyBrowser!
			if ( ! repository.endsWith( "/" ) ) repository += "/";
			UrlOpener.openUrl( repository + "issues/" + issue );
		} catch ( IOException e )
		{
			e.printStackTrace();
		} catch ( URISyntaxException e )
		{
			e.printStackTrace();
		}
	}

	public void showPlateIssueDialogAndCreateIssue( PlateLocation plateLocation, ImagePlus screenShot )
	{
		this.plateLocation = plateLocation;
		this.screenShot = screenShot;

		if ( ! showDialog() ) return;

		final int issue = postPlateIssue();

		if ( isOpenIssue )
		{
			new Thread( () -> {
				IJ.wait( 3000 );
				openIssueInBrowser( issue );
			} ).start();
		}
	}

	private int postPlateIssue()
	{
		final GitHubIssue githubIssue = createPlateGithubIssue();
		return postIssue( repository, accessToken, githubIssue, screenShot );
	}

	public int postIssue( String repository, String accessToken, GitHubIssue issue )
	{
		return postIssue( repository, accessToken, issue, null );
	}

	public int postIssue( String repository, String accessToken, GitHubIssue issue, ImagePlus imp )
	{
		if ( imp != null )
		{
			final String path = commitImagePlus( repository, accessToken, imp );
			addImageToIssue( repository, issue, path );
		}

		String url = getIssueApiUrl( repository );
		final RESTCaller restCaller = new RESTCaller();
		restCaller.call( url, "POST", issue.toString(), accessToken );
		final int issueNumber = restCaller.getIssueNumber();

		return issueNumber;
	}

	public void addImageToIssue( String repository, GitHubIssue issue, String path )
	{
		if ( ! repository.endsWith( "/" ) ) repository += "/";
		issue.body += "\n\n[Screenshot]("+repository+path+")";
	}

	public String commitImagePlus( String repository, String accessToken, ImagePlus imp )
	{
		final String imageJpegBase64String = JpegOutputStreamWriter.createBase64String( imp, 0.90F );

		final String path = createImagePath( imp );

		final GitHubFileCommitter fileCommitter =
				new GitHubFileCommitter(
						repository,
						accessToken,
						path );

		fileCommitter.commitStringAsFile( "Test commit an image", imageJpegBase64String );

		return "blob/master/" + path;
	}

	public String createImagePath( ImagePlus imp )
	{
		final Random random = new Random( System.currentTimeMillis() );
		return "screenshots/" + imp.getTitle() + "-" + ( random.nextInt() & Integer.MAX_VALUE ) + ".jpg";
	}

	private String getIssueApiUrl( String repository )
	{
		String url = repository.replace( "github.com", "api.github.com/repos" );
		if ( url.endsWith( "/" ) ) url += "issues";
		else url += "/issues";
		return url;
	}

	private GitHubIssue createPlateGithubIssue()
	{
		final String title = issueTitle + SEP + plateLocation.plateName + SEP + plateLocation.siteName;
		String body = plateLocation.toString() + "\n\n" + issueBody;

		return new GitHubIssue( title, body, labels );
	}

	public boolean showDialog()
	{
		final GenericDialog gd = new GenericDialog( "Report an issue" );
		final int columns = 80;

		//gd.addStringField( "GitHub user name", Prefs.get( USER_NAME, "tischi" ), columns );
		gd.addStringField( "GitHub repository", Prefs.get( REPOSITORY, "https://github.com/hci-unihd/antibodies-analysis-issues" ), columns );
		gd.addStringField( "GitHub access token", Prefs.get( ACCESS_TOKEN, "1234567890" ), columns );
		gd.addStringField( "Issue title", "", columns );
		gd.addChoice( "Issue label", new String[]{"test", "sample", "acquisition", "segmentation", "analysis", "other"}, "other" );
		gd.addTextAreas( "", null, 10, columns );
		gd.addCheckbox( "Open posted issue", false );

		gd.showDialog();

		if ( gd.wasCanceled() ) return false;

		//userName = gd.getNextString();
		repository = gd.getNextString();
		accessToken = gd.getNextString();
		issueTitle = gd.getNextString();

		labels = new String[]{gd.getNextChoice()};
		issueBody = gd.getNextText();
		isOpenIssue = gd.getNextBoolean();

		//Prefs.set( USER_NAME, userName );
		Prefs.set( REPOSITORY, repository );
		Prefs.set( ACCESS_TOKEN, accessToken );

		return true;
	}

}
