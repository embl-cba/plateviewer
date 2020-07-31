package develop;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import loci.formats.FormatException;

import java.io.IOException;
import java.util.List;

public class ReadCoronaHdf5
{
	public static void main(String[] args) throws FormatException, IOException
	{
//		String filePath = ReadCoronaHdf5.class.getResource( "../BATCHLIBHDF5/WellC01_PointC01_0000_ChannelDAPI,WF_GFP,TRITC,WF_Cy5_Seq0216.h5" ).getFile();
//
//		final boolean matches = Pattern.compile( NamingSchemes.PATTERN_CORONA_HDF5 ).matcher( filePath ).matches();
//
//		final IHDF5Reader hdf5Reader = HDF5Factory.openForReading( filePath );
//		final List< String > groupMembers = hdf5Reader.getGroupMembers( "/" );
//		final HDF5DataSetInformation information = hdf5Reader.getDataSetInformation( "pmap" );
//		//hdf5Reader.string().
//		final String dataType = information.getTypeInformation().toString();
//

		final String filePath = ReadCoronaHdf5.class.getResource( "../tmp/WellC01_PointC01_0000_ChannelDAPI,WF_GFP,TRITC,WF_Cy5_Seq0216.h5" ).getFile();

		final IHDF5Reader hdf5Reader = HDF5Factory.openForReading( filePath );
		final List< String > groupMembers = hdf5Reader.getGroupMembers( "/" );
		final String attr = hdf5Reader.string().getAttr( "raw", "foo" );
		final byte[] arrayAttr = hdf5Reader.int8().getArrayAttr( "raw", "L" );
	}
}
