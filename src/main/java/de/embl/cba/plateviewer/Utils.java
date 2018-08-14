package de.embl.cba.plateviewer;

import ij.IJ;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

	public static ArrayList< File > getFiles( String directoryName )
	{
		final ArrayList< File > files = new ArrayList<>();
		populateFileList( directoryName, files );
		return files;
	}

	public static void populateFileList( String directoryName, List<File> files) {

		File directory = new File( directoryName );

		// Get all the files from a directory.
		File[] fList = directory.listFiles();
		if( fList != null )
		{
			for (File file : fList)
			{
				if (file.isFile())
				{
					files.add(file);
				}
				else if (file.isDirectory())
				{
					populateFileList( file.getAbsolutePath(), files );
				}
			}
		}
	}
}
