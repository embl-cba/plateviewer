package de.embl.cba.gridviewer;

import ij.gui.GenericDialog;

public class ImageFilterUI
{

	public ImageFilterUI()
	{
	}

	public static ImageFilterSettings addSettingsUI( ImageFilterSettings settings  )
	{
		if ( settings.filterType.equals( ImageFilter.SIMPLE_SEGMENTATION ) )
		{
			settings = simpleSegmentationUI( settings );
		}
		else if ( settings.filterType.equals( ImageFilter.MEDIAN_DEVIATION ) )
		{
			settings = medianDeviationUI( settings );
		}
		else if ( settings.filterType.equals( ImageFilter.INFORMATION ) )
		{
			settings = radiusUI( settings );
		}

		return settings;
	}

	private static ImageFilterSettings radiusUI( ImageFilterSettings settings )
	{
		final GenericDialog gd = new GenericDialog( settings.filterType );
		gd.addNumericField("Radius", settings.radius, 0, 5, "pixels" );
		gd.showDialog();
		if ( gd.wasCanceled() ) return null;
		settings.radius = (int) gd.getNextNumber();
		return settings;
	}


	private static ImageFilterSettings medianDeviationUI( ImageFilterSettings settings )
	{
		final GenericDialog gd = new GenericDialog(settings.filterType );
		gd.addNumericField("Radius", settings.radius , 0, 5, "pixels" );
		gd.addNumericField("Offset", settings.offset, 0, 5, "gray values" );
		gd.addCheckbox("Divide by sqrt(median)", settings.normalize );
		gd.addNumericField("Factor", settings.factor, 2, 5, "" );
		gd.showDialog();
		if ( gd.wasCanceled() ) return null;
		settings.radius = (int) gd.getNextNumber();
		settings.offset = (double) gd.getNextNumber();
		settings.normalize = gd.getNextBoolean();
		settings.factor = gd.getNextNumber();
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
