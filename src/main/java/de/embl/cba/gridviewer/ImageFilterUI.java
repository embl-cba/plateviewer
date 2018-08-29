package de.embl.cba.gridviewer;

import ij.gui.GenericDialog;

public class ImageFilterUI
{

	public static ImageFilterSettings addSettingsViaUI( ImageFilterSettings settings  )
	{

		if ( settings.filterType.equals( ImageFilter.SIMPLE_SEGMENTATION ) )
		{
			settings = simpleSegmentationUI( settings );
		}
		else if ( settings.filterType.equals( ImageFilter.SUBTRACT_MEDIAN ) )
		{
			settings = medianSubtractionUI( settings );
		}

		return settings;
	}

	private static ImageFilterSettings medianSubtractionUI( ImageFilterSettings settings )
	{
		final GenericDialog gd = new GenericDialog(settings.filterType );
		gd.addNumericField("Radius", 1, 0, 5, "pixels" );
		gd.addNumericField("Offset", 0, 0, 5, "gray values" );
		gd.showDialog();
		if ( gd.wasCanceled() ) return null;
		settings.radius = (int) gd.getNextNumber();
		settings.offset = (double) gd.getNextNumber();
		return settings;
	}


	private static ImageFilterSettings simpleSegmentationUI( ImageFilterSettings settings )
	{
		final GenericDialog gd = new GenericDialog( settings.filterType );
		gd.addNumericField("Threshold", 1, 0, 5, "gray values" );
		gd.addNumericField("Minimal object size", 100, 0, 5, "pixels" );
		gd.showDialog();
		if ( gd.wasCanceled() ) return null;
		settings.threshold = (double ) gd.getNextNumber();
		settings.minObjectSize = (long) gd.getNextNumber();
		return settings;
	}

}
