package de.embl.cba.plateviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CellFileMapGenerator
{
	public static final String WELL_PLATE_96 = "96 well plate";
	public static final String NAMING_SCHEME_MOLECULAR_DEVICES = "Molecular Devices";
	public static final String WELL = "--WELL--";

	public static final String PATTERN_A01_ = ".*_([A-Z]{1}[0-9]{2})_.*";

	final static String capitalAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String PATTERN_NO_MATCH = "PATTERN_NO_MATCH";

	public static Map< long[], String > createCellFileMap( String plateType,
														   String directoryName )
	{

		final long[] dimensions = getDimensions( plateType );

		final ArrayList< File > files = getFiles( directoryName );

		for ( File file : files )
		{
			final String pattern = getPattern( file );
			getPosition( file, pattern );


		}

		String pattern_A01_ = ;

		if ( matcher.matches() )


		final Map< long[], String > cellFileMap = new LinkedHashMap<>();

		for ( long x = 0; x < dimensions[ 0 ]; ++x )
		{
			for( long y = 0; y < dimensions[ 1 ]; ++ y )
			{
				fileName = fileName.replaceFirst(
						"<C(\\d+)-(\\d+)>",
						String.format("%1$0" + ctzPad[0] + "d", c));

				final String rowColumn = getRowColumn( x, y, namingScheme );

				String filename = fileNamePattern.replace( WELL, rowColumn );

				cellFileMap.put( new long[]{ x, y }, directory + "/" + filename );
			}
		}

		180730-Nup93-mEGFP-clone79-imaging-pipeline_H02_w2.tif

	}

	public static String getPattern( File file )
	{
		String filePath = file.getAbsolutePath();

		if ( Pattern.compile( PATTERN_A01_ ).matcher( filePath ).matches() ) return PATTERN_A01_;

		return PATTERN_NO_MATCH;
	}


	public static long[] getPosition( File file, String pattern )
	{
		String filePath = file.getAbsolutePath();
		final Matcher matcher = Pattern.compile( pattern ).matcher( filePath );
		String position = matcher.group( 1 );
		
		final long[] xy = new long[ 2 ];
		switch ( pattern )
		{
			case PATTERN_A01_:
				xy[ 0 ] = capitalAlphabet.indexOf( position.substring( 0, 0 ) );
				xy[ 1 ] = Integer.parseInt( position.substring( 1, 2 ) );
				break;
		}

		return xy;
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

	private static String getRowColumn( long x, long y, String namingScheme )
	{

		switch ( namingScheme )
		{
			case NAMING_SCHEME_MOLECULAR_DEVICES:

				Character row = capitalAlphabet.charAt( (int) y );
				String column = String.format( "%02d", x );
				String rowColumn = row + column;
				return rowColumn;

			default:

				return null;

		}

	}

	private static long[] getDimensions( String plateType )
	{
		long[] dimensions = new long[ 2 ];

		switch ( plateType )
		{
			case WELL_PLATE_96:
				dimensions[ 0 ] = 12;
				dimensions[ 1 ] = 8;
				break;
			default:
				dimensions[ 0 ] = 12;
				dimensions[ 1 ] = 8;
		}

		return dimensions;
	}
}
