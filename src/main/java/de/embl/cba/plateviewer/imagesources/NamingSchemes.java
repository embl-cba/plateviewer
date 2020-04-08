package de.embl.cba.plateviewer.imagesources;

public abstract class NamingSchemes
{
	public static final String PATTERN_MD_A01_SITE_WAVELENGTH = ".*_([A-Z]{1}[0-9]{2})_s(.*)_w([0-9]{1}).*.tif";
	public static final String PATTERN_MD_A01_SITE = ".*_([A-Z]{1}[0-9]{2})_s([0-9]{1}).*.tif";
	public static final String PATTERN_MD_A01_WAVELENGTH = ".*_([A-Z]{1}[0-9]{2})_(.*).tif";
	public static final String PATTERN_ALMF_SCREENING_WELL_SITE_CHANNEL = ".*--W([0-9]{4})--P([0-9]{3}).*--C([0-9]{2}).*";
	public static final String PATTERN_ALMF_SCREENING_TREAT1_TREAT2_WELLNUM = ".*--(.*)--(.*)--W([0-9]{4})--.*";
	public static final String PATTERN_SCANR_WELL_SITE_CHANNEL = ".*--W([0-9]{5})--P([0-9]{5}).*--.*--(.*)\\..*";
	public static final String PATTERN_SCANR_WELLNAME_WELLNUM = "(.*--W[0-9]{5})--.*\\..*";
	public static final String PATTERN_CORONA = ".*Well([A-Z]{1}[0-9]{2})_Point[A-Z]{1}[0-9]{2}_([0-9]{4})_.*h5$";
}
