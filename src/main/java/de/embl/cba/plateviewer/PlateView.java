package de.embl.cba.plateviewer;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.io.File;
import java.util.Map;

public class PlateView
{
	final int[] imageDimensions;
	final Map< String, File > cellFileMap;
	Bdv bdv;

	public PlateView( CachedPlateViewImg cachedPlateViewImg )
	{
		this.imageDimensions = cachedPlateViewImg.getImageDimensions();
		this.cellFileMap = cachedPlateViewImg.getCellFileMap();

		addChannel( cachedPlateViewImg );
	}

	private BdvSource initBdv( Img img, double[] lutMinMax )
	{

		BdvSource bdvSource = BdvFunctions.show( img, "",
				Bdv.options()
						.is2D()
						.transformEventHandlerFactory( new BehaviourTransformEventHandlerPlanar.BehaviourTransformEventHandlerPlanarFactory() ) );



		bdv = bdvSource.getBdvHandle();

		addAndChangeBdvBehaviors();

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
			bdvSource = BdvFunctions.show( cachedPlateViewImg.getImg(), "", BdvOptions.options().addTo( bdv ) );
		}

		bdvSource.setDisplayRange( cachedPlateViewImg.getLutMinMax()[ 0 ], cachedPlateViewImg.getLutMinMax()[ 1 ] );

	}


	private void addAndChangeBdvBehaviors()
	{
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdv.getBdvHandle().getTriggerbindings(), "my-new-behaviours" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			showImageName( );
		}, "print image name", "P" );
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
