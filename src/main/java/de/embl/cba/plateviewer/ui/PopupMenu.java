package de.embl.cba.plateviewer.ui;

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
