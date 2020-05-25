package de.embl.cba.plateviewer.screenshot;

import bdv.util.BdvHandle;
import bdv.util.BdvStackSource;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.DoubleStatistics;
import de.embl.cba.bdv.utils.measure.PixelValueStatistics;
import de.embl.cba.plateviewer.util.Imglib2ImagePlusConverters;
import de.embl.cba.plateviewer.image.channel.BdvViewable;
import de.embl.cba.plateviewer.image.source.RandomAccessibleIntervalPlateViewerSource;
import ij.CompositeImage;
import ij.gui.YesNoCancelDialog;
import net.imglib2.*;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.util.Grids;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.RealMaskRealInterval;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.*;

import static de.embl.cba.bdv.utils.BdvUtils.*;

public class PlateChannelRawDataFetcher
{
	private final Map< String, BdvViewable > nameToBdvViewable;
	private BdvHandle bdvHandle;
	private Map< Source< ? >, BdvStackSource< ? > > sourceToBdvStackSource;
	private int t;
	private double viewerVoxelSpacing;

	public PlateChannelRawDataFetcher( Map< String, BdvViewable > nameToBdvViewable )
	{
		this.nameToBdvViewable = nameToBdvViewable;
		sourceToBdvStackSource = createSourceToBdvChannelStackSource();

		t = bdvHandle.getViewerPanel().getState().getCurrentTimepoint();
		viewerVoxelSpacing = getViewerVoxelSpacing( bdvHandle );
	}

	public Map< String, PixelValueStatistics > computePixelValueStatistics( RealPoint globalPoint, double radius, int t )
	{
		final HashMap< String, PixelValueStatistics > nameToStatistics = new HashMap<>();

		for ( Source< ? > source : sourceToBdvStackSource.keySet() )
		{
			final int level = 0; // getLevel( source, viewerVoxelSpacing );

			final double[] doublePosition = getSourceDoublePosition( globalPoint, t, source, level );
			RealMaskRealInterval mask = GeomMasks.closedSphere( doublePosition, radius );
			IterableRegion< BoolType > iterableRegion = toIterableRegion(mask, source.getSource( t, level ));
			IterableInterval< ? extends RealType<?>> iterable = ( IterableInterval< ? extends RealType< ? > > ) Regions.sample(iterableRegion, source.getSource( t, level ));

			final ArrayList< Double > values = new ArrayList<>();
			for( RealType<?> pixel : iterable )
			{
				values.add( pixel.getRealDouble() );
			}

			final DoubleStatistics summaryStatistics = values.stream().collect(
					DoubleStatistics::new,
					DoubleStatistics::accept,
					DoubleStatistics::combine );

			final PixelValueStatistics statistics = new PixelValueStatistics();
			statistics.numVoxels = summaryStatistics.getCount();
			statistics.mean = summaryStatistics.getAverage();
			statistics.sdev = summaryStatistics.getStandardDeviation();

			nameToStatistics.put( source.getName(), statistics );
		}

		return nameToStatistics;
	}

	public Map< String, Double > fetchPixelValues( RealPoint globalPoint, int t )
	{
		final HashMap< String, Double > sourceNameToPixelValue = new HashMap<>();

		for ( Source< ? > source : sourceToBdvStackSource.keySet() )
		{
			final int level = getLevel( source, viewerVoxelSpacing );

			int[] sourceIntegerPosition = getSourceIntegerPosition( globalPoint, t, source, level );

			final RandomAccess< ? extends RealType > randomAccess = ( RandomAccess< ? extends RealType > ) source.getSource( t, level ).randomAccess();
			randomAccess.setPosition( sourceIntegerPosition );
			final double realDouble = randomAccess.get().getRealDouble();
			sourceNameToPixelValue.put( source.getName(), realDouble );
		}

		return sourceNameToPixelValue;
	}

	private int[] getSourceIntegerPosition( RealPoint globalPoint, int t, Source< ? > source, int level )
	{
		double[] sourceRealPosition = getSourceDoublePosition( globalPoint, t, source, level );
		int[] sourceIntegerPosition = new int[ 3 ];
		setIntegerPosition( sourceRealPosition, sourceIntegerPosition );
		return sourceIntegerPosition;
	}

	private double[] getSourceDoublePosition( RealPoint globalPoint, int t, Source< ? > source, int level )
	{
		AffineTransform3D sourceTransform = new AffineTransform3D();
		double[] globalPosition = new double[ 3 ];
		globalPoint.localize( globalPosition );
		double[] sourceRealPosition = new double[ 3 ];
		source.getSourceTransform( t, level, sourceTransform );
		sourceTransform.inverse().apply( globalPosition, sourceRealPosition );
		return sourceRealPosition;
	}

