package de.embl.cba.plateviewer;

import de.embl.cba.plateviewer.table.IntervalType;
import de.embl.cba.plateviewer.table.TableSource;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, menuPath = "Plugins>Screening>PlateViewer Advanced..." )
public class PlateViewerAdvancedCommand extends PlateViewerCommand
{
	@Parameter (label = "Additional images directory", style = "directory" )
	public File additionalImagesDirectory;

	@Parameter (label = "Images table file", style = "file" )
	public File imageTableFile;

	public void run()
	{
		final PlateViewerInitializer plateViewerInitializer = new PlateViewerInitializer( imagesDirectory, filePattern, 4, includeSubFolders );

		if ( additionalImagesDirectory != null & additionalImagesDirectory.exists())
			plateViewerInitializer.addInputImagesDirectory( additionalImagesDirectory.getAbsolutePath() );

		if ( imageTableFile != null && imageTableFile.exists() )
		{
			final TableSource tableSource = new TableSource();
			tableSource.filePath = imageTableFile.getAbsolutePath();
			tableSource.intervalType = IntervalType.Sites;
			plateViewerInitializer.setSiteTableSource( tableSource );
		}

		plateViewerInitializer.run();
	}

}

