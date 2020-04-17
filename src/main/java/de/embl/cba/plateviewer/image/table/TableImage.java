package de.embl.cba.plateviewer.image.table;

import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.converters.SelectableVolatileARGBConverter;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.image.img.BDViewable;
import de.embl.cba.plateviewer.table.DefaultSiteNameTableRow;
import de.embl.cba.plateviewer.view.PlateViewerImageView;
import de.embl.cba.tables.Tables;
import de.embl.cba.tables.color.ColorUtils;
import de.embl.cba.tables.color.LabelsARGBConverter;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class TableImage implements BDViewable
{
	private final List< DefaultSiteNameTableRow > tableRows;
	private final LazyCategoryColoringModel< DefaultSiteNameTableRow > coloringModel;
	private final PlateViewerImageView plateViewerImageView;
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
			LazyCategoryColoringModel< DefaultSiteNameTableRow > coloringModel,
			PlateViewerImageView plateViewerImageView )
	{
		this.tableRows = tableRows;
		this.coloringModel = coloringModel;
		this.plateViewerImageView = plateViewerImageView;

		jTable = Tables.jTableFromTableRows( tableRows );

		plateInterval = plateViewerImageView.getPlateInterval();
		siteDimensions = plateViewerImageView.getSiteDimensions();
		siteNameMatrix = plateViewerImageView.getSiteNameMatrix();

		createSiteNameToTableRowMap( tableRows );

		contrastLimits = new double[ 2 ];
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

	public void createImage( String columnName )
	{
		BiConsumer< Localizable, FloatType > biConsumer = ( l, t ) ->
		{
			try
			{
				final int siteRowIndex = ( int ) ( l.getIntPosition( 0 ) / siteDimensions[ 0 ] );
				final int siteColumnIndex = ( int ) ( l.getIntPosition( 1 ) / siteDimensions[ 1 ] );
				final String siteName = this.siteNameMatrix[ siteRowIndex ][ siteColumnIndex ];
				if ( siteName == null ) return;
//				final String value = siteNameToTableRow.get( siteName ).getCell( columnName );
				final Integer integer = siteNameToTableRowIndex.get( siteName );
				t.setReal( integer );
			}
			catch ( Exception e )
			{
				//
			}
		};

		final Supplier< FloatType > typeSupplier = () -> new FloatType();

		final FunctionRandomAccessible< FloatType > randomAccessible =
				new FunctionRandomAccessible( 2, biConsumer, typeSupplier );

		rai = Views.interval( randomAccessible, plateInterval );

		final RandomAccessibleIntervalSource< FloatType > tableRowIndexSource
				= new RandomAccessibleIntervalSource<>( rai, Util.getTypeFromInterval( rai ), "table row index" );

		final SitesARGBConverter< DefaultSiteNameTableRow > argbConverter = new SitesARGBConverter<>( tableRows, coloringModel );

		argbSource = new ARGBConvertedRealSource( tableRowIndexSource , argbConverter );

		contrastLimits[ 0 ] = Tables.columnMin( jTable, jTable.getColumnModel().getColumnIndex( columnName ) );
		contrastLimits[ 1 ] = Tables.columnMax( jTable, jTable.getColumnModel().getColumnIndex( columnName ) );
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
