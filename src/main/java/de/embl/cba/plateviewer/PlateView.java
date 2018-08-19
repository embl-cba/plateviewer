package de.embl.cba.plateviewer;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.io.File;
import java.util.Map;

public class PlateView
{
	final int[] imageDimensions;
	final Map< String, File > cellFileMap;
	final int numIoThreads;
	final SharedQueue loadingQueue;
	Bdv bdv;

	public PlateView( CachedPlateViewImg cachedPlateViewImg, int numIoThreads )
	{
		this.imageDimensions = cachedPlateViewImg.getImageDimensions();
		this.cellFileMap = cachedPlateViewImg.getCellFileMap();
		this.numIoThreads = numIoThreads;

		loadingQueue = new SharedQueue( numIoThreads );

		addChannel( cachedPlateViewImg );

//		zoomToCell( new int[]{ 0, 0 } );

	}


	public void zoomToCell( int[] cellCoordinates )
	{

		final AffineTransform3D affineTransform3D = getCellZoomTransform( cellCoordinates );

		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( affineTransform3D );

	}

	public AffineTransform3D getCellZoomTransform( int[] cellCoordinates )
	{
		final AffineTransform3D affineTransform3D = new AffineTransform3D();

		affineTransform3D.scale( 1 );

		double[] translation = new double[3];

		for( int d = 0; d < 2; ++d )
		{
			translation[ d ] = - cellCoordinates[ d ] * imageDimensions[ d ];
//			translation[ d ] -= imageDimensions[ d ] / 2.0;
		}

		affineTransform3D.translate( translation );

		affineTransform3D.scale( 1 );

		return affineTransform3D;
	}


	private BdvSource initBdv( Img img, double[] lutMinMax )
	{

		BdvSource bdvSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( img, loadingQueue ),
				"",
				Bdv.options()
						.is2D()
						.preferredSize( 800, 800 )
						.sourceTransform( getCellZoomTransform( new int[]{ 0, 0} ) )
						.doubleBuffered( false )
						.transformEventHandlerFactory( new BehaviourTransformEventHandlerPlanar.BehaviourTransformEventHandlerPlanarFactory() ) );

		bdv = bdvSource.getBdvHandle();

		zoomToCell( new int[]{ 0, 0 } );

		setBdvBehaviors();

		return bdvSource;

	}

	public void addChannel( CachedPlateViewImg cachedPlateViewImg )
	{
		BdvSource bdvSource;

		if ( bdv == null )
		{
			bdvSource = initBdv( cachedPlateViewImg.getImg(), cachedPlateViewImg.getLutMinMax() );
		}
		else
		{
			bdvSource = BdvFunctions.show(
					VolatileViews.wrapAsVolatile( cachedPlateViewImg.getImg(), loadingQueue ),
					"",
					BdvOptions.options().addTo( bdv ) );
		}

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
