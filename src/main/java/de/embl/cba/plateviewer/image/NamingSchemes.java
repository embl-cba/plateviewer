package de.embl.cba.plateviewer.image;

import de.embl.cba.plateviewer.mongo.OutlierStatus;
import de.embl.cba.plateviewer.table.AnnotatedIntervalTableRow;
import de.embl.cba.tables.tablerow.TableRow;

import java.util.List;

public abstract class NamingSchemes
{
	public static final String PATTERN_MD_A01_SITE_WAVELENGTH = ".*_([A-Z]{1}[0-9]{2})_s(.*)_w([0-9]{1}).*.tif";
	public static final String PATTERN_MD_A01_SITE = ".*_([A-Z]{1}[0-9]{2})_s([0-9]{1}).*.tif";
	public static final String PATTERN_MD_A01_WAVELENGTH = ".*_([A-Z]{1}[0-9]{2})_(.*).tif";
	public static final String PATTERN_ALMF_SCREENING_WELL_SITE_CHANNEL = ".*--W([0-9]{4})--P([0-9]{3}).*--C([0-9]{2}).*";
	public static final String PATTERN_ALMF_SCREENING_TREAT1_TREAT2_WELLNUM = ".*--(.*)--(.*)--W([0-9]{4})--.*";
	public static final String PATTERN_SCANR_WELLNUM_SITENUM_CHANNEL = ".*--W([0-9]{5})--P([0-9]{5}).*--.*--(.*)\\..*";
	public static final String PATTERN_SCANR_WELLNAME_WELLNUM = "(.*--W[0-9]{5})--.*\\..*";
	public static final String PATTERN_NIKON_TI2_HDF5 = ".*Well([A-Z]{1}[0-9]{2})_Point[A-Z]{1}[0-9]{2}_([0-9]{4})_.*h5$";

	public static abstract class BatchLibHdf5
	{
		public static final String OUTLIER = "is_outlier";
		public static final String WELL_NAME = "well_name";
		public static final String SITE_NAME = "site_name";

		public static String getDefaultColumnNameX( List< ? extends TableRow > siteTableRows )
		{
			if ( siteTableRows.get( 0 ).getColumnNames().contains( "serum_control_q0.5_of_cell_sums" ) )
				return "serum_control_q0.5_of_cell_sums";
			else
				throw new UnsupportedOperationException( "Default column not found!" );
		}

		public static String getDefaultColumnNameY( List< ? extends TableRow > siteTableRows )
		{
			if ( siteTableRows.get( 0 ).getColumnNames().contains( "serum_infected_q0.5_of_cell_sums" ) )
				return "serum_infected_q0.5_of_cell_sums";
			else
				throw new UnsupportedOperationException( "Default column not found!" );
		}


		public static String getDefaultColumnNameY()
		{
			return "infected_median";
		}

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
}
