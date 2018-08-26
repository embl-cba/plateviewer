package de.embl.cba.multipositionviewer;

import bdv.util.*;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.FinalInterval;
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

	private final ArrayList< ImagesSource > imagesSources;
	private final int numIoThreads;
	private final SharedQueue loadingQueue;

	private int[] imageDimensions;
	private int[] bdvWindowDimensions;

	private Bdv bdv;

	public MultiPositionViewer( ImagesSource source, int numIoThreads )
	{
		this.imagesSources = new ArrayList<>();
		this.numIoThreads = numIoThreads;

		setBdvWindowDimensions();

		loadingQueue = new SharedQueue( numIoThreads );

		initBdvAndAddSource( source );

	}

	public Bdv getBdv()
	{
		return bdv;
	}

	public SharedQueue getLoadingQueue()
	{
		return loadingQueue;
	}

	public void setBdvWindowDimensions()
	{
		bdvWindowDimensions = new int[ 2 ];
		bdvWindowDimensions[ 0 ] = 800;
		bdvWindowDimensions[ 1 ] = 800;
	}


	public void zoomToImage( FinalInterval interval )
	{
		final AffineTransform3D affineTransform3D = getImageZoomTransform( interval );

		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( affineTransform3D );

	}

	public void zoomToImage( String imageFileName )
	{
		// TODO: one could loop across all sources...
		int sourceIndex = 0;

		final ImageSource imageSource = imagesSources.get( sourceIndex ).getLoader().getImageFile( imageFileName );

		zoomToImage( imageSource.getInterval() );
	}


	public ArrayList< ImagesSource > getImagesSources()
	{
		return imagesSources;
	}

	public AffineTransform3D getImageZoomTransform( FinalInterval interval )
	{

		final AffineTransform3D affineTransform3D = new AffineTransform3D();

		double[] shiftToImage = new double[ 3 ];

		for( int d = 0; d < 2; ++d )
		{
			shiftToImage[ d ] = - ( interval.min( d ) + interval.dimension( d ) / 2.0 ) ;
		}

		affineTransform3D.translate( shiftToImage );

		affineTransform3D.scale(  1.05 * bdvWindowDimensions[ 0 ] / interval.dimension( 0 ) );

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


	private void initBdvAndAddSource( ImagesSource source )
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

		bdv = bdvTmpSource.getBdvHandle();

		setBdvBehaviors();

		zoomToImage( source.getLoader().getImageFile( 0 ).getInterval() );

		addSource( source );

		bdvTmpSource.removeFromBdv();



	}

	public void addSource( ImagesSource source )
	{
		imagesSources.add( source );

		BdvSource bdvSource = BdvFunctions.show(
					VolatileViews.wrapAsVolatile( source.getCachedCellImg(), loadingQueue ),
					source.getName()xzs,
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

		final ImageSource imageSource = imagesSources.get( 0 ).getLoader().getImageFile( coordinates );

		if ( imageSource != null )
		{
			Utils.log( imageSource.getFile().getName() );
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
