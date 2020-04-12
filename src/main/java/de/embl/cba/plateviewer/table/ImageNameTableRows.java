package de.embl.cba.plateviewer.table;

import de.embl.cba.plateviewer.source.ChannelSourcesGeneratorCoronaHdf5;
import de.embl.cba.plateviewer.source.NamingSchemes;
import de.embl.cba.tables.TableColumns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageNameTableRows
{
	public static List< DefaultSiteNameTableRow > createSiteNameTableRowsFromColumns(
			final Map< String, List< String > > columns,
			final String siteNameColumnName )
	{
		final List< DefaultSiteNameTableRow > siteNameTableRows = new ArrayList<>();

		final int numRows = columns.values().iterator().next().size();

		for ( int row = 0; row < numRows; row++ )
		{
			siteNameTableRows.add(
					new DefaultSiteNameTableRow(
						columns.get( siteNameColumnName ).get( row ),
						columns,
						row )
			);
		}

		return siteNameTableRows;
	}

	public static List< DefaultSiteNameTableRow > createSiteNameTableRowsFromFilePath( String filePath, String imageNamingScheme )
	{
		final Map< String, List< String > > columnNameToColumn = TableColumns.stringColumnsFromTableFile( filePath );

		if ( imageNamingScheme.equals( NamingSchemes.PATTERN_CORONA_HDF5 ) )
		{
			addSiteNameColumn( columnNameToColumn );
			return createSiteNameTableRowsFromColumns( columnNameToColumn, "site-name" );
		}
		else
		{
			throw new UnsupportedOperationException( "Appending a table for naming scheme " + imageNamingScheme + " is not yet supported.");
		}
	}

	public static String addSiteNameColumn( Map< String, List< String > > columnNameToColumn )
	{
		final int numRows = columnNameToColumn.values().iterator().next().size();

		final List< String > siteNameColumn = new ArrayList<>();

		for ( int rowIndex = 0; rowIndex < numRows; rowIndex++ )
		{
			final String imageFileName = columnNameToColumn.get( "image" ).get( rowIndex ) + ".h5";
			final String siteName = ChannelSourcesGeneratorCoronaHdf5.createSiteName( imageFileName );
			siteNameColumn.add( siteName );
		}

		columnNameToColumn.put( "site-name", siteNameColumn );

		return "site-name";
	}
}
