package explore;

import java.util.List;
import java.util.Map;

import static de.embl.cba.plateviewer.table.Tables.stringColumnsFromHDF5;

public class ExploreTableLoadingFromHdf5
{
	public static void main( String[] args )
	{
		final Map< String, List< String > > columnNamesToStringColumns = stringColumnsFromHDF5( "/Users/tischer/Downloads/WellC01_PointC01_0004_ChannelDAPI,WF_GFP,TRITC,WF_Cy5_Seq0220 (1).h5", "tables/cell_segmentation/marker_corrected" );

	}
}
