package de.embl.cba.plateviewer.plot;

import bdv.util.*;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.bdv.BehaviourTransformEventHandlerPlanar;
import de.embl.cba.plateviewer.image.source.ARGBConvertedRealAccessibleSource;
import de.embl.cba.plateviewer.image.table.ListItemsARGBConverter;
import de.embl.cba.plateviewer.view.PopupMenu;
import de.embl.cba.tables.color.ColorUtils;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.tablerow.TableRow;
import ij.gui.GenericDialog;
import net.imglib2.*;
import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.util.Intervals;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class TableRowsScatterPlotView< T extends TableRow >
{
	private final List< T > tableRows;
	private FinalInterval scatterPlotInterval;
	private int numTableRows;
	private final SelectionColoringModel< T > coloringModel;
	private final SelectionModel< T > selectionModel;
	private BdvHandle bdvHandle;
	private ArrayList< RealPoint > points;
	private ArrayList< Integer > indices;
	private double pointSize;
	private Source< VolatileARGBType > argbSource;
	private NearestNeighborSearchOnKDTree< Integer > search;
	private final String plateName;
	private String columnNameX;
	private String columnNameY;
	private BdvStackSource< VolatileARGBType > scatterPlotBdvSource;
	private final String[] columnNames;
	private BdvOverlaySource< SelectedPointOverlay > selectedPointOverlayBdvSource;
	private BdvOverlaySource< ScatterPlotOverlay > scatterPlotOverlayBdvSource;
	private String[] lineChoices;
	private String lineOverlay;
	private double viewerAspectRatio = 1.0;
	private RealRandomAccessibleIntervalSource indexSource;

	public TableRowsScatterPlotView(
			List< T > tableRows,
			SelectionColoringModel< T > coloringModel,
			SelectionModel< T > selectionModel,
			String plateName,
			String columnNameX,
			String columnNameY,
			String lineOverlay
			)
	{
		this.tableRows = tableRows;
		this.coloringModel = coloringModel;
		this.selectionModel = selectionModel;
		this.plateName = plateName;
		this.columnNameX = columnNameX;
		this.columnNameY = columnNameY;

		numTableRows = tableRows.size();
		columnNames = tableRows.get( 0 ).getColumnNames().stream().toArray( String[]::new );

		coloringModel.listeners().add( () -> {
			bdvHandle.getViewerPanel().requestRepaint();
		} );

		this.lineOverlay = lineOverlay;
	}

	private void createAndShowImage()
	{
		setValuesAndSearch( columnNameX, columnNameY );

		BiConsumer< RealLocalizable, IntType > biConsumer = createFunction();

		createSource( biConsumer );

		showSource();

		setViewerTransform();

		showOverlays();
	}

	private void showOverlays()
	{
		showFrameAndAxis();

		showSelectedPoints();
	}

	private void showSelectedPoints()
	{
		SelectedPointOverlay selectedPointOverlay = new SelectedPointOverlay( bdvHandle, tableRows, selectionModel, points );

		if ( selectedPointOverlayBdvSource != null) selectedPointOverlayBdvSource.removeFromBdv();

		selectedPointOverlayBdvSource = BdvFunctions.showOverlay( selectedPointOverlay, "selected point overlay", BdvOptions.options().addTo( bdvHandle ).is2D() );
	}

	private void showFrameAndAxis()
	{
		ScatterPlotOverlay scatterPlotOverlay = new ScatterPlotOverlay( bdvHandle, columnNameX, columnNameY, scatterPlotInterval, lineOverlay );

		if ( scatterPlotOverlayBdvSource != null ) scatterPlotOverlayBdvSource.removeFromBdv();

		scatterPlotOverlayBdvSource = BdvFunctions.showOverlay( scatterPlotOverlay, "scatter plot overlay", BdvOptions.options().addTo( bdvHandle ).is2D() );
	}

	private void installBdvBehaviours()
	{
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdvHandle.getTriggerbindings(), "plate viewer" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			showPopupMenu( x, y );
		}, "context menu", "button3", "button1" ) ; // "button1",
	}

	private void showPopupMenu( int x, int y )
	{
		final PopupMenu popupMenu = new PopupMenu();

		popupMenu.addPopupAction( "Focus closest image", e ->
		{
			new Thread( () -> {
				final RealPoint global2dLocation = getMouseGlobal2dLocation();
				search.search( global2dLocation );
				final Sampler< Integer > sampler = search.getSampler();
				final Integer rowIndex = sampler.get();
				final T tableRow = tableRows.get( rowIndex );
//				final String cell = tableRow.getCell( columnNameX );
				selectionModel.focus( tableRow );
			}).start();
		} );

		popupMenu.addPopupAction( "Change columns...", e ->
		{
			lineChoices = new String[]{ ScatterPlotOverlay.X_Y, ScatterPlotOverlay.Y_1 };

			new Thread( () -> {
				final GenericDialog gd = new GenericDialog( "Column selection" );
				gd.addChoice( "Column X", columnNames, columnNameX );
				gd.addChoice( "Column Y", columnNames, columnNameY );
				gd.addChoice( "Add line", lineChoices, ScatterPlotOverlay.X_Y );
				gd.showDialog();
				if ( gd.wasCanceled() ) return;
				columnNameX = gd.getNextChoice();
				columnNameY = gd.getNextChoice();
				lineOverlay = gd.getNextChoice();

				createAndShowImage();

			}).start();
		} );


		popupMenu.show( bdvHandle.getViewerPanel().getDisplay(), x, y );
	}

	private RealPoint getMouseGlobal2dLocation()
	{
		final RealPoint global3dLocation = new RealPoint( 3 );
		bdvHandle.getViewerPanel().getGlobalMouseCoordinates( global3dLocation );
		return new RealPoint( global3dLocation.getDoublePosition( 0 ), global3dLocation.getDoublePosition( 1 ) );
	}

	public void setValuesAndSearch( String columnNameX, String columnNameY )
	{
		points = new ArrayList<>();
		indices = new ArrayList<>();

		Double x, y;
		Double xMax=-Double.MAX_VALUE,yMax=-Double.MAX_VALUE,xMin=Double.MAX_VALUE,yMin=Double.MAX_VALUE;

		for ( int rowIndex = 0; rowIndex < numTableRows; rowIndex++ )
		{
			x = Utils.parseDouble( tableRows.get( rowIndex ).getCell( columnNameX ) );
			if ( x.isNaN() ) continue;
			y = Utils.parseDouble( tableRows.get( rowIndex ).getCell( columnNameY ) );
			if ( y.isNaN() ) continue;

			points.add( new RealPoint( x, y ) );
			indices.add( rowIndex );
			if ( x > xMax ) xMax = x;
			if ( y > yMax ) yMax = y;
			if ( x < xMin ) xMin = x;
			if ( y < yMin ) yMin = y;
		}

		pointSize = ( xMax - xMin ) / 500.0; // TODO: ?

		scatterPlotInterval = FinalInterval.createMinMax(
				xMin.intValue(), yMin.intValue(), 0,
				(int) Math.ceil( xMax ), (int) Math.ceil( yMax ), 0 );

		scatterPlotInterval = Intervals.expand( scatterPlotInterval, (int) ( 10 * pointSize) );

		// Give a copy because the order of the list is changed by the KDTree
		final ArrayList< RealPoint > copy = new ArrayList<>( points );
		final KDTree< Integer > kdTree = new KDTree<>( indices, copy );
		search = new NearestNeighborSearchOnKDTree<>( kdTree );
	}

	public BiConsumer< RealLocalizable, IntType > createFunction()
	{
		double squaredViewerPointSize = pointSize * pointSize;
		//final double scaledPointSizeY = pointSize * viewerAspectRatio;

		final double[] dxy = new double[ 2 ];

		return ( position, t ) ->
		{
			synchronized ( this )
			{
				search.search( position );

				for ( int d = 0; d < 2; d++ )
				{
					dxy[ d ] = search.getPosition().getDoublePosition( d ) - position.getDoublePosition( d );
				}

				if ( ( dxy[ 0 ] * dxy[ 0 ] + dxy[ 1 ] * dxy[ 1 ] * viewerAspectRatio * viewerAspectRatio ) < squaredViewerPointSize  )
				{
					final Sampler< Integer > sampler = search.getSampler();
					final Integer integer = sampler.get();
					t.set( integer );
				}
				else
				{
					t.set( -1 );
				}
			}
		};
	}

	private void showSource()
	{
		if ( scatterPlotBdvSource != null ) scatterPlotBdvSource.removeFromBdv();

		scatterPlotBdvSource = BdvFunctions.show(
				argbSource,
				BdvOptions.options()
						.is2D()
						.frameTitle( plateName )
						.preferredSize( Utils.getBdvWindowSize(), Utils.getBdvWindowSize() )
						.transformEventHandlerFactory( new BehaviourTransformEventHandlerPlanar
						.BehaviourTransformEventHandlerPlanarFactory() ).addTo( bdvHandle ) );

		bdvHandle = scatterPlotBdvSource.getBdvHandle();

		scatterPlotBdvSource.setDisplayRange( 0, 255);
	}

	public void createSource( BiConsumer< RealLocalizable, IntType > biConsumer )
	{
		final FunctionRealRandomAccessible< IntType > fra = new FunctionRealRandomAccessible< IntType >( 2, biConsumer, IntType::new );

		final RealRandomAccessible< IntType > rra = RealViews.addDimension( fra );

		indexSource = new RealRandomAccessibleIntervalSource( rra, scatterPlotInterval, new IntType(  ), "scatterPlot" );

		//scatterSource.getInterpolatedSource(  )

		final ListItemsARGBConverter< T > converter =
				new ListItemsARGBConverter( tableRows, coloringModel );

		converter.getIndexToColor().put( -1, ColorUtils.getARGBType( Color.LIGHT_GRAY ).get() );

		argbSource = new ARGBConvertedRealAccessibleSource( indexSource, converter );
	}

	public void setViewerTransform()
	{
		AffineTransform3D viewerTransform = new AffineTransform3D();
		bdvHandle.getViewerPanel().getState().getViewerTransform( viewerTransform  );

		AffineTransform3D reflectY = new AffineTransform3D();
		reflectY.set( -1.0, 1,1 );
		viewerTransform.preConcatenate( reflectY );

		final FinalRealInterval bounds = viewerTransform.estimateBounds( scatterPlotInterval );

		final AffineTransform3D translate = new AffineTransform3D();
		translate.translate( 0, - ( bounds.realMin( 1 ) ) , 0 ); // TODO: ??
		viewerTransform.preConcatenate( translate );

		final double plotHeight = Math.abs( bounds.realMax( 1 ) - bounds.realMin( 1 ) );
		final double plotWidth = Math.abs( bounds.realMax( 0 ) - bounds.realMin( 0 ) );

		final double aspectRatio = plotHeight / plotWidth;
		if ( aspectRatio < 0.2 )
		{
			final AffineTransform3D scale = new AffineTransform3D();
			final double yScale = 1.0 / aspectRatio;
			scale.scale( 1.0, yScale, 1.0  );
			viewerTransform.preConcatenate( scale );
			viewerAspectRatio = yScale;
		}
		else
		{
			viewerAspectRatio = 1.0;
		}

		bdvHandle.getViewerPanel().setCurrentViewerTransform( viewerTransform );
	}

	public void show( JComponent component )
	{
		createAndShowImage();

		setWindowPosition( component );

		installBdvBehaviours();
	}

	public void setWindowPosition( JComponent component )
	{
		if ( component != null )
		{
			JFrame topFrame = ( JFrame ) SwingUtilities.getWindowAncestor( component );

			BdvUtils.getViewerFrame( bdvHandle ).setLocation(
					topFrame.getLocationOnScreen().x + component.getWidth() + 10,
					topFrame.getLocationOnScreen().y );
		}
	}
}
