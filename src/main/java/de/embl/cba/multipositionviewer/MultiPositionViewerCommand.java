package de.embl.cba.multipositionviewer;

import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;

@Plugin(type = Command.class, menuPath = "Plugins>Screening>Multiposition Viewer" )
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

	MultiPositionViewer multiPositionViewer;

	public void run()
	{

		Utils.log( "Fetching list of files..." );
		final ArrayList< File > fileList = Utils.getFileList( inputDirectory, onlyLoadFilesMatching );
		Utils.log( "Number of files: " +  fileList.size() );

		final String namingScheme = Utils.getMultiPositionNamingScheme( fileList.get( 0 ) );
		Utils.log( "Detected naming scheme: " +  namingScheme );

		final ArrayList< String > channelPatterns = Utils.getChannelPatterns( fileList, namingScheme );

		for ( String channelPattern : channelPatterns )
		{
			Utils.log( "Adding channel: " + channelPattern );

			final ArrayList< File > channelFiles = Utils.filterFiles( fileList, channelPattern );

			final ImagesSource imagesSource = new ImagesSource( channelFiles, namingScheme, numIoThreads );

			addSourceToViewer( imagesSource );

		}

		createImageNavigatorUI( fileList, channelPatterns.get( 0 ) );

	}

	public void createImageNavigatorUI( ArrayList< File > fileList, String channelPattern )
	{
		final ArrayList< File > files = Utils.filterFiles( fileList, channelPattern );

		ArrayList< String > filenames = new ArrayList<>(  );
		for ( File file : files )
		{
			filenames.add( file.getName() );
		}

		final MultiPositionViewerUI multiPositionViewerUI = new MultiPositionViewerUI( filenames, multiPositionViewer );
	}

	public void addSourceToViewer( ImagesSource imagesSource )
	{
		if ( multiPositionViewer == null )
		{
			multiPositionViewer = new MultiPositionViewer( imagesSource, numIoThreads );
		}
		else
		{
			multiPositionViewer.addSourceToBdv( imagesSource );
		}
	}


}

