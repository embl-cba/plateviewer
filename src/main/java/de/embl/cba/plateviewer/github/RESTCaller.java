package de.embl.cba.plateviewer.github;

import de.embl.cba.tables.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RESTCaller
{
	private int issueNumber;
	private String status;

	public RESTCaller()
	{
	}

	public void call( String url, String requestMethod, String content, String accessToken )
	{
		try
		{
			URL obj = new URL( url );
			HttpURLConnection httpURLConnection = ( HttpURLConnection ) obj.openConnection();

			httpURLConnection.setRequestMethod( requestMethod );
			httpURLConnection.setRequestProperty( "Content-Type", "application/json" );
			httpURLConnection.setRequestProperty( "Authorization", "Token " + accessToken );

			httpURLConnection.setDoOutput( true );
			DataOutputStream wr = new DataOutputStream( httpURLConnection.getOutputStream() );
			wr.writeBytes( content );
			wr.flush();
			wr.close();

			parseResponse( httpURLConnection );
		}
		catch( Exception e )
		{
			Logger.error( "Please see the error in the console" );
			System.err.println( e );
		}
	}

	private void parseResponse( HttpURLConnection httpURLConnection ) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		builder.append(httpURLConnection.getResponseCode())
				.append(" ")
				.append(httpURLConnection.getResponseMessage())
				.append("\n");

		Map<String, List<String> > map = httpURLConnection.getHeaderFields();
		for (Map.Entry<String, List<String>> entry : map.entrySet())
		{
			if (entry.getKey() == null)
				continue;
			builder.append( entry.getKey())
					.append(": ");

			List<String> headerValues = entry.getValue();
			Iterator<String> it = headerValues.iterator();
			if (it.hasNext()) {
				builder.append(it.next());

				while (it.hasNext()) {
					builder.append(", ")
							.append(it.next());
				}
			}

			if (entry.getKey().equals( "Location" ) )
			{
				final String[] split = entry.getValue().get( 0 ).split( "/" );
				issueNumber = Integer.parseInt( split[ split.length - 1 ] );
			}

			if (entry.getKey().equals( "Status" ) )
			{
				status = entry.getValue().get( 0 );
			}


			builder.append("\n");
		}

		System.out.println(builder);

		int responseCode = httpURLConnection.getResponseCode();
		if ( ! ( responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED ) )
		{
			Logger.error( "Unexpected response code: " + responseCode + "\n"+status);
		}
	}

	public int getIssueNumber()
	{
		return issueNumber;
	}
}
