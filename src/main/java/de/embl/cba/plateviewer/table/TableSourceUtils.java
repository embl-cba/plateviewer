package de.embl.cba.plateviewer.table;

import ij.IJ;

import java.io.File;

public class TableSourceUtils
{
	public static File getTableFileBatchLibHdf5( File imagesDirectory, String plateName )
	{
		File tableFile;

		// try all the different conventions
		//
		tableFile = new File( imagesDirectory, plateName + "_table.hdf5" );
		if ( tableFile.exists() ) return tableFile;

		tableFile = new File( imagesDirectory, plateName + "_table_serum_IgG_corrected.hdf5" );
		if ( tableFile.exists() ) return tableFile;

		tableFile = new File( imagesDirectory, plateName + "_analysis.csv" );
		if ( tableFile.exists() ) return tableFile;

		tableFile = new File( imagesDirectory, "analysis.csv" );
		if ( tableFile.exists() ) return tableFile;

		tableFile = new File( imagesDirectory, plateName + "_table_serum_corrected.hdf5" );
		if ( tableFile.exists() ) return tableFile;

		// nothing worked => ask user
		//
		final String tableFilePath = IJ.getFilePath( "Please select table file" );
		if ( tableFilePath != null )
			return new File( tableFilePath );
		else
			return null;
	}
}
