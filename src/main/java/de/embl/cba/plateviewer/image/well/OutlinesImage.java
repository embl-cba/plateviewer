package de.embl.cba.plateviewer.image.well;

import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.image.channel.BDViewable;
import de.embl.cba.plateviewer.view.PlateViewerImageView;
import de.embl.cba.tables.color.ColorUtils;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.awt.*;
import java.util.function.BiConsumer;

public class OutlinesImage implements BDViewable
{
	public static final String IMAGE_NAME = "plate outlines";
	private Interval imageInterval;
	private RandomAccessibleInterval< FloatType > rai;
	private double[] contrastLimits;
	private final long[] wellDimensions;
	private RandomAccessibleIntervalSource< FloatType > source;
	private final double relativeWellBorderWidth;

	public OutlinesImage( PlateViewerImageView imageView, double relativeWellBorderWidth )
	{
		this.wellDimensions = imageView.getWellDimensions();
		this.imageInterval = Intervals.expand(
				imageView.getPlateInterval(),
				(int) ( wellDimensions[ 0 ] * relativeWellBorderWidth ) );
		this.relativeWellBorderWidth = relativeWellBorderWidth;

		contrastLimits = new double[ 2 ];

		createImage();
	}

	private void createImage( )
	{
		BiConsumer< Localizable, UnsignedByteType > biConsumer = ( l, t ) ->
		{
			final double[] distances = new double[ 2 ];
			for ( int d = 0; d < 2; d++ )
			{
				double ratio = 1.0 * l.getIntPosition( d ) / wellDimensions[ d ];
				distances[ d ] = Math.abs( ratio - Math.round( ratio ) );
			}

			final double distance = Math.min( distances[ 0 ], distances[ 1 ] );

			if ( distance < relativeWellBorderWidth )
			{
				t.set( 255 );
				//t.set( (int) ( 255.0 * ( relativeWellBorderWidth - distance ) / relativeWellBorderWidth ));
				return;
			}
			else
			{
				t.set( 0 );
			}
		};

		final FunctionRandomAccessible< FloatType > randomAccessible =
				new FunctionRandomAccessible( 2, biConsumer, UnsignedByteType::new );

		rai = Views.interval( randomAccessible, imageInterval );

		rai = Views.addDimension( rai, 0, 0 );

		source = new RandomAccessibleIntervalSource<>( rai, Util.getTypeFromInterval( rai ), IMAGE_NAME );

		contrastLimits[ 0 ] = 0;
		contrastLimits[ 1 ] = 255;
	}

	@Override
	public String getName()
	{
		return IMAGE_NAME;
	}

	@Override
	public ARGBType getColor()
	{
		return ColorUtils.getARGBType( Color.WHITE );
	}

	@Override
	public double[] getContrastLimits()
	{
		return contrastLimits;
	}

	@Override
	public RandomAccessibleInterval< ? > getRAI()
	{
		return rai;
	}

	@Override
	public Source< ? > getSource()
	{
		return source;
	}

	@Override
	public boolean isInitiallyVisible()
	{
		return true;
	}

	@Override
	public Metadata.Type getType()
	{
		return Metadata.Type.Image;
	}
}
