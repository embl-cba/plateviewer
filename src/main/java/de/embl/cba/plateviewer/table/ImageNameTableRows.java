package de.embl.cba.plateviewer.table;

import de.embl.cba.tables.TableColumns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageNameTableRows
{
	public static List< DefaultImageNameTableRow > imageNameTableRowsFromColumns(
			final Map< String, List< String > > columns,
			final String imageNameColumnName )
	{
		final List< DefaultImageNameTableRow > imageNameTableRows = new ArrayList<>();

		final int numRows = columns.values().iterator().next().size();

		for ( int row = 0; row < numRows; row++ )
		{
			final DefaultImageNameTableRow imageNameTableRow
					= new DefaultImageNameTableRow(
							columns,
							imageNameColumnName,
							row );

			imageNameTableRows.add( imageNameTableRow );
		}

		return imageNameTableRows;
	}

	public static List< DefaultImageNameTableRow > imageNameTableRowsFromFilePath( String filePath )
	{
		final Map< String, List< String > > columnNameToColumn = TableColumns.stringColumnsFromTableFile( filePath );

		return imageNameTableRowsFromColumns( columnNameToColumn, "image" );
	}
}
