package de.embl.cba.plateviewer.table;

import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.tables.tablerow.AbstractTableRow;
import net.imglib2.Interval;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultAnnotatedIntervalTableRow extends AbstractTableRow implements AnnotatedIntervalTableRow
{
	protected final Interval interval;
	protected String outlierColumnName;
	protected final Map< String, List< String > > columns;
	protected final String siteName;
	protected final int rowIndex;

	public DefaultAnnotatedIntervalTableRow(
			String siteName,
			Interval interval,
			String outlierColumnName,
			Map< String, List< String > > columns,
			int rowIndex )
	{
		this.siteName = siteName;
		this.interval = interval;
		this.outlierColumnName = outlierColumnName;
		this.columns = columns;
		this.rowIndex = rowIndex;
	}

	@Override
	public Interval getInterval()
	{
		return interval;
	}

	@Override
	public String getName()
	{
		return siteName;
	}

	@Override
	public boolean isOutlier()
	{
		if ( ! columns.containsKey( outlierColumnName  ) ) return false;

		final String s = columns.get( outlierColumnName ).get( rowIndex );
		return NamingSchemes.BatchLibHdf5.isOutlier( s );
	}

	@Override
	public void setOutlier( boolean isOutlier )
	{
		if ( columns.containsKey( outlierColumnName ))
			setCell( outlierColumnName, NamingSchemes.BatchLibHdf5.getOutlierString( isOutlier ) );
	}

	@Override
	public String getAnnotation()
	{
		return "Not annotated";
	}

	@Override
	public void setAnnotation( String annotation )
	{

	}

	@Override
	public String getCell( String columnName )
	{
		return columns.get( columnName ).get( rowIndex );
	}

	@Override
	public void setCell( String columnName, String value )
	{
		columns.get( columnName ).set( rowIndex, value );
		notifyCellChangedListeners( columnName, value );
	}

	@Override
	public Set< String > getColumnNames()
	{
		return columns.keySet();
	}

	@Override
	public int rowIndex()
	{
		return rowIndex;
	}
}
