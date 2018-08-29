package de.embl.cba.gridviewer;

import java.util.ArrayList;

public interface ImageSourcesGenerator
{
	ArrayList< ImageSource > getImageSources();

	public ArrayList< String > getWellNames();
}
