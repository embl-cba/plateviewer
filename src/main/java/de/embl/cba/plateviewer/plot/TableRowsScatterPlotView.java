package de.embl.cba.plateviewer.plot;

import bdv.util.*;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.bdv.BehaviourTransformEventHandlerPlanar;
import de.embl.cba.plateviewer.image.table.ListItemsARGBConverter;
import de.embl.cba.plateviewer.table.DefaultSiteNameTableRow;
import de.embl.cba.plateviewer.view.PopupMenu;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.tablerow.TableRow;
import net.imglib2.*;
import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

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
	private ARGBConvertedRealSource source;
	private NearestNeighborSearchOnKDTree< Integer > search;
	private String columnNameX;
	private String columnNameY;

	public TableRowsScatterPlotView( List< T > tableRows, SelectionColoringModel< T > coloringModel, SelectionModel< T > selectionModel )
	{
		this.tableRows = tableRows;
		numTableRows = tableRows.size();
		this.coloringModel = coloringModel;
		this.selectionModel = selectionModel;
	}

	public void showScatterPlot( String columnNameX, String columnNameY )
	{
		this.columnNameX = columnNameX;
		this.columnNameY = columnNameY;

		setValuesAndSearch( columnNameX, columnNameY );

		BiConsumer< RealLocalizable, IntType > biConsumer = createFunction();

		createSource( biConsumer );

		showSource();

		final ScatterPlotOverlay overlay = new ScatterPlotOverlay( bdvHandle, columnNameX, columnNameY, scatterPlotInterval );

		BdvFunctions.showOverlay( overlay, "scatter plot overlay", BdvOptions.options().addTo( bdvHandle ).is2D() );

		SelectedPointOverlay selectedPointOverlay = new SelectedPointOverlay( bdvHandle, tableRows, selectionModel, points );

		BdvFunctions.showOverlay( selectedPointOverlay, "selected point overlay", BdvOptions.options().addTo( bdvHandle ).is2D() );


		installBdvBehaviours();
	}

	private void installBdvBehaviours()
	{
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdvHandle.getTriggerbindings(), "plate viewer" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			showPopupMenu( x, y );
		}, "context menu", "button1" ) ; // TODO: make button3
	}

	private void showPopupMenu( int x, int y )
	{
		final PopupMenu popupMenu = new PopupMenu();

		popupMenu.addPopupAction( "Focus closest point", e ->
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

		double x,y,xMax=-Double.MAX_VALUE,yMax=-Double.MAX_VALUE,xMin=Double.MAX_VALUE,yMin=Double.MAX_VALUE;

		for ( int rowIndex = 0; rowIndex < numTableRows; rowIndex++ )
		{
			x = Utils.parseDouble( tableRows.get( rowIndex ).getCell( columnNameX ) );
			y = Utils.parseDouble( tableRows.get( rowIndex ).getCell( columnNameY ) );
			points.add( new RealPoint( x, y ) );
			indices.add( rowIndex );
			if ( x > xMax ) xMax = x;
			if ( y > yMax ) yMax = y;
			if ( x < xMin ) xMin = x;
			if ( y < yMin ) yMin = y;
		}

		scatterPlotInterval = FinalInterval.createMinMax(
				(int) ( 0.9 * xMin ), (int)( 0.9 * yMin ), 0,
				(int) Math.ceil( 1.1 * xMax ), (int) Math.ceil( 1.1 * yMax ), 0 );

		// Give a copy because the order of the list is changed by the KDTree
		final ArrayList< RealPoint > copy = new ArrayList<>( points );
		final KDTree< Integer > kdTree = new KDTree<>( indices, copy );
		search = new NearestNeighborSearchOnKDTree<>( kdTree );

		pointSize = xMax / 500.0; // TODO: ?
	}

	public BiConsumer< RealLocalizable, IntType > createFunction()
	{
		double minDistanceSquared = pointSize * pointSize;

		return ( position, t ) ->
		{
			synchronized ( this )
			{
				search.search( position );
				final Sampler< Integer > sampler = search.getSampler();
				final Integer integer = sampler.get();

				// TODO: make point size dependend on ViewerTransform
				if ( search.getSquareDistance() < minDistanceSquared )
				{
					t.set( integer );
				}
				else
				{
					t.set( -1 );
				}
			}
		};
	}

	public void showSource()
	{
		final BdvStackSource< IntType > plot = BdvFunctions.show(
				source,
				BdvOptions.options().is2D().transformEventHandlerFactory(
				new BehaviourTransformEventHandlerPlanar
						.BehaviourTransformEventHandlerPlanarFactory() ) );

		bdvHandle = plot.getBdvHandle();

		plot.setDisplayRange( 0, 255);

		fixViewerTransform();
	}

	public void createSource( BiConsumer< RealLocalizable, IntType > biConsumer )
	{
		final FunctionRealRandomAccessible< IntType > fra = new FunctionRealRandomAccessible<>( 2, biConsumer, IntType::new );

		final RealRandomAccessible< IntType > rra = RealViews.addDimension( fra );

		final RealRandomAccessibleIntervalSource scatterSource = new RealRandomAccessibleIntervalSource( rra, scatterPlotInterval, new IntType(  ), "scatterPlot" );

		final ListItemsARGBConverter< DefaultSiteNameTableRow > argbConverter =
				new ListItemsARGBConverter( tableRows, coloringModel );

		source = new ARGBConvertedRealSource( scatterSource, argbConverter );
	}

	public void fixViewerTransform()
	{
		AffineTransform3D viewerTransform = new AffineTransform3D();
		bdvHandle.getViewerPanel().getState().getViewerTransform( viewerTransform  );

		AffineTransform3D reflectY = new AffineTransform3D();
		reflectY.set( -1.0, 1,1 );

		viewerTransform.concatenate( reflectY );
		final int height = bdvHandle.getViewerPanel().getHeight();
		viewerTransform.translate( 0, height, 0 ); // TODO: ??
		bdvHandle.getViewerPanel().setCurrentViewerTransform( viewerTransform );
	}
}
