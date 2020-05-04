package de.embl.cba.plateviewer.table;

import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.mongo.AssayMetadataRepository;
import de.embl.cba.tables.select.Listeners;
import de.embl.cba.tables.tablerow.AbstractTableRow;
import de.embl.cba.tables.tablerow.TableRowListener;
import net.imglib2.Interval;

import java.util.*;

public class RepositoryAnnotatedIntervalTableRow extends AbstractTableRow implements AnnotatedIntervalTableRow
{
	private final String name;
	private final Interval interval;
	private final Map< String, List< String > > columns;
	private final int rowIndex;
	private final AssayMetadataRepository repository;
	private String annotation;
	private HashSet< String > columnNames;
	private Boolean isOutlier;

	public RepositoryAnnotatedIntervalTableRow(
			String name,
			Interval interval,
			Map< String, List< String > > columns,
			int rowIndex,
			AssayMetadataRepository repository )
	{
		this.name = name;
		this.interval = interval;
		this.columns = columns;
		this.rowIndex = rowIndex;
		this.repository = repository;
	}

	@Override
	public String getCell( String columnName )
	{
		if ( columnName.equals( AssayMetadataRepository.repositoryOutlierColumnName ) )
			return NamingSchemes.BatchLibHdf5.getOutlierString( isOutlier() );
		else if ( columnName.equals( AssayMetadataRepository.cohortIdColumnName ) )
			return getAnnotation();
		else
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
		if ( columnNames == null )
		{
			columnNames = new LinkedHashSet<>( columns.keySet() );
			columnNames.add( AssayMetadataRepository.repositoryOutlierColumnName );
			columnNames.add( AssayMetadataRepository.cohortIdColumnName );
		}

		return columnNames;
	}


	@Override
	public int rowIndex()
	{
		return rowIndex;
	}

	@Override
	public Interval getInterval()
	{
		return interval;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getAnnotation()
	{
		if ( annotation == null )
		{
			try
			{
				annotation = repository.getManualAssessment( repository.getDefaultPlateName(), this.getName() );
			} catch ( Exception e )
			{
				annotation = "DB connection failed";
			}
		}

		return annotation;
	}

	@Override
	public void setAnnotation( String annotation )
	{
		// TODO: not needed
	}

	@Override
	public boolean isOutlier()
	{
		if ( isOutlier == null )
			isOutlier = false; // TODO: get from database

		return isOutlier;
	}

	@Override
	public void setOutlier( boolean isOutlier )
	{
		this.isOutlier = isOutlier;
		// TODO: set in database
		notifyCellChangedListeners( AssayMetadataRepository.cohortIdColumnName, NamingSchemes.BatchLibHdf5.getOutlierString( isOutlier ) );
	}
}
