package de.embl.cba.plateviewer.source.plate;

import bdv.util.BdvOverlay;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.source.channel.AbstractBdvViewable;
import de.embl.cba.plateviewer.PlateViewer;
import de.embl.cba.tables.color.ColorUtils;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.awt.*;
import java.util.function.BiConsumer;

public class WellAndSiteOutlinesSource extends AbstractBdvViewable
{
	public static final String IMAGE_NAME = "plate outlines";
	private Interval plateInterval;
	private RandomAccessibleInterval< UnsignedByteType > rai;
	private double[] contrastLimits;
	private final long[] wellDimensions;
	private RandomAccessibleIntervalSource< UnsignedByteType > source;
	private final double relativeWellBorderWidth;
	private final long[] siteDimensions;
	private final double relativeSiteBorderWidth;
	private VoxelDimensions voxelDimensions;

	public WellAndSiteOutlinesSource( PlateViewer plateViewer, double relativeWellBorderWidth, double relativeSiteBorderWidth )
	{
		wellDimensions = plateViewer.getWellDimensions();
		siteDimensions = plateViewer.getSiteDimensions();
		voxelDimensions = plateViewer.getVoxelDimensions();
		this.relativeWellBorderWidth = relativeWellBorderWidth;
		this.relativeSiteBorderWidth = relativeSiteBorderWidth;
		plateInterval = Intervals.expand( plateViewer.getPlateInterval(),
				(int) ( wellDimensions[ 0 ] * relativeWellBorderWidth ) );
		contrastLimits = new double[ 2 ];
		createBordersImage();
	}

	private void createBordersImage( )
	{
		BiConsumer< Localizable, UnsignedByteType > biConsumer = ( l, t ) ->
		{
			t.set( 0 );

//			final boolean drawSiteBorder = isDrawBorder( l, siteDimensions, relativeSiteBorderWidth );
//			if ( drawSiteBorder ) t.set( 180 );

			final boolean drawWellBorder = isDrawBorder( l, wellDimensions, relativeWellBorderWidth );
			if ( drawWellBorder ) t.set( 255 );

		};

		final FunctionRandomAccessible< UnsignedByteType > randomAccessible = new FunctionRandomAccessible( 2, biConsumer, UnsignedByteType::new );

		rai = Views.interval( randomAccessible, plateInterval );

		rai = Views.addDimension( rai, 0, 0 );

		final AffineTransform3D sourceTransform = new AffineTransform3D();
		sourceTransform.scale( voxelDimensions.dimension( 0 ), voxelDimensions.dimension( 1 ), voxelDimensions.dimension(2) );
		source = new RandomAccessibleIntervalSource<>( rai, new UnsignedByteType(), sourceTransform, IMAGE_NAME );

		contrastLimits[ 0 ] = 0;
		contrastLimits[ 1 ] = 255;
	}

	private static boolean isDrawBorder( Localizable l, long[] wellDimensions, double relativeWellBorderWidth )
	{
		final double[] distances = new double[ 2 ];
		for ( int d = 0; d < 2; d++ )
		{
			double ratio = 1.0 * l.getIntPosition( d ) / wellDimensions[ d ];
			distances[ d ] = Math.abs( ratio - Math.round( ratio ) );
		}

		final double distance = Math.min( distances[ 0 ], distances[ 1 ] );

		return distance < relativeWellBorderWidth;
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
	@Deprecated // use getSource()
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
	public BdvOverlay getOverlay()
	{
		return null;
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