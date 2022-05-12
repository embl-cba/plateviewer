package de.embl.cba.plateviewer.image;

import de.embl.cba.plateviewer.mongo.OutlierStatus;
import de.embl.cba.plateviewer.table.AnnotatedIntervalTableRow;
import de.embl.cba.plateviewer.table.IntervalType;
import de.embl.cba.plateviewer.table.TableSource;
import de.embl.cba.tables.tablerow.TableRow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class NamingSchemes
{
	public static final String WELL = "W";
	public static final String SITE = "S";
	public static final String CHANNEL = "C";
	
	public static final String PATTERN_MD_A01_SITE_WAVELENGTH = ".*_(?<"+WELL+">[A-Z]{1}[0-9]{2})_s(?<"+SITE+">.*)_w(?<"+CHANNEL+">[0-9]{1}).*";
	public static final String PATTERN_MD_A01_SITE = ".*_(?<"+WELL+">[A-Z]{1}[0-9]{2})_s(?<"+SITE+">[0-9]{1}).*";
	public static final String PATTERN_MD_A01_WAVELENGTH = ".*_(?<"+WELL+">[A-Z]{1}[0-9]{2})_(?<"+CHANNEL+">.*).tif";
	public static final String PATTERN_ALMF_TREAT1_TREAT2_WELLNUM_POSNUM_CHANNEL = ".*--(.*)--(.*)--W(?<"+WELL+">[0-9]{4})--P(?<"+SITE+">[0-9]{3})--T[0-9]{4,5}--Z[0-9]{3}--(?<"+CHANNEL+">.*)";
	public static final String PATTERN_SCANR_WELLNUM_SITENUM_CHANNEL = ".*--W(?<"+WELL+">[0-9]{5})--P(?<"+SITE+">[0-9]{5}).*--.*--(?<"+CHANNEL+">.*)\\..*";
	public static final String PATTERN_NIKON_TI2_HDF5 = ".*Well([A-Z]{1}[0-9]{2})_Point[A-Z]{1}[0-9]{2}_([0-9]{4})_.*h5$";
	

	public static String getDefaultColumnNameX( List< ? extends TableRow > tableRows )
	{
		final Set< String > columnNames = tableRows.get( 0 ).getColumnNames();

		if ( columnNames.contains( "n_cells" ) )
			return "n_cells";
		else if ( columnNames.contains( CHANNEL + "ount_Cells" ) )
			return CHANNEL + "ount_Cells";
		else if ( columnNames.contains( "Metadata_WellFolder" ) )
			return "Metadata_WellFolder";
		else
			return (String) new ArrayList( columnNames ).get( 0 );
	}

	public static String getDefaultColumnNameY( List< ? extends TableRow > tableRows )
	{
		final Set< String > columnNames = tableRows.get( 0 ).getColumnNames();

		if ( columnNames.contains( "score" ) )
			return "score";
		else if ( columnNames.contains( "Metadata_WellFolder" ) )
			return "Metadata_WellFolder";
		else if ( columnNames.contains( CHANNEL + "ount_Cells" ) )
			return CHANNEL + "ount_Cells";
		else
			return (String) new ArrayList( columnNames ).get( 1 );
			//throw new UnsupportedOperationException( "Default column not found!" );
	}

	public static String getNamingScheme( File file )
	{
		String filePath = file.getAbsolutePath();

		if ( Pattern.compile( PATTERN_NIKON_TI2_HDF5 ).matcher( filePath ).matches() )
			return PATTERN_NIKON_TI2_HDF5;
		else if ( Pattern.compile( PATTERN_MD_A01_SITE_WAVELENGTH ).matcher( filePath ).matches() )
			return PATTERN_MD_A01_SITE_WAVELENGTH;
		else if ( Pattern.compile( PATTERN_MD_A01_SITE ).matcher( filePath ).matches() )
			return PATTERN_MD_A01_SITE;
		else if ( Pattern.compile( PATTERN_MD_A01_WAVELENGTH ).matcher( filePath ).matches() )
			return PATTERN_MD_A01_WAVELENGTH;
		else if ( Pattern.compile( PATTERN_ALMF_TREAT1_TREAT2_WELLNUM_POSNUM_CHANNEL ).matcher( filePath ).matches() )
			return PATTERN_ALMF_TREAT1_TREAT2_WELLNUM_POSNUM_CHANNEL;
		else if ( Pattern.compile( PATTERN_SCANR_WELLNUM_SITENUM_CHANNEL ).matcher( filePath ).matches() )
			return PATTERN_SCANR_WELLNUM_SITENUM_CHANNEL;
		else
			throw new UnsupportedOperationException( CHANNEL + "ould not match file name: " + file );
	}

	public static abstract class BatchLibHdf5
	{
		public static final String outlierColumnName = "is_outlier";
		public static final String WELL_NAME = "well_name";
		public static final String SITE_NAME = "site_name";

		public static final Function< String, Boolean > stringToOutlier = s -> s.equals( "1" ) ? true : false;
		public static final Function< Boolean, String > outlierToString = b -> b ? "1" : "0";

		public static String getIntervalName( String hdf5Group )
		{
			String intervalName = null;
			if ( hdf5Group.contains( "images" ) )
				intervalName = SITE_NAME; // ensureSiteNameColumn( columnNameToColumn );
			else if ( hdf5Group.contains( "wells" ) )
				intervalName = WELL_NAME;
			return intervalName;
		}

		public static String getOutlierString( boolean isOutlier )
		{
			return isOutlier ? "1" : "0";
		}

		public static OutlierStatus getOutlierEnum( boolean isOutlier )
		{
			return isOutlier ? OutlierStatus.OUTLIER : OutlierStatus.VALID;
		}

		public static boolean isOutlier( String s )
		{
			return s.equals( "1" ) ? true : false;
		}

		public static boolean isOutlier( Integer s )
		{
			return s == 1 ? true : false;
		}
	}


	public static abstract class ALMFScreening
	{
		public static final String qcColumnName = "Q" + CHANNEL;

		public static final Function< String, Boolean > stringToOutlier = s -> s.equals( "1" ) ? false : true;

		public static final Function< Boolean, String > outlierToString = b -> b ? "0" : "1";

		public static String getFileNameColumn( Map< String, List< String > > columnNameToColumn )
		{
			String fileNameColumnName = null;
			for ( String columName : columnNameToColumn.keySet() )
			{
				if ( columName.toLowerCase().contains( "filename" ) )
				{
					fileNameColumnName = columName;
					final String siteName = MultiWellChannelFilesProviderALMFScreening.getSiteName( columnNameToColumn.get( columName ).get( 0 ) );

					if ( siteName != null )
					{
						fileNameColumnName = columName;
						break;
					}
					else continue;
				}
			}

			if ( fileNameColumnName == null )
			{
				throw new RuntimeException( CHANNEL + "ould not find a column that would allow to construct proper site names." );
			}
			return fileNameColumnName;
		}

		public static String addIntervalNameColumn( TableSource tableSource, Map< String, List< String > > columnNameToColumn )
		{
			String fileNameColumnName = getFileNameColumn( columnNameToColumn );

			final ArrayList< String > intervalNameColumn = new ArrayList<>();
			final List< String > fileNames = columnNameToColumn.get( fileNameColumnName );

			if ( tableSource.intervalType.equals( IntervalType.Sites ) )
			{
				for ( String fileName : fileNames )
				{
					intervalNameColumn.add( MultiWellChannelFilesProviderALMFScreening.getSiteName( fileName ) );
				}
				columnNameToColumn.put( "site_name", intervalNameColumn );
				return "site_name";
			}
			else if ( tableSource.intervalType.equals( IntervalType.Wells ))
			{
				for ( String fileName : fileNames )
				{
					intervalNameColumn.add( MultiWellChannelFilesProviderALMFScreening.getWellName( fileName ) );
				}
				columnNameToColumn.put( "well_name", intervalNameColumn );
				return "well_name";
			}
			else
			{
				throw new RuntimeException( );
			}
		}
	}
}
