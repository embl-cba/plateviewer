package de.embl.cba.plateviewer.location;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LocationInformation
{
	public String plateName = "";
	public String siteName = "";
	public double[] pixelLocation;
	public String analysisVersion = "?";

	public LocationInformation( String plateName, String siteName, double[] pixelLocation )
	{
		this.plateName = plateName;
		this.siteName = siteName;
		this.pixelLocation = pixelLocation;
	}

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

	public void setAnalysisVersion( String analysisVersion )
	{
		this.analysisVersion = analysisVersion;
	}
}
