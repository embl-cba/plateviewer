import de.embl.cba.plateviewer.table.*;
import de.embl.cba.plateviewer.view.PlateViewerTableView;
import net.imagej.ImageJ;

import java.util.List;

public class TestNamingSchemeCorona
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		final List< DefaultImageNameTableRow > imageNameTableRows = ImageNameTableRows.imageNameTableRowsFromFilePath( TestNamingSchemeCorona.class.getResource( "CORONA/default.csv" ).getFile() );

		new PlateViewerTableView( imageNameTableRows ).showTable();

//		final JTable jTable = Tables.loadTable( TestNamingSchemeCorona.class.getResource( "CORONA/default.csv" ).getFile() );
//
//		new JTableView( jTable ).showTable();

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
