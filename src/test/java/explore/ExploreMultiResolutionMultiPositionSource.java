package explore;

import de.embl.cba.plateviewer.io.FileUtils;
import de.embl.cba.plateviewer.imagesources.MultiResolutionSource;
import de.embl.cba.plateviewer.view.PlateViewerImageView;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.imagesources.ImagesSource;
import net.imglib2.RandomAccessibleInterval;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class ExploreMultiResolutionMultiPositionSource
{
	public static void main( String[] args )
	{

		final String inputDirectory = ExploreMultiResolutionMultiPositionSource.class.getResource(
				"../ALMF-EMBL-ZeroBased-P2-S4-C2-T1" ).getFile();


		final List< File > fileList = FileUtils.getFileList(
				new File( inputDirectory ),
				".*.tif" );

		final String namingScheme = PlateViewerImageView.getNamingScheme( fileList );

		final List< String > channelPatterns =
				Utils.getChannelPatterns( fileList, namingScheme );

		final List< File > channelFiles = FileUtils.filterFiles(
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
