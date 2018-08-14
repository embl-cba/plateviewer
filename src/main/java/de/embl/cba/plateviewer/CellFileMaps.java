package de.embl.cba.plateviewer;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CellFileMaps
{

	public static ArrayList< Map< String, File > > create( String directoryName )
	{
		final ArrayList< Map< String, File > > cellFileMaps = new ArrayList<>(  );
		cellFileMaps.add( new HashMap<>() );

		final ArrayList< File > files = Utils.getFiles( directoryName );

		for ( File file : files )
		{
			final String pattern = getPattern( file );

			final String cell = Utils.getCellString( getCell( file, pattern ) );

			putCellToMaps( cellFileMaps, cell, file );

		}

		return cellFileMaps;

	}

	public static void putCellToMaps( ArrayList< Map< String, File > > cellFileMaps,
									  String cell,
									  File file )
	{
		boolean cellCouldBePlaceInExistingMap = false;

		for( int iMap = 0; iMap < cellFileMaps.size(); ++iMap )
		{
			if ( !cellFileMaps.get( iMap ).containsKey( cell ) )
			{
				cellFileMaps.get( iMap ).put( cell, file );
				cellCouldBePlaceInExistingMap = true;
				break;
			}
		}

		if ( ! cellCouldBePlaceInExistingMap )
		{
			// new channel
			cellFileMaps.add( new HashMap<>() );
			cellFileMaps.get( cellFileMaps.size() - 1 ).put( cell, file );
		}
	}

	public static String getPattern( File file )
	{
		String filePath = file.getAbsolutePath();

		if ( Pattern.compile( Utils.PATTERN_A01_ ).matcher( filePath ).matches() ) return Utils.PATTERN_A01_;

		return Utils.PATTERN_NO_MATCH;
	}


	public static int[] getCell( File file, String pattern )
	{
		String filePath = file.getAbsolutePath();

		final Matcher matcher = Pattern.compile( pattern ).matcher( filePath );

		if ( matcher.matches() )
		{
			String position = matcher.group( 1 );

			final int[] xy = new int[ 2 ];
			switch ( pattern )
			{
				case Utils.PATTERN_A01_:
					xy[ 0 ] = Integer.parseInt( position.substring( 1, 3 ) ) - 1;
					xy[ 1 ] = Utils.CAPITAL_ALPHABET.indexOf( position.substring( 0, 1 ) );
					break;
			}

			return xy;

		}
		else
		{
			return null;
		}

	}


	private static long[] getDimensions( String plateType )
	{
		long[] dimensions = new long[ 2 ];

		switch ( plateType )
		{
			case Utils.WELL_PLATE_96:
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
