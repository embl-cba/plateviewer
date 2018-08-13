package de.embl.cba.plateviewer;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class CellFileMapGenerator
{
	public static final String WELL_PLATE_96 = "96 well plate";
	public static final String NAMING_SCHEME_MOLECULAR_DEVICES = "Molecular Devices";
	public static final String WELL = "--WELL--";

	final static String capitalAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static Map< long[], String > createCellFileMap( String plateType,
														   String namingScheme,
														   String directory,
														   String fileNamePattern )
	{
		final long[] dimensions = getDimensions( plateType );

		final Map< long[], String > cellFileMap = new LinkedHashMap<>();

		for ( long x = 0; x < dimensions[ 0 ]; ++x )
		{
			for( long y = 0; y < dimensions[ 1 ]; ++ y )
			{

				final String rowColumn = getRowColumn( x, y, namingScheme );

				String filename = fileNamePattern.replace( WELL, rowColumn );

				cellFileMap.put( new long[]{ x, y }, directory + "/" + filename );
			}
		}

		180730-Nup93-mEGFP-clone79-imaging-pipeline_H02_w2.tif

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
