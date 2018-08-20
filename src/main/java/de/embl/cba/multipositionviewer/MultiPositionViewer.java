package de.embl.cba.multipositionviewer;

import bdv.util.*;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.RealPoint;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.logic.BitType;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.ArrayList;

public class MultiPositionViewer
{

	final ArrayList< MultiPositionImagesSource > multiPositionImagesSources;
	final int numIoThreads;
	final SharedQueue loadingQueue;

	int[] imageDimensions;
	int[] bdvWindowDimensions;

	final Bdv bdv;

	public MultiPositionViewer( MultiPositionImagesSource source, int numIoThreads )
	{
		this.multiPositionImagesSources = new ArrayList<>();
		multiPositionImagesSources.add( source );
		this.numIoThreads = numIoThreads;

		setBdvWindowDimensions();

		loadingQueue = new SharedQueue( numIoThreads );

		this.bdv = createBdv( source );

	}

	public void setBdvWindowDimensions()
	{
		bdvWindowDimensions = new int[ 2 ];
		bdvWindowDimensions[ 0 ] = 800;
		bdvWindowDimensions[ 1 ] = 800;
	}


	public void zoomToImage( long[] imageCoordinates, int[] imageDimensions )
	{

		final AffineTransform3D affineTransform3D = getImageZoomTransform( imageCoordinates, imageDimensions );

		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( affineTransform3D );

	}


	public AffineTransform3D getImageZoomTransform( long[] imageCenterCoordinates, int[] imageDimensions )
	{

		final AffineTransform3D affineTransform3D = new AffineTransform3D();

		double[] shiftToImage = new double[ 3 ];

		for( int d = 0; d < 2; ++d )
		{
			shiftToImage[ d ] = -imageCenterCoordinates[ d ];
		}

		affineTransform3D.translate( shiftToImage );

		affineTransform3D.scale(  1.05 * bdvWindowDimensions[ 0 ] / imageDimensions[ 0 ] );

		double[] shiftToBdvWindowCenter = new double[ 3 ];

		for( int d = 0; d < 2; ++d )
		{
			shiftToBdvWindowCenter[ d ] += bdvWindowDimensions[ d ] / 2.0;
		}

		affineTransform3D.translate( shiftToBdvWindowCenter );

		return affineTransform3D;
	}

	public int[] getImageCenterCoordinates( int[] imageCoordinates )
	{
		int[] imageCenterCoordinatesInPixels = new int[ 2 ];

		for( int d = 0; d < 2; ++d )
		{
			imageCenterCoordinatesInPixels[ d ] = imageCoordinates[ d ] * imageDimensions[ d ];
			imageCenterCoordinatesInPixels[ d ] += imageDimensions[ d ] / 2.0;
		}
		return imageCenterCoordinatesInPixels;
	}


	private Bdv createBdv( MultiPositionImagesSource source )
	{

		// TODO:
		// - first show overlay, then zoom in, then add channel

		final ArrayImg< BitType, LongArray > dummyImageForInitialisation = ArrayImgs.bits( new long[]{ 100, 100 } );

		BdvSource bdvTmpSource = BdvFunctions.show(
				dummyImageForInitialisation,
				"",
				Bdv.options()
						.is2D()
						.preferredSize( 800, 800 )
						.doubleBuffered( false )
						.transformEventHandlerFactory( new BehaviourTransformEventHandlerPlanar.BehaviourTransformEventHandlerPlanarFactory() ) );

		Bdv bdv = bdvTmpSource.getBdvHandle();

		addSourceToBdv( source );

		bdvTmpSource.removeFromBdv();

		zoomToImage( source.getImageFile( 0 ).centerCoordinates, source.getImageFile( 0 ).dimensions );

		setBdvBehaviors();

		return bdv;

	}

	public void addSourceToBdv( MultiPositionImagesSource source )
	{
		BdvSource bdvSource = BdvFunctions.show(
					VolatileViews.wrapAsVolatile( source.getCachedCellImg(), loadingQueue ),
					"",
					BdvOptions.options().addTo( bdv ) );

		bdvSource.setDisplayRange( source.getLutMinMax()[ 0 ], source.getLutMinMax()[ 1 ] );

	}


	private void setBdvBehaviors()
	{
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdv.getBdvHandle().getTriggerbindings(), "my-new-behaviours" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			showImageName( );
		}, "log image info", "P" );
	}

	private void showImageName( )
	{
		final long[] coordinates = getMouseCoordinates();

		final ImageFile imageFile = multiPositionImagesSources.get( 0 ).getImageFile( coordinates );

		if ( imageFile != null )
		{
			Utils.log( imageFile.file.getName() );
		}

	}

	private long[] getMouseCoordinates()
	{
		final RealPoint position = new RealPoint( 3 );

		bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates( position );

		long[] cellPos = new long[ 2 ];

		for ( int d = 0; d < 2; ++d )
		{
			cellPos[ d ] = (long) ( position.getDoublePosition( d ) );
		}

		return cellPos;
	}

}
