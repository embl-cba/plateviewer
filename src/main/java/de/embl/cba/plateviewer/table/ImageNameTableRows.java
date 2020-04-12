package de.embl.cba.plateviewer.table;

import de.embl.cba.plateviewer.imagesources.ImageSourcesGeneratorCoronaHdf5;
import de.embl.cba.plateviewer.imagesources.NamingSchemes;
import de.embl.cba.tables.TableColumns;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageNameTableRows
{
	public static List< DefaultImageNameTableRow > imageNameTableRowsFromColumns(
			final Map< String, List< String > > columns,
			final String imageFileNameColumnName,
			String imageNamingScheme )
	{
		final List< DefaultImageNameTableRow > imageNameTableRows = new ArrayList<>();

		final int numRows = columns.values().iterator().next().size();

		for ( int row = 0; row < numRows; row++ )
		{
			String imageName = getImageName( columns, imageFileNameColumnName, imageNamingScheme, row );

			final DefaultImageNameTableRow imageNameTableRow
					= new DefaultImageNameTableRow(
							imageName,
							columns,
							row );

			imageNameTableRows.add( imageNameTableRow );
		}

		return imageNameTableRows;
	}

	public static String getImageName( Map< String, List< String > > columns, String imageFileNameColumnName, String imageNamingScheme, int row )
	{
		final String imageFileName = columns.get( imageFileNameColumnName ).get( row );

		if ( imageNamingScheme.equals( NamingSchemes.PATTERN_CORONA_HDF5 ) )
		{
			return ImageSourcesGeneratorCoronaHdf5.createImageName( imageFileName );
		}
		else
		{
			return imageFileName;
		}
	}

	public static List< DefaultImageNameTableRow > imageNameTableRowsFromFilePath( String filePath, String imageNamingScheme )
	{
		final Map< String, List< String > > columnNameToColumn = TableColumns.stringColumnsFromTableFile( filePath );

		return imageNameTableRowsFromColumns( columnNameToColumn, "image", imageNamingScheme );
	}
}
