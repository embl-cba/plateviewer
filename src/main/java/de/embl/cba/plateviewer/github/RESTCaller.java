package de.embl.cba.plateviewer.github;

import de.embl.cba.tables.Logger;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RESTCaller
{
	public static void call( String url, String requestMethod, String content, String accessToken )
	{
		try
		{
			URL obj = new URL( url );
			HttpURLConnection con = ( HttpURLConnection ) obj.openConnection();

			con.setRequestMethod( requestMethod );
			con.setRequestProperty( "Content-Type", "application/json" );
			con.setRequestProperty( "Authorization", "Token " + accessToken );

			con.setDoOutput( true );
			DataOutputStream wr = new DataOutputStream( con.getOutputStream() );
			wr.writeBytes( content );
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
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
