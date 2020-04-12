package de.embl.cba.plateviewer.source;

import java.util.ArrayList;

public interface ChannelSourcesGenerator
{
	ArrayList< ChannelSource > getChannelSources();

	public ArrayList< String > getWellNames();
}
