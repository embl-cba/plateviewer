package de.embl.cba.plateviewer.image;

import java.util.ArrayList;

public interface MultiWellChannelFilesProvider
{
	ArrayList< SingleSiteChannelFile > getSingleSiteChannelFiles();

	ArrayList< String > getWellNames();
}
