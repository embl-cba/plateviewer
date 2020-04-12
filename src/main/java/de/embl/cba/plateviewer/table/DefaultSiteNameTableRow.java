package de.embl.cba.plateviewer.table;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultSiteNameTableRow implements SiteNameTableRow
{
	private final Map< String, List< String > > columns;
	private final String siteName;
	private final int rowIndex;

	public DefaultSiteNameTableRow( String siteName, Map< String, List< String > > columns, int rowIndex )
	{
		this.siteName = siteName;
		this.columns = columns;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getSiteName()
	{
		return siteName;
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
