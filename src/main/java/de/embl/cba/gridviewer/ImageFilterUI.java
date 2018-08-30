package de.embl.cba.gridviewer;

import ij.gui.GenericDialog;

public class ImageFilterUI
{

	public ImageFilterUI()
	{
	}

	public static ImageFilterSettings addSettingsViaUI( ImageFilterSettings settings  )
	{

		if ( settings.filterType.equals( ImageFilter.SIMPLE_SEGMENTATION ) )
		{
			settings = simpleSegmentationUI( settings );
		}
		else if ( settings.filterType.equals( ImageFilter.MEDIAN_ABSOLUTE_DEVIATION )
				|| settings.filterType.equals( ImageFilter.MEDIAN_DEVIATION ))
		{
			settings = medianSubtractionUI( settings );
		}

		return settings;
	}

	private static ImageFilterSettings maxMinusMinUI( ImageFilterSettings settings )
	{
		final GenericDialog gd = new GenericDialog( settings.filterType );
		gd.addNumericField("Radius", settings.radius, 0, 5, "pixels" );
		gd.showDialog();
		if ( gd.wasCanceled() ) return null;
		settings.radius = (int) gd.getNextNumber();
		return settings;
	}

	private static ImageFilterSettings medianSubtractionUI( ImageFilterSettings settings )
	{
		final GenericDialog gd = new GenericDialog(settings.filterType );
		gd.addNumericField("Radius", settings.radius , 0, 5, "pixels" );
		gd.addNumericField("Offset", settings.offset, 0, 5, "gray values" );
		gd.showDialog();
		if ( gd.wasCanceled() ) return null;
		settings.radius = (int) gd.getNextNumber();
		settings.offset = (double) gd.getNextNumber();
		return settings;
	}


	private static ImageFilterSettings simpleSegmentationUI( ImageFilterSettings settings )
	{
		final GenericDialog gd = new GenericDialog( settings.filterType );
		gd.addNumericField("Threshold", settings.threshold , 0, 5, "gray values" );
		gd.addNumericField("Minimal object size", settings.minObjectSize, 0, 5, "pixels" );
		gd.showDialog();
		if ( gd.wasCanceled() ) return null;
		settings.threshold = (double ) gd.getNextNumber();
		settings.minObjectSize = (long) gd.getNextNumber();
		return settings;
	}

}
