package de.embl.cba.plateviewer.channel;

public class ChannelProperties
{
	public boolean isInitiallyVisible = true;
	public String regExp;
	public String name;

	public ChannelProperties( String name, String regExp, boolean isInitiallyVisible )
	{
		this.isInitiallyVisible = isInitiallyVisible;
		this.regExp = regExp;
		this.name = name;
	}
}
