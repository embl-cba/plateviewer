package de.embl.cba.plateviewer.table;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.plateviewer.image.MultiWellChannelFilesProviderBatchLibHdf5;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.mongo.AssayMetadataRepository;
import de.embl.cba.tables.TableColumns;
import net.imglib2.Interval;
import org.bson.Document;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class Tables
{
	public static List< ? extends AnnotatedIntervalTableRow > createDefaultAnnotatedIntervalTableRowsFromColumns(
			final Map< String, List< String > > columns,
			final String intervalNameColumnName,
			final Map< String, Interval > nameToInterval,
			final String outlierColumnName,
			Function< String, Boolean > stringToOutlier,
			Function< Boolean, String > outlierToString )
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
			{
				interval = nameToInterval.get( intervalName );
			}

			tableRows.add(
					new DefaultAnnotatedIntervalTableRow(
							intervalName,
							interval,
							outlierColumnName,
							stringToOutlier,
							outlierToString,
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
			TableSource tableSource,
			String namingScheme,
			Map< String, Interval > nameToInterval,
			AssayMetadataRepository repository // optional, can be null
	)
	{
		final Map< String, List< String > > columnNameToColumn;

		if ( tableSource.filePath.endsWith( ".csv" ) || tableSource.filePath.endsWith( ".txt" ) )
		{
			columnNameToColumn = TableColumns.stringColumnsFromTableFile( tableSource.filePath );
		}
		else if ( tableSource.filePath.endsWith( ".hdf5" ) || tableSource.filePath.endsWith( ".h5" ) )
		{
			columnNameToColumn = Tables.stringColumnsFromHDF5( tableSource.filePath, tableSource.hdf5Group );
		}
		else
		{
			throw new UnsupportedOperationException( "Table file extension not supported: " + tableSource );
		}

		if ( namingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			final String plateName = new File( new File( tableSource.filePath ).getParent() ).getName();
			boolean hasRepository = hasRepository( repository, plateName );

			if ( hasRepository )
			{
				final List< ? extends AnnotatedIntervalTableRow > fromColumnsAndRepository = createAnnotatedIntervalTableRowsFromColumnsAndRepository(
						columnNameToColumn,
						NamingSchemes.BatchLibHdf5.getIntervalName( tableSource.hdf5Group ),
						nameToInterval,
						repository );

				return fromColumnsAndRepository;
			}
			else
			{
				return createDefaultAnnotatedIntervalTableRowsFromColumns(
							columnNameToColumn,
							NamingSchemes.BatchLibHdf5.getIntervalName( tableSource.hdf5Group ),
							nameToInterval,
							NamingSchemes.BatchLibHdf5.outlierColumnName,
							NamingSchemes.BatchLibHdf5.stringToOutlier,
						    NamingSchemes.BatchLibHdf5.outlierToString );
			}
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_ALMF_TREAT1_TREAT2_WELLNUM_POSNUM_CHANNEL ) )
		{
			final String intervalNameColumn = NamingSchemes.ALMFScreening.addIntervalNameColumn( tableSource, columnNameToColumn );

			if ( ! columnNameToColumn.containsKey( NamingSchemes.ALMFScreening.qcColumnName ) )
			{
				throw new RuntimeException( "Table must contain column: " + NamingSchemes.ALMFScreening.qcColumnName );
			}

			// TODO: make an interface and classes for the outlier stuff
			return createDefaultAnnotatedIntervalTableRowsFromColumns(
					columnNameToColumn,
					intervalNameColumn,
					nameToInterval,
					NamingSchemes.ALMFScreening.qcColumnName,
					NamingSchemes.ALMFScreening.stringToOutlier,
					NamingSchemes.ALMFScreening.outlierToString );
		}
		else
		{
			throw new UnsupportedOperationException( "Appending a table for naming scheme " + namingScheme + " is not yet supported.");
		}
	}

	public static boolean hasRepository( AssayMetadataRepository repository, String plateName )
	{
		boolean hasRepository = false;

		if ( repository != null )
		{
			repository.setPlateName( plateName );

			final Document plateDocument = repository.getPlateDocument( repository.getPlateName() );
			if ( plateDocument != null )
			{
				hasRepository = true;
			}
		}
		return hasRepository;
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

		if ( hdf5Reader.object().hasAttribute( "/", "batchlib_commit" ) )
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
