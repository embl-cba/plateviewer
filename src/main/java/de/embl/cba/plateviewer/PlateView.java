package de.embl.cba.plateviewer;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.io.File;
import java.util.Map;

public class PlateView
{
	final Img img;
	final int[] cellDimensions;
	final Map< String, File > cellFileMap;
	Bdv bdv;

	public PlateView( Img img, int[] cellDimensions, Map< String, File > cellFileMap )
	{
		this.img = img;
		this.cellDimensions = cellDimensions;
		this.cellFileMap = cellFileMap;
	}

	public void show()
	{

		bdv = BdvFunctions.show( img, "",
				Bdv.options()
						.is2D()
						.transformEventHandlerFactory( new BehaviourTransformEventHandlerPlanar.BehaviourTransformEventHandlerPlanarFactory() ) );

		addAndChangeBdvBehaviors();

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
			cellPos[ d ] = (int) ( position.getDoublePosition( d ) / cellDimensions[ d ] );
		}

		return cellPos;
	}

}
