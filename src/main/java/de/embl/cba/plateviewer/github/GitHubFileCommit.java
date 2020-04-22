package de.embl.cba.plateviewer.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class GitHubFileCommit
{
	public String message = "my commit message";
	public Map< String, String > committer = new HashMap<>(  );
	public String content = "Hello World";

	public GitHubFileCommit()
	{
		committer.put( "name", "tischi" );
		committer.put( "email", "tischitischer@gmail.com" );
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
