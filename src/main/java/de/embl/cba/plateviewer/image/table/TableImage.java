package de.embl.cba.plateviewer.image.table;

import bdv.util.BdvOverlay;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.image.channel.BdvViewable;
import de.embl.cba.plateviewer.table.DefaultSiteNameTableRow;
import de.embl.cba.plateviewer.view.ImagePlateViewer;
import de.embl.cba.tables.Tables;
import de.embl.cba.tables.color.ColorUtils;
import de.embl.cba.tables.color.SelectionColoringModel;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

public class TableImage implements BdvViewable
{
	private final List< DefaultSiteNameTableRow > tableRows;
	private final SelectionColoringModel< DefaultSiteNameTableRow > coloringModel;
	private final ImagePlateViewer imagePlateViewer;
	private Interval plateInterval;
	private final long[] siteDimensions;
	private RandomAccessibleInterval< FloatType > rai;
	private double[] contrastLimits;
	private final String[][] siteNameMatrix;
	private HashMap< String, DefaultSiteNameTableRow > siteNameToTableRow;
	private HashMap< String, Integer > siteNameToTableRowIndex;
	private final JTable jTable;
	private ARGBConvertedRealSource argbSource;

	public TableImage(
			List< DefaultSiteNameTableRow > tableRows,
			SelectionColoringModel< DefaultSiteNameTableRow > coloringModel,
			ImagePlateViewer imagePlateViewer )
	{
		this.tableRows = tableRows;
		this.coloringModel = coloringModel;
		this.imagePlateViewer = imagePlateViewer;

		jTable = Tables.jTableFromTableRows( tableRows );

		plateInterval = imagePlateViewer.getPlateInterval();
		siteDimensions = imagePlateViewer.getSiteDimensions();
		siteNameMatrix = imagePlateViewer.getSiteNameMatrix();

		createSiteNameToTableRowMap( tableRows );

		contrastLimits = new double[ 2 ];

		createImage();
	}

	public void createSiteNameToTableRowMap( List< DefaultSiteNameTableRow > tableRows )
	{
		siteNameToTableRow = new HashMap<>();
		siteNameToTableRowIndex = new HashMap();

		int rowIndex = 0;
		for ( DefaultSiteNameTableRow tableRow : tableRows )
		{
			siteNameToTableRow.put( tableRow.getSiteName(), tableRow );
			siteNameToTableRowIndex.put( tableRow.getSiteName(), rowIndex++ );
		}
	}

	private void createImage( )
	{
		// TODO: below code could be optimised by precomputing a tableRowIndexMatrix
		BiConsumer< Localizable, IntType > biConsumer = ( l, t ) ->
		{
			t.setInteger( ListItemsARGBConverter.OUT_OF_BOUNDS_ROW_INDEX );

			final int siteRowIndex = ( int ) ( l.getIntPosition( 0 ) / siteDimensions[ 0 ] );
			final int siteColumnIndex = ( int ) ( l.getIntPosition( 1 ) / siteDimensions[ 1 ] );

			if ( siteRowIndex < 0
					|| siteRowIndex >= siteNameMatrix.length
					|| siteColumnIndex < 0
					|| siteColumnIndex >= siteNameMatrix[ 0 ].length )
				return;

			final String siteName = siteNameMatrix[ siteRowIndex ][ siteColumnIndex ];

			if ( siteName == null ) return;

			t.setInteger( siteNameToTableRowIndex.get( siteName ) );
		};

		final FunctionRandomAccessible< FloatType > randomAccessible =
				new FunctionRandomAccessible( 2, biConsumer, IntType::new );

		rai = Views.interval( randomAccessible, plateInterval );

		rai = Views.addDimension( rai, 0, 0 );

		final RandomAccessibleIntervalSource< FloatType > tableRowIndexSource
				= new RandomAccessibleIntervalSource<>( rai, Util.getTypeFromInterval( rai ), "table row index" );

		final ListItemsARGBConverter< DefaultSiteNameTableRow > argbConverter =
				new ListItemsARGBConverter<>( tableRows, coloringModel );

		argbSource = new ARGBConvertedRealSource( tableRowIndexSource , argbConverter );

		contrastLimits[ 0 ] = 0; //Tables.columnMin( jTable, jTable.getColumnModel().getColumnIndex( columnName ) );
		contrastLimits[ 1 ] = 255; //Tables.columnMax( jTable, jTable.getColumnModel().getColumnIndex( columnName ) );
	}

	@Override
	public String getName()
	{
		return "table values";
	}

	@Override
	public ARGBType getColor()
	{
		return ColorUtils.getARGBType( Color.GRAY );
	}

	@Override
	public double[] getContrastLimits()
	{
		return contrastLimits;
	}

	@Override
	public RandomAccessibleInterval< ? > getRAI()
	{
		return rai;
	}

	@Override
	public Source< ? > getSource()
	{
		return argbSource;
	}

	@Override
	public BdvOverlay getOverlay()
	{
		return null;
	}

	@Override
	public boolean isInitiallyVisible()
	{
		return true;
	}

	@Override
	public Metadata.Type getType()
	{
		return Metadata.Type.Image;
	}
}
