package de.embl.cba.plateviewer.image;

import de.embl.cba.plateviewer.image.table.TableRowsIntervalImage;
import de.embl.cba.plateviewer.table.AnnotatedIntervalTableRow;
import de.embl.cba.plateviewer.table.DefaultAnnotatedIntervalTableRow;
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

	public static abstract class ColumnNamesBatchLibHdf5
	{
		public static final String OUTLIER = "is_outlier";
		public static final String WELL_NAME = "well_name";
		public static final String SITE_NAME = "site_name";

		public static String getDefaultColumnNameX( List< ? extends TableRow > siteTableRows )
		{
			String defaultColumnNameX = "not_infected_median";
			if ( siteTableRows.get( 0 ).getColumnNames().contains( "control_median" ) )
				defaultColumnNameX = "control_median";
			return defaultColumnNameX;
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
	}
}
