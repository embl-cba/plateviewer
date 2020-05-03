package de.embl.cba.plateviewer.plot;

import bdv.util.*;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.bdv.BehaviourTransformEventHandlerPlanar;
import de.embl.cba.plateviewer.image.source.ARGBConvertedRealAccessibleSource;
import de.embl.cba.plateviewer.image.table.ListItemsARGBConverter;
import de.embl.cba.plateviewer.table.Outlier;
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
import net.imglib2.ui.TransformListener;
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
	private Interval dataPlotInterval;
	private int numTableRows;
	private final SelectionColoringModel< T > coloringModel;
	private final SelectionModel< T > selectionModel;
	private BdvHandle bdvHandle;
	private ArrayList< RealPoint > points;
	private ArrayList< Integer > indices;
	private double viewerPointSize;
	private Source< VolatileARGBType > argbSource;
	private NearestNeighborSearchOnKDTree< Integer > search;
	private final String plateName;
	private String columnNameX;
	private String columnNameY;
	private BdvStackSource< VolatileARGBType > scatterPlotBdvSource;
	private final String[] columnNames;
	private String[] lineChoices;
	private String lineOverlay;
	private double viewerAspectRatio = 1.0;
	private RealRandomAccessibleIntervalSource indexSource;
	private FinalRealInterval dataInterval;
	private double[] dataRanges;
	private double dataAspectRatio;
	private ArrayList< RealPoint> viewerPoints;
	private AffineTransform3D viewerTransform;
	private String name;

	public TableRowsScatterPlotView(
			List< T > tableRows,
			String name,
			SelectionColoringModel< T > coloringModel,
			SelectionModel< T > selectionModel,
			String plateName,
			String columnNameX,
			String columnNameY,
			String lineOverlay )
	{
		this.tableRows = tableRows;
		this.name = name;
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

	private void createAndShowImage( int x, int y )
	{
		fetchDataPoints( columnNameX, columnNameY );

		createSearchTree();

		setViewerAspectRatio();

		setViewerPointSize();

		BiConsumer< RealLocalizable, IntType > biConsumer = createPlotFunction();

		// TODO: distinguish colors outside scatter plot range and background
		createSource( biConsumer );

		showSource();

		registerAsViewerTransformListener();

		setViewerTransform();

		installBdvBehaviours();

		setWindowPosition( x, y );

		showOverlays();
	}

	private void registerAsViewerTransformListener()
	{
		bdvHandle.getViewerPanel().addTransformListener( new TransformListener< AffineTransform3D >()
		{
			@Override
			public void transformChanged( AffineTransform3D affineTransform3D )
			{
				synchronized ( this )
				{
					viewerTransform = affineTransform3D;
					createViewerSearchTree( viewerTransform );
				}
			}
		} );
	}

	private void setViewerPointSize()
	{
		viewerPointSize = 7; // TODO: ?
	}

	private void setViewerAspectRatio()
	{
		if ( dataAspectRatio < 0.2  || dataAspectRatio > 1 / 0.2 )
		{
			viewerAspectRatio = 1 / dataAspectRatio;
		}
		else
		{
			viewerAspectRatio = 1.0;
		}
	}

	private void createSearchTree()
	{
		// Give a copy because the order of the list is changed by the KDTree
		final ArrayList< RealPoint > copy = new ArrayList<>( points );
		final KDTree< Integer > kdTree = new KDTree<>( indices, copy );
		search = new NearestNeighborSearchOnKDTree<>( kdTree );
	}

	private void createViewerSearchTree( AffineTransform3D transform3D )
	{
		for ( int i = 0; i < points.size(); i++ )
		{
			transform3D.apply( points.get( i ), viewerPoints.get( i ) );
		}

		final KDTree< Integer > kdTree = new KDTree<>( indices, viewerPoints );
		search = new NearestNeighborSearchOnKDTree<>( kdTree );
	}

	private void showOverlays()
	{
		showFrameAndAxis();

		showSelectedPoints();
	}

	private void showSelectedPoints()
	{
		SelectedPointOverlay selectedPointOverlay =
				new SelectedPointOverlay(
					bdvHandle,
					tableRows,
					selectionModel,
					points,
					columnNameX,
					columnNameY );

		BdvFunctions.showOverlay( selectedPointOverlay, "selected point overlay", BdvOptions.options().addTo( bdvHandle ).is2D() );
	}

	private void showFrameAndAxis()
	{
		ScatterPlotOverlay scatterPlotOverlay = new ScatterPlotOverlay( bdvHandle, columnNameX, columnNameY, dataPlotInterval, lineOverlay );

		BdvFunctions.showOverlay( scatterPlotOverlay, "scatter plot overlay", BdvOptions.options().addTo( bdvHandle ).is2D() );
	}

	private void installBdvBehaviours()
	{
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdvHandle.getTriggerbindings(), "plate viewer" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			showPopupMenu( x, y );
		}, "context menu", "button3" ) ; // "button1",
	}

	private void showPopupMenu( int x, int y )
	{
		final PopupMenu popupMenu = new PopupMenu();

		popupMenu.addPopupAction( "Focus closest point", e ->
		{
			new Thread( () -> {
				final RealPoint mouse3d = getViewerMouse3dPosition();
				search.search( mouse3d );
				final Integer rowIndex = search.getSampler().get();
				final T tableRow = tableRows.get( rowIndex );
				selectionModel.focus( tableRow );
			}).start();
		} );

		popupMenu.addPopupAction( "Change columns...", e ->
		{
			lineChoices = new String[]{ ScatterPlotOverlay.Y_NX, ScatterPlotOverlay.Y_1_2 };

			new Thread( () -> {
				final GenericDialog gd = new GenericDialog( "Column selection" );
				gd.addChoice( "Column X", columnNames, columnNameX );
				gd.addChoice( "Column Y", columnNames, columnNameY );
				gd.addChoice( "Add line", lineChoices, ScatterPlotOverlay.Y_NX );
				gd.showDialog();

				if ( gd.wasCanceled() ) return;

				columnNameX = gd.getNextChoice();
				columnNameY = gd.getNextChoice();
				lineOverlay = gd.getNextChoice();

				final int xLoc = SwingUtilities.getWindowAncestor( bdvHandle.getViewerPanel() ).getLocationOnScreen().x;
				final int yLoc = SwingUtilities.getWindowAncestor( bdvHandle.getViewerPanel() ).getLocationOnScreen().y;

				bdvHandle.close();

				createAndShowImage( xLoc, yLoc );

			}).start();
		} );


		popupMenu.show( bdvHandle.getViewerPanel().getDisplay(), x, y );
	}

	private RealPoint getViewerMouse3dPosition()
	{
		final RealPoint mouse2d = new RealPoint( 0, 0 );
		bdvHandle.getViewerPanel().getMouseCoordinates( mouse2d );
		final RealPoint mouse3d = new RealPoint( 3 );
		for ( int d = 0; d < 2; d++ )
		{
			mouse3d.setPosition( mouse2d.getDoublePosition( d ), d );
		}
		return mouse3d;
	}

	private RealPoint getMouseGlobal2dLocation()
	{
		final RealPoint global3dLocation = new RealPoint( 3 );
		bdvHandle.getViewerPanel().getGlobalMouseCoordinates( global3dLocation );
		final RealPoint dataPosition = new RealPoint( global3dLocation.getDoublePosition( 0 ), global3dLocation.getDoublePosition( 1 ) );
		return dataPosition;
	}

	public void fetchDataPoints( String columnNameX, String columnNameY )
	{
		points = new ArrayList<>();
		viewerPoints = new ArrayList<>();
		indices = new ArrayList<>();

		Double x, y;

		Double xMax=-Double.MAX_VALUE,yMax=-Double.MAX_VALUE,xMin=Double.MAX_VALUE,yMin=Double.MAX_VALUE;

		for ( int rowIndex = 0; rowIndex < numTableRows; rowIndex++ )
		{
			final T tableRow = tableRows.get( rowIndex );

			x = Utils.parseDouble( tableRow.getCell( columnNameX ) );
			if ( x.isNaN() ) continue;

			y = Utils.parseDouble( tableRow.getCell( columnNameY ) );
			if ( y.isNaN() ) continue;


			if ( tableRow instanceof Outlier )
			{
				if ( ( ( Outlier ) tableRow ).isOutlier() )
				{
					continue;
				}
			}

			points.add( new RealPoint( x, y, 0 ) );
			viewerPoints.add( new RealPoint( 0, 0, 0 ) );
			indices.add( rowIndex );
			if ( x > xMax ) xMax = x;
			if ( y > yMax ) yMax = y;
			if ( x < xMin ) xMin = x;
			if ( y < yMin ) yMin = y;
		}

		dataInterval = FinalRealInterval.createMinMax( xMin, yMin, 0, xMax, yMax, 0 );

		dataRanges = new double[ 2 ];
		for ( int d = 0; d < 2; d++ )
		{
			dataRanges[ d ] = dataInterval.realMax( d ) - dataInterval.realMin( d );
		}

		dataAspectRatio = dataRanges[ 1 ] / dataRanges[ 0 ];

	}

	public BiConsumer< RealLocalizable, IntType > createPlotFunction()
	{
		final double squaredViewerPointSize = viewerPointSize * viewerPointSize;
		final RealPoint dataPoint = new RealPoint( 0, 0, 0 );
		final RealPoint viewerPoint = new RealPoint( 0, 0, 0 );

		return ( p, t ) ->
		{
			synchronized ( this )
			{
				if ( viewerTransform == null ) return;

				for ( int d = 0; d < 2; d++ )
				{
					dataPoint.setPosition( p.getDoublePosition( d ), d );
				}

				viewerTransform.apply( dataPoint, viewerPoint );

				search.search( viewerPoint );

				final double sqrDistance = sqrDistance( viewerPoint, search.getPosition() );

				if ( sqrDistance < squaredViewerPointSize  )
				{
					t.set( search.getSampler().get() );
				}
				else
				{
					t.set( -1 );
				}
			}
		};
	}

	final public static double sqrDistance( final RealLocalizable position1, final RealLocalizable position2 )
	{
		double distSqr = 0;

		final int n = position1.numDimensions();
		for ( int d = 0; d < n; ++d )
		{
			final double pos = position2.getDoublePosition( d ) - position1.getDoublePosition( d );

			distSqr += pos * pos;
		}

		return distSqr;
	}

	private void showSource()
	{
		Prefs.showMultibox( false );

		scatterPlotBdvSource = BdvFunctions.show(
				argbSource,
				BdvOptions.options()
						.is2D()
						.frameTitle( plateName )
						.preferredSize( Utils.getBdvWindowSize(), Utils.getBdvWindowSize() )
						.transformEventHandlerFactory( new BehaviourTransformEventHandlerPlanar
						.BehaviourTransformEventHandlerPlanarFactory() ) );

		bdvHandle = scatterPlotBdvSource.getBdvHandle();

		scatterPlotBdvSource.setDisplayRange( 0, 255);
	}

	public void createSource( BiConsumer< RealLocalizable, IntType > biConsumer )
	{
		dataPlotInterval = Intervals.smallestContainingInterval( dataInterval );
		dataPlotInterval = Intervals.expand( dataPlotInterval, (int) ( 10 * viewerPointSize ) );

		// make 3D
		dataPlotInterval = FinalInterval.createMinMax(
				dataPlotInterval.min( 0 ),
				dataPlotInterval.min( 1 ),
				0,
				dataPlotInterval.max( 0 ),
				dataPlotInterval.max( 1 ),
				0 );

		final FunctionRealRandomAccessible< IntType > fra = new FunctionRealRandomAccessible< IntType >( 2, biConsumer, IntType::new );

		// make 3D
		final RealRandomAccessible< IntType > rra = RealViews.addDimension( fra );

		indexSource = new RealRandomAccessibleIntervalSource( rra, dataPlotInterval, new IntType(  ), name );

		//scatterSource.getInterpolatedSource(  )

		final ListItemsARGBConverter< T > converter =
				new ListItemsARGBConverter( tableRows, coloringModel );

		converter.getIndexToColor().put( -1, ColorUtils.getARGBType( Color.GRAY ).get() );

		argbSource = new ARGBConvertedRealAccessibleSource( indexSource, converter );
	}

	public void setViewerTransform()
	{
		viewerTransform = new AffineTransform3D();
		bdvHandle.getViewerPanel().getState().getViewerTransform( viewerTransform );

		AffineTransform3D reflectY = new AffineTransform3D();
		reflectY.set( -1.0, 1, 1 );
		viewerTransform.preConcatenate( reflectY );

		final AffineTransform3D scale = new AffineTransform3D();
		scale.scale( 0.8, 0.8 * viewerAspectRatio, 1.0  );
		viewerTransform.preConcatenate( scale );

		FinalRealInterval bounds = viewerTransform.estimateBounds( dataInterval );

		final AffineTransform3D translate = new AffineTransform3D();
		translate.translate( - ( bounds.realMin( 0 ) ), - ( bounds.realMin( 1 ) ), 0 );
		viewerTransform.preConcatenate( translate );
		bounds = viewerTransform.estimateBounds( dataInterval );

		final double[] translation = new double[ 2 ];
		translation[ 0 ] = 0.5 * ( BdvUtils.getBdvWindowWidth( bdvHandle ) - bounds.realMax( 0 ) );
		translation[ 1 ] = 0.5 * ( BdvUtils.getBdvWindowHeight( bdvHandle ) - bounds.realMax( 1 ));

		final AffineTransform3D translate2 = new AffineTransform3D();
		translate2.translate( translation[ 0 ], translation[ 1 ], 0 );
		viewerTransform.preConcatenate( translate2 );

		bdvHandle.getViewerPanel().setCurrentViewerTransform( viewerTransform );
	}

	public void show( JComponent component )
	{
		if ( component != null )
		{
			JFrame topFrame = ( JFrame ) SwingUtilities.getWindowAncestor( component );

			final int x = topFrame.getLocationOnScreen().x + component.getWidth() + 10;
			final int y = topFrame.getLocationOnScreen().y;
			createAndShowImage( x, y );
		}
		else
		{
			createAndShowImage( 10, 10 );
		}
	}

	public void setWindowPosition( int x, int y )
	{
		BdvUtils.getViewerFrame( bdvHandle ).setLocation( x, y );
	}
}
