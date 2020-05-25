package de.embl.cba.plateviewer;

import de.embl.cba.plateviewer.image.NamingSchemes;
import de.embl.cba.plateviewer.mongo.AssayMetadataRepository;
import de.embl.cba.plateviewer.table.*;
import ij.IJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;

import static de.embl.cba.plateviewer.mongo.AssayMetadataRepository.getCovid19AssayMetadataRepository;
import static de.embl.cba.plateviewer.table.IntervalType.*;

public class PlateViewerInitializer< R extends NativeType< R > & RealType< R >, T extends AnnotatedInterval >
{
	private final File imagesDirectories;
	private final String filePattern;
	private final boolean loadSiteTable;
	private final boolean loadWellTable;
	private final boolean connectToDatabase;
	private final int numIoThreads;
	private final boolean includeSubFolders;
	private String namingScheme;
	private TableSource siteTableSource;
	private TableSource wellTableSource;
	private String additionalImagesDirectory;

	public PlateViewerInitializer( File imagesDirectory, String filePattern, boolean loadSiteTable, boolean loadWellTable, boolean connectToDatabase, int numIoThreads, boolean includeSubFolders )
	{
		this.imagesDirectories = imagesDirectory;
		this.filePattern = filePattern;
		this.loadSiteTable = loadSiteTable;
		this.loadWellTable = loadWellTable;
		this.connectToDatabase = connectToDatabase;
		this.numIoThreads = numIoThreads;
		this.includeSubFolders = includeSubFolders;
	}

	public void run()
	{
		final PlateViewer< R, DefaultAnnotatedIntervalTableRow > plateViewer =
				new PlateViewer(
						imagesDirectories.getAbsolutePath(),
						filePattern,
						numIoThreads,
						includeSubFolders );

		if ( additionalImagesDirectory != null )
			plateViewer.getInputDirectories().add( additionalImagesDirectory );

		plateViewer.run();

		namingScheme = plateViewer.getNamingScheme();

		if ( plateViewer.getNamingScheme().equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5  ) )
		{
			final BatchLibHdf5CellFeatureProvider valueProvider =
					new BatchLibHdf5CellFeatureProvider( imagesDirectories.getAbsolutePath(), plateViewer.getSiteFiles() );

			plateViewer.setCellFeatureProvider( valueProvider );
		}

		new Thread( () ->
		{
			IJ.wait( 3000 );

			if ( loadSiteTable )
			{
				if ( siteTableSource == null )
					siteTableSource = getTableSource( namingScheme, imagesDirectories, Sites );
				addTable( plateViewer, siteTableSource );
			}

			if ( loadWellTable )
			{
				wellTableSource = getTableSource( namingScheme, imagesDirectories, Wells );
				addTable( plateViewer, wellTableSource );
			}
		}).start();
	}

	public void setSiteTableSource( TableSource siteTableSource )
	{
		this.siteTableSource = siteTableSource;
	}

	public void setWellTableSource( TableSource wellTableSource )
	{
		this.wellTableSource = wellTableSource;
	}

	public void addTable( PlateViewer< R, DefaultAnnotatedIntervalTableRow > imageView, TableSource tableSource )
	{
		AnnotatedIntervalCreatorAndAdder intervalCreatorAndAdder = getAnnotatedIntervalCreatorAndAdder( imageView, namingScheme, tableSource );

		intervalCreatorAndAdder.createAndAddAnnotatedIntervals( );
	}

	public AnnotatedIntervalCreatorAndAdder getAnnotatedIntervalCreatorAndAdder( PlateViewer< R, DefaultAnnotatedIntervalTableRow > imageView, String fileNamingScheme, TableSource tableSource )
	{
		if ( connectToDatabase )
		{
			final AssayMetadataRepository repository = getCovid19AssayMetadataRepository( "covid" + ( 2500 + 81 ) );
			return new AnnotatedIntervalCreatorAndAdder( imageView, fileNamingScheme, tableSource, repository );
		}
		else
		{
			return new AnnotatedIntervalCreatorAndAdder( imageView, fileNamingScheme, tableSource );
		}
	}

	public static TableSource getTableSource( String namingScheme, File imagesDirectory, IntervalType intervalType )
	{
		final TableSource tableSource = new TableSource();
		tableSource.intervalType = intervalType;

		final String plateName = imagesDirectory.getName();

		if ( namingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			final File tableFile = TableSourceUtils.getTableFileBatchLibHdf5( imagesDirectory, plateName );

			tableSource.filePath = tableFile.getAbsolutePath();

			switch ( intervalType )
			{
				case Sites:
					tableSource.hdf5Group = "tables/images/default";
					break;
				case Wells:
					tableSource.hdf5Group = "tables/wells/default";
					break;
				default:
					throw new UnsupportedOperationException();
			}

			return tableSource;
		}
		else
		{
			final String tableFilePath = IJ.getFilePath( "Please select table file" );
			if ( tableFilePath != null )
			{
				tableSource.filePath = tableFilePath;
				return tableSource;
			}
			else
			{
				return null;
			}
		}
	}

	public void addInputImagesDirectory( String additionalImagesDirectory )
	{
		this.additionalImagesDirectory = additionalImagesDirectory;
	}
}
