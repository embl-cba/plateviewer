package de.embl.cba.plateviewer;

import bdv.viewer.ViewerPanel;
import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.image.table.TableRowsSitesImage;
import de.embl.cba.plateviewer.table.AnnotatedInterval;
import de.embl.cba.plateviewer.table.AnnotatedIntervalCreatorAndAdder;
import de.embl.cba.plateviewer.table.DefaultAnnotatedIntervalTableRow;
import de.embl.cba.plateviewer.view.ImagePlateViewer;
import de.embl.cba.tables.color.ColoringLuts;
import de.embl.cba.tables.color.NumericColoringModelDialog;
import de.embl.cba.tables.view.TableRowsTableView;
import ij.IJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.awt.*;
import java.io.File;
import java.util.Set;

public class PlateViewer < R extends NativeType< R > & RealType< R >, T extends AnnotatedInterval >
{
	private final File imagesDirectory;


	public PlateViewer( File imagesDirectory, String filePattern, boolean loadSiteTable, int numIoThreads, boolean includeSubFolders )
	{
		this.imagesDirectory = imagesDirectory;

		final ImagePlateViewer< R, DefaultAnnotatedIntervalTableRow > imageView =
				new ImagePlateViewer(
						imagesDirectory.toString(),
						filePattern,
						numIoThreads,
						includeSubFolders );

		if ( loadSiteTable )
		{
			addSiteTable( imageView );
		}
	}

	public void addSiteTable( ImagePlateViewer< R, DefaultAnnotatedIntervalTableRow > imageView )
	{
		final String fileNamingScheme = imageView.getFileNamingScheme();
		File tableFile = getTableFile( fileNamingScheme );

		final AnnotatedIntervalCreatorAndAdder intervalCreatorAndAdder =
				new AnnotatedIntervalCreatorAndAdder( imageView, fileNamingScheme, tableFile );

		intervalCreatorAndAdder.createAndAddAnnotatedIntervals( "tables/images/default" );
	}


	public File getTableFile( String fileNamingScheme )
	{
		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			final String plateName = imagesDirectory.getName();
			File tableFile;

			// try all the different conventions
			//
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
		else
		{
			throw new UnsupportedOperationException( "Cannot yet load tables for naming scheme: " + fileNamingScheme );
		}

	}
}
