package de.embl.cba.plateviewer.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GitHubIssue
{
	public String title = "aaa";
	public String body = "bbb";

	public GitHubIssue( String title, String body, String[] labels )
	{
		this.title = title;
		this.body = body;
	}

	@Override
	public String toString()
	{
		final ObjectMapper objectMapper = new ObjectMapper();
		try
		{
			return objectMapper.writeValueAsString( this );
		} catch ( JsonProcessingException e )
		{
			e.printStackTrace();
			throw new RuntimeException( "Could not build Json string" );
		}
	}
}
