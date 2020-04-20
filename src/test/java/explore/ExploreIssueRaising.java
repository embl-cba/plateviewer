package explore;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.embl.cba.plateviewer.github.Issue;
import kong.unirest.*;

public class ExploreIssueRaising
{
	public static void main( String[] args )
	{
		// https://api.github.com/embl-cba/fiji-plugin-plateViewer/issues

		// https://api.github.com/repos/tischi/HTM_Explorer/issues
		// curl -i -u tischi -d '{"title": "A sample new issue", "body": "The user interface is upside down"}' https://api.github.com/repos/tischi/HTM_Explorer/issues
		//© 2020 GitHub, Inc.

		// https://api.github.com/repos/pengwynn/api-sandbox/issues

		// curl -i -u tischi:ad99337fffaaf428069ca2e6a7761abe5791399c -d '{"title": "aaaA sample new issue", "body": "The user interface is upside down"}' https://api.github.com/repos/tischi/HTM_Explorer/issues

		JsonObjectMapper mapper = new JsonObjectMapper();
		String json = mapper.writeValue( new Issue() );
		System.out.println("ResultingJSONstring = " + json);
		//System.out.println(json);


		final HttpResponse< String  > response =
				Unirest.post( "https://api.github.com/repos/tischi/HTM_Explorer/issues" )
					.header( "accept", "application/json" )
					.basicAuth( "tischi", "29b00ef73853f313e6aa54833a0ce44815fb1413" )
					.body( json )
				.asString();

		System.out.println( response.getBody() );
	}
}
