package de.embl.cba.plateviewer.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PlateLocation
{
	public String plateName = "";
	public String siteName = "";
	public double[] pixelLocation = new double[3];

	@Override
	public String toString()
	{
		final ObjectMapper mapper = new ObjectMapper();
		try
		{
			return mapper.writeValueAsString( this );
		} catch ( JsonProcessingException e )
		{
			e.printStackTrace();
			throw new RuntimeException( "Could not parse PlateLocation to String" );
		}
	}

}
