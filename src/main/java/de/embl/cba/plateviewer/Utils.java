package de.embl.cba.plateviewer;

import ij.IJ;

public class Utils
{

	public static final String WELL_PLATE_96 = "96 well plate";
	public static final String PATTERN_A01_ = ".*_([A-Z]{1}[0-9]{2})_.*";
	public static final String PATTERN_NO_MATCH = "PATTERN_NO_MATCH";
	final static String CAPITAL_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static void log( String msg )
	{
		IJ.log( msg );
	}

	public static String getCellString( int[] cellPos )
	{
		return "" + cellPos[ 0] + "_" + cellPos[ 1 ];
	}
}
