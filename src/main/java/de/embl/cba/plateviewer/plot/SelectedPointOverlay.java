package de.embl.cba.plateviewer.plot;

import bdv.util.BdvHandle;
import bdv.util.BdvOverlay;
import de.embl.cba.tables.select.SelectionListener;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.tablerow.TableRow;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import static de.embl.cba.plateviewer.Utils.bdvTextOverlayFontSize;

public class SelectedPointOverlay < T extends TableRow > extends BdvOverlay
{
	private final BdvHandle bdvHandle;
	private final List< T > tableRows;
	private final SelectionModel< T > selectionModel;
	private final ArrayList< RealPoint > points;
	private RealPoint selectedPoint;
	private int selectionCircleWidth;

	public SelectedPointOverlay( BdvHandle bdvHandle, List< T > tableRows, SelectionModel< T > selectionModel, ArrayList< RealPoint > points )
	{
		super();
		this.bdvHandle = bdvHandle;
		this.tableRows = tableRows;
		this.selectionModel = selectionModel;
		this.points = points;

		selectionCircleWidth = 10;

		registerAsSelectionListener( selectionModel, points );
	}

	public void registerAsSelectionListener( SelectionModel< T > selectionModel, ArrayList< RealPoint > points )
	{
		selectionModel.listeners().add( new SelectionListener< T >()
		{
			@Override
			public void selectionChanged()
			{

			}

			@Override
			public void focusEvent( T selection )
			{
				final int rowIndex = selection.rowIndex();
				selectedPoint = points.get( rowIndex );
				bdvHandle.getViewerPanel().requestRepaint();
			}
		} );
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		if ( selectedPoint == null ) return;

		g.setColor( Color.WHITE );
		final AffineTransform3D globalToViewerTransform = new AffineTransform3D();
		getCurrentTransform3D( globalToViewerTransform );

		final RealPoint viewerPoint = new RealPoint( 0, 0, 0 );
		final RealPoint globalPoint = new RealPoint( selectedPoint.getDoublePosition( 0 ), selectedPoint.getDoublePosition( 1 ), 0 );
		globalToViewerTransform.apply( globalPoint, viewerPoint );


		final Ellipse2D.Double circle = new Ellipse2D.Double(
				viewerPoint.getDoublePosition( 0 ) - selectionCircleWidth / 2,
				viewerPoint.getDoublePosition( 1 ) - selectionCircleWidth / 2,
				selectionCircleWidth,
				selectionCircleWidth );

		g.draw( circle );
	}
}

