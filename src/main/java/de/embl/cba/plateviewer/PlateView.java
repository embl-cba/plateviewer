package de.embl.cba.plateviewer;

import bdv.util.*;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.Scale;
import net.imglib2.type.logic.BitType;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.io.File;
import java.util.Map;

public class PlateView
{
	final int[] imageDimensions;
	int[] bdvWindowDimensions;

	final Map< String, File > cellFileMap;
	final int numIoThreads;
	final SharedQueue loadingQueue;
	Bdv bdv;

	public PlateView( CachedPlateViewImg cachedPlateViewImg, int numIoThreads )
	{
		this.imageDimensions = cachedPlateViewImg.getImageDimensions();
		this.cellFileMap = cachedPlateViewImg.getCellFileMap();
		this.numIoThreads = numIoThreads;

		setBdvWindowDimensions();

		loadingQueue = new SharedQueue( numIoThreads );

		addChannel( cachedPlateViewImg );

//		zoomToImage( new int[]{ 0, 0 } );

	}

	public void setBdvWindowDimensions()
	{
		bdvWindowDimensions = new int[ 2 ];
		bdvWindowDimensions[ 0 ] = 800;
		bdvWindowDimensions[ 1 ] = 800;
	}


	public void zoomToImage( int[] imageCoordinates )
	{

		final AffineTransform3D affineTransform3D = getImageZoomTransform( imageCoordinates );

		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( affineTransform3D );

	}

	public AffineTransform3D getImageZoomTransform( int[] imageCoordinates )
	{

		int[] imageCenterCoordinatesInPixels = new int[ 2 ];

		for( int d = 0; d < 2; ++d )
		{
			imageCenterCoordinatesInPixels[ d ] = imageCoordinates[ d ] * imageDimensions[ d ];
			imageCenterCoordinatesInPixels[ d ] += imageDimensions[ d ] / 2.0;
		}

		final AffineTransform3D affineTransform3D = new AffineTransform3D();

		double[] shiftToImage = new double[ 3 ];

		for( int d = 0; d < 2; ++d )
		{
			shiftToImage[ d ] = -imageCenterCoordinatesInPixels[ d ];
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


	private BdvSource initBdv( Img img )
	{

		// TODO:
		// - first show overlay, then zoom in, then add channel

		final ArrayImg< BitType, LongArray > dummyImageForInitialisation = ArrayImgs.bits( new long[]{ 100, 100 } );

		BdvSource bdvSource = BdvFunctions.show(
				dummyImageForInitialisation,
				"",
				Bdv.options()
						.is2D()
						.preferredSize( 800, 800 )
						.doubleBuffered( false )
						.transformEventHandlerFactory( new BehaviourTransformEventHandlerPlanar.BehaviourTransformEventHandlerPlanarFactory() ) );

		bdv = bdvSource.getBdvHandle();

		zoomToImage( new int[]{ 0, 0 } );

		setBdvBehaviors();

		return bdvSource;

	}

	public void addChannel( CachedPlateViewImg cachedPlateViewImg )
	{
		BdvSource bdvSource;

		if ( bdv == null )
		{
			bdvSource = initBdv( cachedPlateViewImg.getImg() );
		}

//		else
//		{
			bdvSource = BdvFunctions.show(
					VolatileViews.wrapAsVolatile( cachedPlateViewImg.getImg(), loadingQueue ),
					"",
					BdvOptions.options().addTo( bdv ) );
//		}

		bdvSource.setDisplayRange( cachedPlateViewImg.getLutMinMax()[ 0 ], cachedPlateViewImg.getLutMinMax()[ 1 ] );
		
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
		final int[] cellPos = getCellPosFromMouseCoordinates();

		final String key = Utils.getCellString( cellPos );

		if ( cellFileMap.containsKey( key ) )
		{
			Utils.log( cellFileMap.get( key ).getName() );
		}

	}

	private int[] getCellPosFromMouseCoordinates()
	{
		final RealPoint position = new RealPoint( 3 );

		bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates( position );

		int[] cellPos = new int[ 2 ];

		for ( int d = 0; d < 2; ++d )
		{
			cellPos[ d ] = (int) ( position.getDoublePosition( d ) / imageDimensions[ d ] );
		}

		return cellPos;
	}

}
