package de.embl.cba.plateviewer.github;

public class GitHubFileCommitter
{
	private String repository;
	private String accessToken;
	private String path;

	public GitHubFileCommitter( String repository, String accessToken, String path )
	{
		this.repository = repository;
		this.accessToken = accessToken;
		this.path = path;
	}

	public void commitStringAsFile( String message, String base64String )
	{
		final GitHubFileCommit fileCommit = new GitHubFileCommit( message, base64String );
		String url = createFileCommitApiUrl( path );
		final String requestMethod = "PUT";
		final String json = fileCommit.toString();

		new RESTCaller().call( url, requestMethod, json, accessToken );
	}

	public String createFileCommitApiUrl( String path )
	{
		String url = repository.replace( "github.com", "api.github.com/repos" );
		if ( ! url.endsWith( "/" ) ) url += "/";
		if ( ! path.startsWith( "/" ) ) path = "/" + path;
		url += "contents" + path;
		return url;
	}
}
