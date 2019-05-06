package explore;

import de.embl.cba.plateviewer.FileUtils;
import de.embl.cba.plateviewer.MultiResolutionSource;
import de.embl.cba.plateviewer.PlateViewer;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.imagesources.ImagesSource;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ExploreMultiResolutionMultiPositionSource
{
	public static void main( String[] args )
	{

		final String inputDirectory = ExploreMultiResolutionMultiPositionSource.class.getResource(
				"../ALMF-EMBL-P2-S4-C2-T1" ).getFile();


		final ArrayList< File > fileList = FileUtils.getFileList(
				new File( inputDirectory ),
				".*.tif" );

		final String namingScheme = PlateViewer.getNamingScheme( fileList );

		final ArrayList< String > channelPatterns =
				Utils.getChannelPatterns( fileList, namingScheme );

		final ArrayList< File > channelFiles = FileUtils.filterFiles(
				fileList,
				channelPatterns.get( 0 ) );

		final ImagesSource imagesSource =
				new ImagesSource( channelFiles, namingScheme, 1 );

		final RandomAccessibleInterval img = imagesSource.getCachedCellImg();

		final HashMap< Double, RandomAccessibleInterval > scaleToRai = new HashMap<>();

		scaleToRai.put( 1.0, img );

		final MultiResolutionSource source = new MultiResolutionSource( scaleToRai );



	}


}
