package de.embl.cba.plateviewer.bdv;

import bdv.util.BdvHandle;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.BdvUtils;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.LUT;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.util.Grids;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static de.embl.cba.bdv.utils.BdvUtils.*;

public class RawDataFetcher
{
	private final BdvHandle bdvHandle;

	public RawDataFetcher( BdvHandle bdvHandle )
	{
		this.bdvHandle = bdvHandle;
	}

	public CompositeImage fetchRawData( int level )
	{
		final List< Integer > visibleSourceIndices = getVisibleSourceIndices( bdvHandle );
		final int t = bdvHandle.getViewerPanel().getState().getCurrentTimepoint();

		final int w = getBdvWindowWidth( bdvHandle );
		final int h = getBdvWindowHeight( bdvHandle );

		final double viewerVoxelSpacing = getViewerVoxelSpacing( bdvHandle );
		final double[] rawPixelSpacing = getRawPixelSpacing( visibleSourceIndices );
		final double[] rawPixelSpacingInViewer = new double[ 2 ];

		for ( int d = 0; d < 2; d++ )
		{
			rawPixelSpacingInViewer[ d ] = rawPixelSpacing[ d ] / viewerVoxelSpacing;
		}

		final long captureWidth = ( long ) Math.ceil( w / rawPixelSpacingInViewer[ 0 ] );
		final long captureHeight = ( long ) Math.ceil( h / rawPixelSpacingInViewer[ 1 ] );

		final ArrayList< RandomAccessibleInterval< UnsignedShortType > > captures = new ArrayList<>();
		final ArrayList< double[] > displayRanges = new ArrayList<>();

		final AffineTransform3D viewerTransform = new AffineTransform3D();
		bdvHandle.getViewerPanel().getState().getViewerTransform( viewerTransform );

		for ( int sourceIndex : visibleSourceIndices )
		{
			final RandomAccessibleInterval< UnsignedShortType > realCapture
					= ArrayImgs.unsignedShorts( captureWidth, captureHeight );

			Source< ? > source = getSource( bdvHandle, sourceIndex );

			final AffineTransform3D sourceTransform =
					BdvUtils.getSourceTransform( source, t, level );

			AffineTransform3D viewerToSourceTransform = new AffineTransform3D();
			viewerToSourceTransform.preConcatenate( viewerTransform.inverse() );
			viewerToSourceTransform.preConcatenate( sourceTransform.inverse() );

			Grids.collectAllContainedIntervals(
					Intervals.dimensionsAsLongArray( realCapture ),
					new int[]{100, 100}).parallelStream().forEach( interval ->
			{
				// TODO: this does not work, the type still is volatile...
				// Solve this another way!
				RandomAccess< ? extends RealType< ? > > realTypeAccess =
						BdvUtils.getRealTypeNonVolatileRandomAccess( source, t, level );

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

					// we convert to integer position, because we want to fetch
					// non interpolated raw pixel values
					for ( int d = 0; d < 3; d++ )
					{
						sourceIntegerPosition[ d ] = (int) sourceRealPosition[ d ];
					}

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

			captures.add( realCapture );
			displayRanges.add( BdvUtils.getDisplayRange( bdvHandle, sourceIndex) );
		}

		if ( captures.size() > 0 )
		{
			return createCompositeImage( captures, displayRanges );
		}
		else
			return null;
	}

	private double[] getRawPixelSpacing( List< Integer > visibleSourceIndices )
	{
		final double[] dxy = new double[]{ 1, 1};

		final VoxelDimensions voxelDimensions = getVoxelDimensions( bdvHandle, visibleSourceIndices.get( 0 ) );

		if ( voxelDimensions != null )
		{
			dxy[ 0 ] = voxelDimensions.dimension( 0 );
			dxy[ 1 ] = voxelDimensions.dimension( 1 );
		}
		return dxy;
	}

	public static CompositeImage createCompositeImage(
			ArrayList< RandomAccessibleInterval< UnsignedShortType > > rais,
			ArrayList< double[] > displayRanges )
	{
		final RandomAccessibleInterval< UnsignedShortType > stack = Views.stack( rais );

		ImagePlus imp = ImageJFunctions.wrap( stack, "View Capture Raw" );

		// duplicate: otherwise it is virtual and cannot be modified
		imp = new Duplicator().run( imp );
//

		final CompositeImage compositeImage = new CompositeImage( imp );

		for ( int channel = 1; channel <= compositeImage.getNChannels(); ++channel )
		{
			final LUT lut = compositeImage.createLutFromColor( Color.WHITE );
			compositeImage.setC( channel );
			compositeImage.setChannelLut( lut );
			final double[] range = displayRanges.get( channel - 1 );
			compositeImage.setDisplayRange( range[ 0 ], range[ 1 ] );
		}

		compositeImage.setTitle( "Raw" );
		return compositeImage;
	}
}
