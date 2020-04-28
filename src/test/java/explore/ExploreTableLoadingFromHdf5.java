package explore;

import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

import java.util.List;

public class ExploreTableLoadingFromHdf5
{
	public static void main( String[] args )
	{
		final IHDF5Reader hdf5Reader = HDF5Factory.openForReading( "/Users/tischer/Downloads/WellC01_PointC01_0007_ChannelDAPI,WF_GFP,TRITC,WF_Cy5_Seq0223.h5" );
		final List< String > groupMembers = hdf5Reader.getGroupMembers( "/" );
		final String[] columns = hdf5Reader.string().readMDArray( "tables/data/columns" ).getAsFlatArray();
		final String[] cells = hdf5Reader.string().readMDArray( "tables/data/cells" ).getAsFlatArray();
//		final int[] dimensions = entries.dimensions();
//		final String[] flatArray = entries.getAsFlatArray();
	}
}
