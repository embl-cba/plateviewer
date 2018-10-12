package de.embl.cba.gridviewer.commands;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvOverlay;
import de.embl.cba.gridviewer.Utils;
import de.embl.cba.gridviewer.bdv.BdvImageNamesOverlay;
import de.embl.cba.gridviewer.imagesources.ImagesSource;
import de.embl.cba.gridviewer.viewer.MultiPositionViewer;
import de.embl.cba.gridviewer.viewer.MultiPositionViewerUI;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;

@Plugin(type = Command.class, menuPath = "Plugins>Screening>Plate viewer" )
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

		Utils.log( "Fetching files..." );
		final ArrayList< File > fileList = Utils.getFileList( inputDirectory, onlyLoadFilesMatching );
		Utils.log( "Number of files: " +  fileList.size() );

		final String namingScheme = Utils.getNamingScheme( fileList.get( 0 ) );
		Utils.log( "Detected naming scheme: " +  namingScheme );

		final ArrayList< String > channelPatterns = Utils.getChannelPatterns( fileList, namingScheme );

		addChannelsToViewer( fileList, namingScheme, channelPatterns );

		addImageNamesOverlay();

		// TODO: wellNames Overlay

		new MultiPositionViewerUI( multiPositionViewer );

	}

	public void addChannelsToViewer( ArrayList< File > fileList, String namingScheme, ArrayList< String > channelPatterns )
	{
		for ( String channelPattern : channelPatterns )
		{
			Utils.log( "Adding channel: " + channelPattern );

			final ArrayList< File > channelFiles = Utils.filterFiles( fileList, channelPattern );

			final ImagesSource imagesSource = new ImagesSource( channelFiles, namingScheme, numIoThreads );
			imagesSource.setName( channelPattern );
			addSourceToViewer( imagesSource );

		}
	}

	public void addImageNamesOverlay()
	{
		BdvOverlay bdvOverlay = new BdvImageNamesOverlay( multiPositionViewer.getImagesSources());
		BdvFunctions.showOverlay( bdvOverlay, "names overlay", BdvOptions.options().addTo( multiPositionViewer.getBdv() ) );
	}

	public void addSourceToViewer( ImagesSource imagesSource )
	{
		if ( multiPositionViewer == null )
		{
			multiPositionViewer = new MultiPositionViewer( imagesSource, numIoThreads );
		}
		else
		{
			multiPositionViewer.addSource( imagesSource );
		}
	}


}

