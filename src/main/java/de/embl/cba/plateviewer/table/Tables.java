package de.embl.cba.plateviewer.table;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.plateviewer.image.MultiWellChannelFilesProviderBatchLibHdf5;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.mongo.AssayMetadataRepository;
import de.embl.cba.tables.TableColumns;
import net.imglib2.Interval;

import java.io.File;
import java.util.*;

public class Tables
{
	public static List< ? extends AnnotatedIntervalTableRow > createDefaultAnnotatedIntervalTableRowsFromColumns(
			final Map< String, List< String > > columns,
			final String intervalNameColumnName,
			final Map< String, Interval > nameToInterval,
			final String outlierColumnName )
	{
		final List< DefaultAnnotatedIntervalTableRow > tableRows = new ArrayList<>();

		if ( ! columns.containsKey( intervalNameColumnName ) )
		{
			throw new UnsupportedOperationException( "Table does not contain required column: " + intervalNameColumnName );
		}

		final int numRows = columns.values().iterator().next().size();
		for ( int row = 0; row < numRows; row++ )
		{
			final String intervalName = columns.get( intervalNameColumnName ).get( row );

			Interval interval = null;
			if ( nameToInterval != null )
				interval = nameToInterval.get( intervalName );

			tableRows.add(
					new DefaultAnnotatedIntervalTableRow(
							intervalName,
							interval,
							outlierColumnName,
							columns,
							row )
			);
		}

		return tableRows;
	}

	public static List< ? extends AnnotatedIntervalTableRow > createAnnotatedIntervalTableRowsFromColumnsAndRepository(
			final Map< String, List< String > > columns,
			final String intervalNameColumnName,
			final Map< String, Interval > nameToInterval,
			AssayMetadataRepository repository )
	{
		final List< RepositoryAnnotatedIntervalTableRow > tableRows = new ArrayList<>();
		final int numRows = columns.values().iterator().next().size();
		for ( int row = 0; row < numRows; row++ )
		{
			final String siteName = columns.get( intervalNameColumnName ).get( row );

			Interval interval = null;
			if ( nameToInterval != null )
				interval = nameToInterval.get( siteName );

			tableRows.add(
					new RepositoryAnnotatedIntervalTableRow(
							siteName,
							interval,
							columns,
							row,
							repository)
			);
		}

		return tableRows;
	}

	public static List< ? extends AnnotatedIntervalTableRow >
	createAnnotatedIntervalTableRowsFromFileAndRepository(
			String filePath,
			String imageNamingScheme,
			Map< String, Interval > nameToInterval,
			String hdf5Group,
			AssayMetadataRepository repository // optional, can be null
	 		)
	{
		final Map< String, List< String > > columnNameToColumn;

		if ( filePath.endsWith( ".csv" ) )
		{
			columnNameToColumn = TableColumns.stringColumnsFromTableFile( filePath );
		}
		else if ( filePath.endsWith( ".hdf5" ) || filePath.endsWith( ".h5" ) )
		{
			columnNameToColumn =
					Tables.stringColumnsFromHDF5(
						filePath,
						hdf5Group );
		}
		else
		{
			throw new UnsupportedOperationException( "Table file extension not supported: " + filePath );
		}

		if ( imageNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			if ( repository != null )
			{
				final String plateName = new File( new File( filePath ).getParent() ).getName();
				repository.setPlateName( plateName );

				final List< ? extends AnnotatedIntervalTableRow > fromColumnsAndRepository = createAnnotatedIntervalTableRowsFromColumnsAndRepository(
						columnNameToColumn,
						NamingSchemes.BatchLibHdf5.getIntervalName( hdf5Group ),
						nameToInterval,
						repository );

				return fromColumnsAndRepository;

			}
			else
			{
				return createDefaultAnnotatedIntervalTableRowsFromColumns(
							columnNameToColumn,
							NamingSchemes.BatchLibHdf5.getIntervalName( hdf5Group ),
							nameToInterval,
							NamingSchemes.BatchLibHdf5.OUTLIER );
			}
		}
		else
		{
			throw new UnsupportedOperationException( "Appending a table for naming scheme " + imageNamingScheme + " is not yet supported.");
		}
	}

	public static String ensureSiteNameColumn( Map< String, List< String > > columnNameToColumn )
	{
		if ( columnNameToColumn.keySet().contains( "site_name" ) )
		{
			return "site_name";
		}
		else if ( columnNameToColumn.keySet().contains( "site-name" ) )
		{
			return "site-name";
		}
		else
		{
			final int numRows = columnNameToColumn.values().iterator().next().size();

			final List< String > siteNameColumn = new ArrayList<>();

			for ( int rowIndex = 0; rowIndex < numRows; rowIndex++ )
			{
				final String imageFileName = columnNameToColumn.get( "image" ).get( rowIndex ) + ".h5";
				final String siteName = MultiWellChannelFilesProviderBatchLibHdf5.createSiteName( imageFileName );
				siteNameColumn.add( siteName );
			}

			columnNameToColumn.put( "site_name", siteNameColumn );

			return "site_name";
		}
	}

	public static Map< String, List< String > > stringColumnsFromHDF5( final String filePath, String tableGroup )
	{
		final IHDF5Reader hdf5Reader = HDF5Factory.openForReading( filePath );
		final List< String > groupMembers = hdf5Reader.getGroupMembers( "/" );
		final List< String > columnNames = new ArrayList<>( Arrays.asList( hdf5Reader.string().readMDArray( tableGroup + "/columns" ).getAsFlatArray() ) );

		final byte[] visible = hdf5Reader.uint8().readArray( tableGroup + "/visible" );

		final Map< String, List< String > > columnNameToStrings = new LinkedHashMap<>();
		final int numColumns = columnNames.size();
		for ( int columnIndex = 0; columnIndex < numColumns; columnIndex++ )
		{
			if ( visible[ columnIndex ] == 0 ) continue;
			final String columnName = columnNames.get( columnIndex );
			columnNameToStrings.put( columnName, new ArrayList<>( ) );
		}

		// add column content
		final String[] cells = hdf5Reader.string().readMDArray( tableGroup + "/cells" ).getAsFlatArray();
		final int numRows = cells.length / columnNames.size();

		for ( int columnIndex = 0; columnIndex < numColumns; columnIndex++ )
		{
			if ( visible[ columnIndex ] == 0 ) continue;
			final String columName = columnNames.get( columnIndex );
			final List< String > strings = columnNameToStrings.get( columName );

			for ( int rowIndex = 0; rowIndex < numRows; ++rowIndex )
			{
				strings.add( cells[ rowIndex * numColumns + columnIndex ] );
			}
		}

		if ( hdf5Reader.hasAttribute( "/", "batchlib_commit" ) )
		{
			final String batchlibCommit = hdf5Reader.string().getAttr( "/", "batchlib_commit" );

			final ArrayList< String > versions = new ArrayList<>();
			for ( int rowIndex = 0; rowIndex < numRows; ++rowIndex )
			{
				versions.add( batchlibCommit );
			}
			columnNameToStrings.put( "version", versions );
		}

		// System.out.println( ( System.currentTimeMillis() - start ) / 1000.0 ) ;

		return columnNameToStrings;
	}

}
