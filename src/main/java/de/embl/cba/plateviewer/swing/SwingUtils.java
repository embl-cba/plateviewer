package de.embl.cba.plateviewer.swing;

import javax.swing.*;
import java.awt.*;

public class SwingUtils
{
	public static JLabel getJLabel( String text )
	{
		final JLabel comp = new JLabel( text );
		comp.setPreferredSize( new Dimension( 170,10 ) );
		comp.setHorizontalAlignment( SwingConstants.LEFT );
		comp.setHorizontalTextPosition( SwingConstants.LEFT );
		comp.setAlignmentX( Component.LEFT_ALIGNMENT );
		return comp;
	}
}
