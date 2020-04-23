package de.embl.cba.plateviewer.github;

import java.io.IOException;
import java.net.URISyntaxException;

public class UrlOpener
{
	public static void openUrl(String url) throws IOException, URISyntaxException
	{
		if(java.awt.Desktop.isDesktopSupported() ) {
			java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

			if(desktop.isSupported(java.awt.Desktop.Action.BROWSE) ) {
				java.net.URI uri = new java.net.URI(url);
				desktop.browse(uri);
			}
		}
	}
}
