package de.embl.cba.plateviewer.table;

import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.mongo.AssayMetadataRepository;
import de.embl.cba.plateviewer.mongo.OutlierStatus;
import de.embl.cba.tables.tablerow.AbstractTableRow;
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
		if ( Arrays.stream( AssayMetadataRepository.attributes ).anyMatch( columnName::equals ) )
		{
			return repository.getSiteOrWellAttribute( this.name, columnName );
		}
		else
		{
			return columns.get( columnName ).get( rowIndex );
		}
	}

	@Override
	public void setCell( String columnName, String value )
	{
		if ( Arrays.stream( AssayMetadataRepository.attributes ).anyMatch( columnName::equals ) )
		{
			// TODO
		}
		else
		{
			columns.get( columnName ).set( rowIndex, value );
		}
		notifyCellChangedListeners( columnName, value );
	}

	@Override
	public Set< String > getColumnNames()
	{
		if ( columnNames == null )
		{
			columnNames = new LinkedHashSet<>( columns.keySet() );
			for ( String attribute : AssayMetadataRepository.attributes )
			{
				columnNames.add( attribute );
			}
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
	public boolean isOutlier()
	{
		final String outlier = repository.getSiteOrWellAttribute( this.getName(), AssayMetadataRepository.dbOutlier );
		final boolean booleanOutlier = NamingSchemes.BatchLibHdf5.isOutlier( outlier );
		return booleanOutlier;
	}

	@Override
	public void setOutlier( boolean isOutlier )
	{
		final OutlierStatus outlierStatus = NamingSchemes.BatchLibHdf5.getOutlierEnum( isOutlier );
		switch ( repository.getDefaultTableType() )
		{
			case Well:
				repository.updateWellQC( repository.getDefaultPlateName(), this.getName(), outlierStatus, "manual" );
				break;
			case Image:
				repository.updateImageQC( repository.getDefaultPlateName(), this.getName(), outlierStatus, "manual" );
				break;
		}

		notifyCellChangedListeners( AssayMetadataRepository.dbOutlier, NamingSchemes.BatchLibHdf5.getOutlierString( isOutlier ) );
	}
}
