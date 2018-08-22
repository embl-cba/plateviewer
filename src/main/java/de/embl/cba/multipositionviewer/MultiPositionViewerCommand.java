package de.embl.cba.multipositionviewer;

import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

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

		final Set< String > channelPatterns = Utils.getChannelPatterns( fileList, namingScheme );

		for ( String channelPattern : channelPatterns )
		{
			Utils.log( "Adding channel: " + channelPattern );

			final ArrayList< File > channelFiles = Utils.filterFiles( fileList, channelPattern );

			final MultiPositionImagesSource multiPositionImagesSource = new MultiPositionImagesSource( channelFiles, namingScheme, numIoThreads );

			addSourceToViewer( multiPositionImagesSource );

		}

	}

	public void addSourceToViewer( MultiPositionImagesSource multiPositionImagesSource )
	{
		if ( multiPositionViewer == null )
		{
			multiPositionViewer = new MultiPositionViewer( multiPositionImagesSource, numIoThreads );
		}
		else
		{
			multiPositionViewer.addSourceToBdv( multiPositionImagesSource );
		}
	}


}

