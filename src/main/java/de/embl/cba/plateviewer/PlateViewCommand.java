package de.embl.cba.plateviewer;

import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

@Plugin(type = Command.class, menuPath = "Plugins>Screening>Plate View" )
public class PlateViewCommand implements Command
{
	@Parameter
	public LogService logService;

	@Parameter
	public CommandService commandService;

	@Parameter (label = "Input directory", style = "directory" )
	public File inputDirectory;

	@Parameter (label = "Only load files matching" )
	public String fileNameRegExp = ".*.tif";



	public void run()
	{

		final CellFileMapsGenerator cellFileMapsGenerator = new CellFileMapsGenerator( inputDirectory.getAbsolutePath(), fileNameRegExp );
		final ArrayList< Map< String, File > > cellFileMaps = cellFileMapsGenerator.getCellFileMaps();
		final int[] siteDimensions = cellFileMapsGenerator.getSiteDimensions();
		final int[] wellDimensions = cellFileMapsGenerator.getWellDimensions();

		PlateView plateView = null;

		for ( int channel = 0; channel < cellFileMaps.size(); ++channel )
		{
			final CachedPlateViewImg cachedPlateViewImg = new CachedPlateViewImg( cellFileMaps.get( channel ), wellDimensions, siteDimensions );

			if ( channel == 0 )
			{
				plateView = new PlateView( cachedPlateViewImg );
			}
			else
			{
				plateView.addChannel( cachedPlateViewImg );
			}
		}

	}



}

