package de.embl.cba.plateviewer;

import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.mongo.AssayMetadataRepository;
import de.embl.cba.plateviewer.table.AnnotatedInterval;
import de.embl.cba.plateviewer.table.AnnotatedIntervalCreatorAndAdder;
import de.embl.cba.plateviewer.table.BatchLibHdf5CellFeatureProvider;
import de.embl.cba.plateviewer.table.DefaultAnnotatedIntervalTableRow;
import de.embl.cba.plateviewer.view.ImagePlateViewer;
import ij.IJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;

import static de.embl.cba.plateviewer.mongo.AssayMetadataRepository.getCovid19AssayMetadataRepository;

public class PlateViewer < R extends NativeType< R > & RealType< R >, T extends AnnotatedInterval >
{
	private final File imagesDirectory;
	private final String filePattern;
	private final boolean loadSiteTable;
	private final boolean loadWellTable;
	private final boolean connectToDatabase;
	private final int numIoThreads;
	private final boolean includeSubFolders;

	public PlateViewer( File plateDirectory, String filePattern, boolean loadSiteTable, boolean loadWellTable, boolean connectToDatabase, int numIoThreads, boolean includeSubFolders )
	{
		this.imagesDirectory = plateDirectory;
		this.filePattern = filePattern;
		this.loadSiteTable = loadSiteTable;
		this.loadWellTable = loadWellTable;
		this.connectToDatabase = connectToDatabase;
		this.numIoThreads = numIoThreads;
		this.includeSubFolders = includeSubFolders;

		final ImagePlateViewer< R, DefaultAnnotatedIntervalTableRow > imageView =
				new ImagePlateViewer(
						plateDirectory.getAbsolutePath(),
						filePattern,
						numIoThreads,
						includeSubFolders );

		final BatchLibHdf5CellFeatureProvider valueProvider = new BatchLibHdf5CellFeatureProvider( plateDirectory.getAbsolutePath() );

		imageView.setCellFeatureProvider( valueProvider );

		new Thread( () ->
		{
			IJ.wait( 5000 );

			if ( loadSiteTable )
			{
				addTable( imageView, "tables/images/default", AnnotatedIntervalCreatorAndAdder.IntervalType.Sites );
			}

			if ( loadWellTable )
			{
				addTable( imageView, "tables/wells/default", AnnotatedIntervalCreatorAndAdder.IntervalType.Wells );
			}
		}).start();
	}

	public void addTable( ImagePlateViewer< R, DefaultAnnotatedIntervalTableRow > imageView, String tableName, AnnotatedIntervalCreatorAndAdder.IntervalType intervalType )
	{
		final String fileNamingScheme = imageView.getFileNamingScheme();
		File tableFile = getTableFile( fileNamingScheme );

		AnnotatedIntervalCreatorAndAdder intervalCreatorAndAdder = getAnnotatedIntervalCreatorAndAdder( imageView, fileNamingScheme, tableFile );

		intervalCreatorAndAdder.createAndAddAnnotatedIntervals( intervalType, tableName );
	}

	public AnnotatedIntervalCreatorAndAdder getAnnotatedIntervalCreatorAndAdder( ImagePlateViewer< R, DefaultAnnotatedIntervalTableRow > imageView, String fileNamingScheme, File tableFile )
	{
		AnnotatedIntervalCreatorAndAdder intervalCreatorAndAdder;
		if ( connectToDatabase )
		{
			final AssayMetadataRepository repository = getCovid19AssayMetadataRepository( "covid" + ( 2500 + 81 ) );
			intervalCreatorAndAdder = new AnnotatedIntervalCreatorAndAdder( imageView, fileNamingScheme, tableFile, repository );
		}
		else
		{
			intervalCreatorAndAdder = new AnnotatedIntervalCreatorAndAdder( imageView, fileNamingScheme, tableFile );
		}
		return intervalCreatorAndAdder;
	}

	public File getTableFile( String fileNamingScheme )
	{
		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			final String plateName = imagesDirectory.getName();
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
		else
		{
			throw new UnsupportedOperationException( "Cannot yet load tables for naming scheme: " + fileNamingScheme );
		}

	}
}
