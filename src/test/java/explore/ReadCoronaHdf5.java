package explore;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.plateviewer.PlateViewer;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.imagesources.NamingSchemes;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class ReadCoronaHdf5
{
	public static void main(String[] args) throws FormatException, IOException
	{
		String filePath = ReadCoronaHdf5.class.getResource( "../CORONA/WellC01_PointC01_0000_ChannelDAPI,WF_GFP,TRITC,WF_Cy5_Seq0216.h5" ).getFile();

		final boolean matches = Pattern.compile( NamingSchemes.PATTERN_CORONA ).matcher( filePath ).matches();

		final IHDF5Reader hdf5Reader = HDF5Factory.openForReading( filePath );
		final List< String > groupMembers = hdf5Reader.getGroupMembers( "/" );
		final HDF5DataSetInformation information = hdf5Reader.getDataSetInformation( "pmap" );
		//hdf5Reader.string().
		final String dataType = information.getTypeInformation().toString();
	}
}