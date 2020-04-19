package de.embl.cba.plateviewer.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils
{
	public static List< File > getFileList( File directory, String fileNameRegExp, boolean includeSubFolders )
	{
		final List< File > files = new ArrayList<>();
		populateFileList( directory, fileNameRegExp,files, includeSubFolders );
		return files;
	}

	private static void populateFileList( File directory, String fileNameRegExp, List< File > files, boolean includeSubFolders ) {

		// Get all the files from a directory.
		File[] fList = directory.listFiles();

		if( fList != null )
		{
			for ( File file : fList )
			{
				final Matcher matcher = Pattern.compile( fileNameRegExp ).matcher( file.getName() );

				if ( matcher.matches() )
				{
					files.add( file );
				}
				else
				{
					if ( includeSubFolders )
					{
						if ( file.isDirectory() )
						{
							populateFileList( file, fileNameRegExp, files, includeSubFolders );
						}
					}
				}
			}
		}
	}

	public static List< File > filterFiles( List< File > files, String filterPattern )
	{
		final List< File > filteredFiles = new ArrayList<>( );

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( filterPattern ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				filteredFiles.add( file );
			}

		}

		return filteredFiles;
	}
}
