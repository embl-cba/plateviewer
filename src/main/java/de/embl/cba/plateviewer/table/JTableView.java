package de.embl.cba.plateviewer.table;

import de.embl.cba.tables.Tables;
import de.embl.cba.tables.color.ColumnColoringModelCreator;

import javax.swing.*;
import java.awt.*;

public class JTableView extends JPanel
{
	private JTable table;
	private JScrollPane scrollPane;
	private JFrame frame;

	public JTableView( JTable table )
	{
		super( new GridLayout(1, 0 ) );
		this.table = table;
	}

	public void showTable()
	{
		table.setPreferredScrollableViewportSize( new Dimension(500, 200) );
		table.setFillsViewportHeight( true );
		table.setAutoCreateRowSorter( true );
		table.setRowSelectionAllowed( true );
		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		scrollPane = new JScrollPane(
				table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add( scrollPane );

		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

		updateUI();

		frame = new JFrame( "Table" );

		this.setOpaque( true ); //content panes must be opaque
		frame.setContentPane( this );

		//Display the window.
		frame.pack();
		frame.setVisible( true );
	}
}
