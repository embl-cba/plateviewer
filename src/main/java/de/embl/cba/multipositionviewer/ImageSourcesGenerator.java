package de.embl.cba.multipositionviewer;

import java.util.ArrayList;

public interface ImageSourcesGenerator
{
	ArrayList< ImageSource > getImageSources();

	public ArrayList< String > getWellNames();
}
