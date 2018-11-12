package de.embl.cba.gridviewer.bdv;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.BoundedValueDouble;

import java.util.ArrayList;

public class BrightnessUpdateListener implements BoundedValueDouble.UpdateListener
{
 	final private ArrayList< ConverterSetup > converterSetups;
 	final private BoundedValueDouble min;
	final private BoundedValueDouble max;

	public BrightnessUpdateListener( BoundedValueDouble min,
									 BoundedValueDouble max,
									 ArrayList< ConverterSetup > converterSetups )
	{
		this.min = min;
		this.max = max;
		this.converterSetups = converterSetups;
	}

	public BrightnessUpdateListener( BoundedValueDouble min,
									 BoundedValueDouble max,
									 ConverterSetup converterSetup )
	{
		this.min = min;
		this.max = max;

		converterSetups = new ArrayList<>(  );
		converterSetups.add( converterSetup );
	}

	@Override
	public void update()
	{
		for ( ConverterSetup converterSetup : converterSetups )
		{
			converterSetup.setDisplayRange( min.getCurrentValue(), max.getCurrentValue() );
		}
	}
}
