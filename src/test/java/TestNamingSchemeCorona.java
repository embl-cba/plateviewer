import de.embl.cba.plateviewer.PlateViewer;
import de.embl.cba.plateviewer.table.JTableView;
import de.embl.cba.tables.Tables;
import de.embl.cba.tables.view.TableRowsTableView;
import net.imagej.ImageJ;

import javax.swing.*;

public class TestNamingSchemeCorona
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		final JTable jTable = Tables.loadTable( TestNamingSchemeCorona.class.getResource( "CORONA/default.csv" ).getFile() );

		new JTableView( jTable ).showTable();

//		new PlateViewer(
//				TestNamingSchemeCorona.class.getResource( "CORONA" ).getFile(),
//				".*.h5",
//				4 );

//		new PlateViewer(
//				"/Volumes/kreshuk/pape/Work/data/covid-antibodies/data-processed/20200405_test_images/",
//				".*.h5",
//				4 );

	}
}
