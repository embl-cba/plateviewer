package de.embl.cba.plateviewer.plot;

import bdv.util.Bdv;
import bdv.util.BdvHandle;
import bdv.util.BdvOverlay;
import bdv.viewer.animate.SimilarityTransformAnimator;
import bdv.viewer.animate.TranslationAnimator;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.bdv.RelativeTranslationAnimator;
import de.embl.cba.plateviewer.table.Outlier;
import de.embl.cba.tables.select.SelectionListener;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.tablerow.TableRow;
import net.imglib2.RealPoint;
import net.imglib2.ops.parse.token.Real;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.util.LinAlgHelpers;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class SelectedPointOverlay < T extends TableRow > extends BdvOverlay implements SelectionListener< T >
{
	private final BdvHandle bdvHandle;
	private final List< T > tableRows;
	private final SelectionModel< T > selectionModel;
	private final ArrayList< RealPoint > points;
	private final String columnNameX;
	private final String columnNameY;
	private RealPoint selectedPoint;
	private int selectionCircleWidth;

	public SelectedPointOverlay( BdvHandle bdvHandle, List< T > tableRows, SelectionModel< T > selectionModel, ArrayList< RealPoint > points, String columnNameX, String columnNameY )
	{
		super();
		this.bdvHandle = bdvHandle;
		this.tableRows = tableRows;
		this.selectionModel = selectionModel;
		this.points = points;
		this.columnNameX = columnNameX;
		this.columnNameY = columnNameY;

		selectionCircleWidth = 20;

		selectionModel.listeners().add( this );
	}

	public void close()
	{
		selectionModel.listeners().remove( this );
	}

	private void centerViewer( RealPoint selectedPoint, long durationMillis )
	{
		final AffineTransform3D currentViewerTransform = new AffineTransform3D();
		getCurrentTransform3D( currentViewerTransform );

		final double[] globalLocation = { selectedPoint.getDoublePosition( 0 ), selectedPoint.getDoublePosition( 1 ), 0 };
		final double[] currentViewerLocation = new double[ 3 ];
		currentViewerTransform.apply( globalLocation, currentViewerLocation );

		final double[] bdvWindowCenter = BdvUtils.getBdvWindowCenter( this.bdvHandle );

		final double[] translation = new double[ 3 ];
		LinAlgHelpers.subtract( bdvWindowCenter, currentViewerLocation, translation );

		final RelativeTranslationAnimator animator = new RelativeTranslationAnimator(
				currentViewerTransform.copy(),
				translation,
				durationMillis );

		this.bdvHandle.getViewerPanel().setTransformAnimator( animator );
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		if ( selectedPoint == null ) return;

		g.setColor( Color.WHITE );

		final RealPoint viewerPoint = getViewerPoint( selectedPoint );

		final Ellipse2D.Double circle = new Ellipse2D.Double(
				viewerPoint.getDoublePosition( 0 ) - selectionCircleWidth / 2,
				viewerPoint.getDoublePosition( 1 ) - selectionCircleWidth / 2,
				selectionCircleWidth,
				selectionCircleWidth );

		g.draw( circle );
	}

	public RealPoint getViewerPoint( RealPoint globalPoint2D )
	{
		final AffineTransform2D globalToViewerTransform = new AffineTransform2D();
		getCurrentTransform2D( globalToViewerTransform );

		final RealPoint viewerPoint2D = new RealPoint( 0, 0 );
		globalToViewerTransform.apply( globalPoint2D, viewerPoint2D );
		return viewerPoint2D;
	}

	@Override
	public void selectionChanged()
	{

	}

	@Override
	public void focusEvent( T selection )
	{
		if ( bdvHandle == null ) return;

		if ( selection instanceof Outlier )
		{
			if ( ( ( Outlier ) selection ).isOutlier() )
			{
				return;
			}
		}

		final double x = Utils.parseDouble( selection.getCell( columnNameX ));
		final double y = Utils.parseDouble( selection.getCell( columnNameY ) );
		selectedPoint = new RealPoint( x, y );

		centerViewer( selectedPoint, 2000 );
	}
}

