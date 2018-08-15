package de.embl.cba.plateviewer;

import ij.IJ;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{

	public static final String WELL_PLATE_96 = "96 well plate";
	public static final String PATTERN_A01 = ".*_([A-Z]{1}[0-9]{2})_.*";
	public static final String PATTERN_W0001_P000 = ".*--W([0-9]{4})--P([0-9]{3})--.*";
	public static final String PATTERN_NO_MATCH = "PATTERN_NO_MATCH";
	final static String CAPITAL_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static void log( String msg )
	{
		IJ.log( msg );
	}

	public static void debug( String msg )
	{
		IJ.log( "[DEBUG] " + msg );
	}

	public static String getCellString( int[] cellPos )
	{
		return "" + cellPos[ 0] + "_" + cellPos[ 1 ];
	}

	public static ArrayList< File > getFiles( String directoryName, String fileNameRegExp )
	{
		final ArrayList< File > files = new ArrayList<>();
		populateFileList( directoryName, fileNameRegExp,files );
		return files;
	}

	public static void populateFileList( String directoryName, String fileNameRegExp, List<File> files) {

		File directory = new File( directoryName );

		// Get all the files from a directory.
		File[] fList = directory.listFiles();
		if( fList != null )
		{
			for (File file : fList)
			{
				if (file.isFile())
				{
					final Matcher matcher = Pattern.compile( fileNameRegExp ).matcher( file.getName() );

					if ( matcher.matches() )
					{
						files.add( file );
					}

				}
				else if (file.isDirectory())
				{
					populateFileList( file.getAbsolutePath(), fileNameRegExp, files );
				}
			}
		}
	}
}
