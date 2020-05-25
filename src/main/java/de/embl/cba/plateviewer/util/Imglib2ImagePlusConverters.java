package de.embl.cba.plateviewer.util;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.LUT;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Imglib2ImagePlusConverters
{
	public static CompositeImage createCompositeImage(
			Map< String, RandomAccessibleInterval< UnsignedShortType > > rais,
			ArrayList< double[] > displayRanges )
	{
		final Set< String > channelNames = rais.keySet();
		final ArrayList< RandomAccessibleInterval< UnsignedShortType > > channelRais = new ArrayList( rais.values() );

		final RandomAccessibleInterval< UnsignedShortType > stack = Views.stack( channelRais );
		ImagePlus imp = ImageJFunctions.wrap( stack, "Raw" );
		imp = new Duplicator().run( imp ); // duplicate: otherwise it is virtual and cannot be modified

		final CompositeImage compositeImage = new CompositeImage( imp );
		int channelIndex = 1;
		for ( String name : channelNames )
		{
			final LUT lut = compositeImage.createLutFromColor( Color.WHITE );
			compositeImage.setC( channelIndex );
			compositeImage.setChannelLut( lut );
			compositeImage.getStack().setSliceLabel( name, channelIndex );

			final double[] range = displayRanges.get( channelIndex - 1 );
			compositeImage.setDisplayRange( range[ 0 ], range[ 1 ] );
			channelIndex++;
		}

		compositeImage.setTitle( "Raw" );
		return compositeImage;
	}
}
