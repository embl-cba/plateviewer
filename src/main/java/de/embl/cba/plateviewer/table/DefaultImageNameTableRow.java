package de.embl.cba.plateviewer.table;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultImageNameTableRow implements ImageNameTableRow
{
	private final Map< String, List< String > > columns;
	private final String imageNameColumnName;
	private final int rowIndex;

	public DefaultImageNameTableRow( Map< String, List< String > > columns, String imageNameColumnName, int rowIndex )
	{
		this.columns = columns;
		this.imageNameColumnName = imageNameColumnName;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getImageName()
	{
		return columns.get( imageNameColumnName ).get( rowIndex );
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
