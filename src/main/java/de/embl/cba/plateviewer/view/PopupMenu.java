package de.embl.cba.plateviewer.view;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;

import javax.swing.*;
import java.awt.event.ActionListener;

public class PopupMenu
{
	private JPopupMenu popup;

	public PopupMenu(  )
	{
		createPopupMenu();
	}

	private void createPopupMenu()
	{
		popup = new JPopupMenu();
	}

	private void addPopupLine() {
		popup.addSeparator();
	}

	public void addPopupAction( String actionName, ActionListener actionListener ) {

		JMenuItem menuItem = new JMenuItem(actionName);
		menuItem.addActionListener( actionListener );
		popup.add(menuItem);
	}

	public void show( JComponent display, int x, int y )
	{
		popup.show( display, x, y );
	}
}