	public CompositeImage captureCurrentView( int level )
	{
		final int w = getBdvWindowWidth( bdvHandle );
		final int h = getBdvWindowHeight( bdvHandle );

		final double[] rawPixelSpacing = new double[]{1, 1};
		final double[] rawPixelSpacingInViewer = new double[ 2 ];

		for ( int d = 0; d < 2; d++ )
		{
			rawPixelSpacingInViewer[ d ] = rawPixelSpacing[ d ] / viewerVoxelSpacing;
		}

		final long captureWidth = ( long ) Math.ceil( w / rawPixelSpacingInViewer[ 0 ] );
		final long captureHeight = ( long ) Math.ceil( h / rawPixelSpacingInViewer[ 1 ] );

		if ( Math.max( captureHeight, captureWidth ) > 10000 )
		{
			final YesNoCancelDialog dialog = new YesNoCancelDialog( null, "Large image warning",
					"You are about to create a very large image with more than 10000 x 1000 pixels.\n" +
							"It may take some time to create it.\n" +
							"You could zoom in more and then ui the raw data\n." +
							"Are you sure you want to continue?" );

			if ( ! dialog.yesPressed() ) return null;
		}

		final Map< String, RandomAccessibleInterval< UnsignedShortType > > captures = new LinkedHashMap<>();
		final ArrayList< double[] > displayRanges = new ArrayList<>();

		final AffineTransform3D viewerTransform = getViewerTransform();

		for ( Source< ? > source : sourceToBdvStackSource.keySet() )
		{
			final RandomAccessibleInterval< UnsignedShortType > realCapture
					= ArrayImgs.unsignedShorts( captureWidth, captureHeight );

			final AffineTransform3D sourceTransform =
					BdvUtils.getSourceTransform( source, t, level );

			AffineTransform3D viewerToSourceTransform = new AffineTransform3D();
			viewerToSourceTransform.preConcatenate( viewerTransform.inverse() );
			viewerToSourceTransform.preConcatenate( sourceTransform.inverse() );

			Grids.collectAllContainedIntervals(
					Intervals.dimensionsAsLongArray( realCapture ),
					new int[]{100, 100}).parallelStream().forEach( interval ->
			{
				RandomAccess< ? extends RealType< ? > > realTypeAccess = (RandomAccess<? extends RealType<?>> ) source.getSource( t, level ).randomAccess();

				final IntervalView< UnsignedShortType > realCrop = Views.interval( realCapture, interval );
				final Cursor< UnsignedShortType > realCaptureCursor = Views.iterable( realCrop ).localizingCursor();
				final RandomAccess< UnsignedShortType > realCaptureAccess = realCrop.randomAccess();

				final double[] canvasPosition = new double[ 3 ];
				final double[] sourceRealPosition = new double[ 3 ];
				final int[] sourceIntegerPosition = new int[ 3 ];

				while ( realCaptureCursor.hasNext() )
				{
					realCaptureCursor.fwd();
					realCaptureCursor.localize( canvasPosition );
					realCaptureAccess.setPosition( realCaptureCursor );

					// canvasPosition is the position on the canvas, in calibrated units
					// dx and dy is the step size that is needed to get the desired resolution in the
					// output image
					canvasPosition[ 0 ] *= rawPixelSpacingInViewer[ 0 ];
					canvasPosition[ 1 ] *= rawPixelSpacingInViewer[ 1 ];

					viewerToSourceTransform.apply( canvasPosition, sourceRealPosition );
					setIntegerPosition( sourceRealPosition, sourceIntegerPosition );

					try
					{
						realTypeAccess.setPosition( sourceIntegerPosition );
						final RealType< ? > realType = realTypeAccess.get();
						realCaptureAccess.get().setReal( realType.getRealDouble() );
					}
					catch ( Exception e )
					{
						int a = 1;
					}
				}
			});

			captures.put( source.getName(), realCapture );

			final double[] displayRange = getDisplayRange( sourceToBdvStackSource, source );
			displayRanges.add( displayRange );
		}

		if ( captures.size() > 0 )
		{
			return Imglib2ImagePlusConverters.createCompositeImage( captures, displayRanges );
		}
		else
			return null;
	}

	private AffineTransform3D getViewerTransform()
	{
		final AffineTransform3D viewerTransform = new AffineTransform3D();
		bdvHandle.getViewerPanel().getState().getViewerTransform( viewerTransform );
		return viewerTransform;
	}

	private static void setIntegerPosition( double[] sourceRealPosition, int[] sourceIntegerPosition )
	{
		// we convert to integer position, because we want to fetch
		// non interpolated raw pixel values
		for ( int d = 0; d < 3; d++ )
		{
			sourceIntegerPosition[ d ] = (int) sourceRealPosition[ d ];
		}
	}

	private Map< Source< ? >, BdvStackSource< ? > > createSourceToBdvChannelStackSource()
	{
		final Map< Source< ? >, BdvStackSource< ? > > sourceToBdvStackSource = new HashMap<>();
		for ( BdvViewable bdvViewable : nameToBdvViewable.values() )
		{
			if ( ! ( bdvViewable.getBdvSource() instanceof BdvStackSource ) ) continue;

			final BdvStackSource< ? > bdvStackSource = ( BdvStackSource ) bdvViewable.getBdvSource();
			bdvHandle = bdvStackSource.getBdvHandle();

			if ( ! BdvUtils.isActive( bdvHandle, bdvStackSource.getSources().get( 0 ).getSpimSource() ) )
			{
				continue;
			}

			if ( ! ( bdvViewable.getSource() instanceof RandomAccessibleIntervalPlateViewerSource ) ) continue;

			sourceToBdvStackSource.put( bdvViewable.getSource(), bdvStackSource );
		}

		if ( sourceToBdvStackSource.keySet().size() == 0)
		{
			throw new UnsupportedOperationException( "No active sources present, cannot fetch raw data." );
		}

		return sourceToBdvStackSource;
	}

	private double[] getDisplayRange( Map< Source< ? >, BdvStackSource< ? > > sourceToBdvStackSource, Source< ? > source )
	{
		final double[] displayRange = new double[ 2 ];
		displayRange[ 0 ] = sourceToBdvStackSource.get( source ).getConverterSetups().get( 0 ).getDisplayRangeMin();
		displayRange[ 1 ] = sourceToBdvStackSource.get( source ).getConverterSetups().get( 0 ).getDisplayRangeMax();
		return displayRange;
	}

}
