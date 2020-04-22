package de.embl.cba.plateviewer.github;

import de.embl.cba.tables.Logger;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RESTCaller
{
	public static void call( String url, String requestMethod, String content, String accessToken )
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

				builder.append("\n");
			}

			System.out.println(builder);


			int responseCode = httpURLConnection.getResponseCode();
			if ( responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED )
			{
				// worked fine
			}
			else
			{
				Logger.error( "Unexpected response code: " + responseCode );
			}
		}
		catch( Exception e )
		{
			Logger.error( "Please see the error in the console" );
			System.err.println( e );
		}
	}
}
