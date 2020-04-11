package de.embl.cba.plateviewer.view;

import de.embl.cba.tables.Tables;
import de.embl.cba.tables.tablerow.TableRow;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PlateViewerTableView< T extends TableRow > extends JPanel
{
	private JTable jTable;
	private JScrollPane jScrollPane;
	private JFrame jFrame;

	public PlateViewerTableView( List< T > tableRows )
	{
		jTable = Tables.jTableFromTableRows( tableRows );
	}

	public void showTable()
	{
		jTable.setPreferredScrollableViewportSize( new Dimension(500, 200) );
		jTable.setFillsViewportHeight( true );
		jTable.setAutoCreateRowSorter( true );
		jTable.setRowSelectionAllowed( true );
		jTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		jScrollPane = new JScrollPane(
				jTable,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.add( jScrollPane );

		jTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

		this.updateUI();

		jFrame = new JFrame( "Table" );

		this.setOpaque( true ); //content panes must be opaque
		jFrame.setContentPane( this );

		//Display the window.
		jFrame.pack();
		jFrame.setVisible( true );
	}
}
