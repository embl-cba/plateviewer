package de.embl.cba.plateviewer.imagesources;

import java.util.ArrayList;

public interface ImageSourcesGenerator
{
	ArrayList< ImageSource > getImageSources();

	public ArrayList< String > getWellNames();
}
