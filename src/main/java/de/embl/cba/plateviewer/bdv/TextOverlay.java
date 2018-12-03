package de.embl.cba.plateviewer.bdv;

import java.awt.*;

public class TextOverlay
{
	final String text;
	final double[] position;
	final int size;
	final Color color;

	public TextOverlay( String text, double[] position, int size, Color color )
	{
		this.text = text;
		this.position = position;
		this.size = size;
		this.color = color;
	}
}
