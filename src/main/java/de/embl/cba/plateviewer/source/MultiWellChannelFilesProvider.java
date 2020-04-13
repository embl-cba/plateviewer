package de.embl.cba.plateviewer.source;

import java.util.ArrayList;

public interface MultiWellChannelFilesProvider
{
	ArrayList< SingleSiteChannelFile > getSingleSiteChannelFiles();

	ArrayList< String > getWellNames();
}
