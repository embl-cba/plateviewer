package de.embl.cba.gridviewer;

import bdv.util.*;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.FinalInterval;
import net.imglib2.RealPoint;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.ArrayList;

public class MultiPositionViewer < T extends NativeType< T > & RealType< T > >
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


	public void zoomToInterval( FinalInterval interval )
	{
		final AffineTransform3D affineTransform3D = getImageZoomTransform( interval );

		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( affineTransform3D );

	}

	public ArrayList< String > getSiteNames()
	{
		final ArrayList< ImageSource > imageSources = imagesSources.get( 0 ).getLoader().getImageSources();

		final ArrayList< String > imageNames = new ArrayList<>(  );

		for ( ImageSource imageSource : imageSources )
		{
			imageNames.add( imageSource.getFile().getName() );
		}

		return imageNames;
	}

	public ArrayList< String > getWellNames()
	{
		return imagesSources.get( 0 ).getWellNames();
	}

	public void zoomToWell( String wellName )
	{
		int sourceIndex = 0; // channel 0

		final ArrayList< ImageSource > imageSources = imagesSources.get( sourceIndex ).getLoader().getImageSources();

		FinalInterval union = null;

		for ( ImageSource imageSource : imageSources )
		{
			if ( imageSource.getWellName().equals( wellName ) )
			{
				if ( union == null )
				{
					union = new FinalInterval( imageSource.getInterval() );
				}
				else
				{
					union = Intervals.union( imageSource.getInterval(), union );
				}
			}
		}

		zoomToInterval( union );
	}

	public void zoomToImage( String imageFileName )
	{
		int sourceIndex = 0;

		final ImageSource imageSource = imagesSources.get( sourceIndex ).getLoader().getImageFile( imageFileName );

		zoomToInterval( imageSource.getInterval() );
	}

	public boolean isImageExisting( final SingleCellArrayImg< T, ? > cell )
	{
		final ImageSource imageFile = imagesSources.get( 0 ).getLoader().getImageFile( cell );

		if ( imageFile != null ) return true;
		else return false;
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
						.preferredSize( bdvWindowDimensions[ 0 ], bdvWindowDimensions[ 1 ] )
						.doubleBuffered( false )
						.transformEventHandlerFactory( new BehaviourTransformEventHandlerPlanar.BehaviourTransformEventHandlerPlanarFactory() ) );

		bdv = bdvTmpSource.getBdvHandle();

		setBdvBehaviors();

		zoomToInterval( source.getLoader().getImageFile( 0 ).getInterval() );

		addSource( source );

		bdvTmpSource.removeFromBdv();

	}

	public void addSource( ImagesSource source )
	{
		imagesSources.add( source );

		BdvSource bdvSource = BdvFunctions.show(
					VolatileViews.wrapAsVolatile( source.getCachedCellImg(), loadingQueue ),
					source.getName(),
					BdvOptions.options().addTo( bdv ) );

		bdvSource.setDisplayRange( source.getLutMinMax()[ 0 ], source.getLutMinMax()[ 1 ] );

		source.setBdvSource( bdvSource );
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
