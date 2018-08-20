package de.embl.cba.multipositionviewer;

import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Plugin(type = Command.class, menuPath = "Plugins>Screening>Plate View" )
public class MultiPositionViewerCommand implements Command
{
	@Parameter
	public LogService logService;

	@Parameter
	public CommandService commandService;

	@Parameter (label = "Input directory", style = "directory" )
	public File inputDirectory;

	@Parameter (label = "Only load files matching" )
	public String onlyLoadFilesMatching = ".*.tif";

//	@Parameter (label = "Number of IO threads" )
	public int numIoThreads = 1;

	public void run()
	{

		final ArrayList< File > fileList = Utils.getFileList( inputDirectory, onlyLoadFilesMatching );
		final String filenamePattern = Utils.getMultiPositionFilenamePattern( fileList.get( 0 ) );
		final Set< String > channelPatterns = Utils.getChannelPatterns( fileList, filenamePattern );

		final Iterator< String > channelIterator = channelPatterns.iterator();
		while ( channelIterator.hasNext() )
		{
			final ArrayList< File > channelFiles = Utils.filterFiles( fileList, channelIterator.next() );
			final MultiPositionImagesSource multiPositionImagesSource = new MultiPositionImagesSource( channelFiles, filenamePattern );
			int a = 1;
		}

		final CellFileMapsGenerator cellFileMapsGenerator = new CellFileMapsGenerator( inputDirectory.getAbsolutePath(), onlyLoadFilesMatching );
		final ArrayList< Map< String, File > > cellFileMaps = cellFileMapsGenerator.getCellFileMaps();
		final int[] siteDimensions = cellFileMapsGenerator.getSiteDimensions();
		final int[] wellDimensions = cellFileMapsGenerator.getWellDimensions();

		MultiPositionViewer multiPositionViewer = null;

		for ( int sourceIndex = 0; sourceIndex < cellFileMaps.size(); ++sourceIndex )
		{
			final CachedPlateViewImg cachedPlateViewImg = new CachedPlateViewImg( cellFileMaps.get( sourceIndex ), wellDimensions, siteDimensions, numIoThreads );

			if ( multiPositionViewer == null )
			{
				multiPositionViewer = new MultiPositionViewer( cachedPlateViewImg, numIoThreads );
			}
			else
			{
				multiPositionViewer.addChannel( cachedPlateViewImg );
			}
		}

	}



}

