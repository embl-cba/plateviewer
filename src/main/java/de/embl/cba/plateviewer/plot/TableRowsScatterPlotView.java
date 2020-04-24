package de.embl.cba.plateviewer.plot;

import bdv.util.*;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.bdv.BehaviourTransformEventHandlerPlanar;
import de.embl.cba.plateviewer.image.table.ListItemsARGBConverter;
import de.embl.cba.plateviewer.table.DefaultSiteNameTableRow;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.tablerow.TableRow;
import net.imglib2.*;
import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.BiConsumer;

public class TableRowsScatterPlotView< T extends TableRow >
{
	public static final String IMAGE_NAME = "plate outlines";
	private final List< T > tableRows;
	private FinalInterval scatterPlotInterval;
	private RandomAccessibleInterval< UnsignedShortType > rai;
	private double[] contrastLimits = new double[ 2 ];
	private RandomAccessibleIntervalSource< UnsignedShortType > source;
	private FunctionRealRandomAccessible< UnsignedShortType > rra;
	private int numTableRows;
	private final LazyCategoryColoringModel< DefaultSiteNameTableRow > coloringModel;
	private final SelectionColoringModel< DefaultSiteNameTableRow > selectionColoringModel;
	private BdvHandle bdvHandle;

	public TableRowsScatterPlotView( List< T > tableRows, LazyCategoryColoringModel< DefaultSiteNameTableRow > coloringModel, SelectionColoringModel< DefaultSiteNameTableRow > selectionColoringModel )
	{
		this.tableRows = tableRows;
		numTableRows = tableRows.size();
		this.coloringModel = coloringModel;
		this.selectionColoringModel = selectionColoringModel;
	}

	public void showScatterPlot( String columnNameX, String columnNameY )
	{
		final ArrayList< RealPoint > points = new ArrayList<>();
		final ArrayList< Integer > indices = new ArrayList<>();

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

		final KDTree< Integer > kdTree = new KDTree<>( indices, points );
		final NearestNeighborSearchOnKDTree< Integer > search = new NearestNeighborSearchOnKDTree<>( kdTree );

		double pointSize = xMax / 500.0; // TODO: ?

		double minDistanceSquared = pointSize * pointSize;

		BiConsumer< RealLocalizable, IntType > biConsumer = ( position, t ) ->
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

		final FunctionRealRandomAccessible< IntType > fra = new FunctionRealRandomAccessible<>( 2, biConsumer, IntType::new );

		final RealRandomAccessible< IntType > rra = RealViews.addDimension( fra );

		final RealRandomAccessibleIntervalSource scatterSource = new RealRandomAccessibleIntervalSource( rra, scatterPlotInterval, new IntType(  ), "scatterPlot" );

		final ListItemsARGBConverter< DefaultSiteNameTableRow > argbConverter =
				new ListItemsARGBConverter( tableRows, coloringModel );

		final ARGBConvertedRealSource argbScatterSource = new ARGBConvertedRealSource( scatterSource, argbConverter );

		final BdvStackSource< IntType > plot = BdvFunctions.show(
				argbScatterSource,
				BdvOptions.options().is2D().transformEventHandlerFactory(
				new BehaviourTransformEventHandlerPlanar
						.BehaviourTransformEventHandlerPlanarFactory() ) );

		bdvHandle = plot.getBdvHandle();

		plot.setDisplayRange( 0, 255);

		fixViewerTransform();

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
