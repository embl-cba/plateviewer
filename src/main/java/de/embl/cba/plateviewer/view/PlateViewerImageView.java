package de.embl.cba.plateviewer.view;

import bdv.util.*;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.converters.RandomARGBConverter;
import de.embl.cba.bdv.utils.measure.PixelValueStatistics;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.github.IssueRaiser;
import de.embl.cba.plateviewer.github.PlateLocation;
import de.embl.cba.plateviewer.image.channel.BdvViewable;
import de.embl.cba.plateviewer.image.source.MultiResolutionBatchLibHdf5ChannelSourceCreator;
import de.embl.cba.plateviewer.image.well.OutlinesImage;
import de.embl.cba.plateviewer.image.well.OverlayBdvViewable;
import de.embl.cba.plateviewer.image.well.WellNamesOverlay;
import de.embl.cba.plateviewer.io.FileUtils;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.bdv.BdvSiteAndWellNamesOverlay;
import de.embl.cba.plateviewer.bdv.BehaviourTransformEventHandlerPlanar;
import de.embl.cba.plateviewer.image.*;
import de.embl.cba.plateviewer.image.channel.MultiWellImg;
import de.embl.cba.plateviewer.image.channel.MultiWellImagePlusImg;
import de.embl.cba.plateviewer.table.DefaultSiteNameTableRow;
import de.embl.cba.plateviewer.table.SiteName;
import de.embl.cba.plateviewer.view.panel.PlateViewerMainPanel;
import de.embl.cba.tables.Logger;
import de.embl.cba.tables.color.LazyLabelsARGBConverter;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.SelectionListener;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.view.TableRowsTableView;
import ij.gui.GenericDialog;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.util.Intervals;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlateViewerImageView < R extends NativeType< R > & RealType< R >, T extends SiteName >
{
	private final ArrayList< MultiWellImg > multiWellImgs;
	private int numIoThreads;
	private final SharedQueue loadingQueue;

	private Bdv bdv;
	private PlateViewerMainPanel plateViewerMainPanel;
	private List< T > siteNames;
	private SelectionModel< T > selectionModel;
	private final String fileNamingScheme;
	private Interval plateInterval;
	private HashMap< String, Interval > wellNameToInterval;
	private HashMap< String, Interval > siteNameToInterval;
	private HashMap< Interval, String > intervalToSiteName;
	private String[][] siteNameMatrix;

	private boolean isFirstChannel = true;
	private long[] siteDimensions;
	private long[] wellDimensions;
	private String plateName;
	private TableRowsTableView< DefaultSiteNameTableRow > tableView;

	public PlateViewerImageView( String inputDirectory, String filterPattern, int numIoThreads )
	{
		this( inputDirectory, filterPattern, numIoThreads, true );
	}

	public PlateViewerImageView( String inputDirectory, String filterPattern, int numIoThreads, boolean includeSubFolders )
	{
		this.plateName = new File( inputDirectory ).getName();
		this.multiWellImgs = new ArrayList<>();
		this.numIoThreads = numIoThreads;
		this.loadingQueue = new SharedQueue( numIoThreads );

		final List< File > fileList = getFiles( inputDirectory, filterPattern, includeSubFolders );

		fileNamingScheme = getImageNamingScheme( fileList );

		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
			this.numIoThreads = 1; // Hdf5 does not support multi-threading

		final List< String > channelPatterns =
				Utils.getChannelPatterns( fileList, fileNamingScheme );

		Logger.info( "Detected channels: " );
		for ( String channelPattern : channelPatterns )
		{
			Logger.info( "- " + channelPattern );
		}

		addChannels( fileList, fileNamingScheme, channelPatterns );

		addWellOutlinesImages();

		addSiteAndWellNamesOverlay( multiWellImgs.get( 0 ));

		zoomToWell( wellNameToInterval.keySet().iterator().next());

		plateViewerMainPanel.showUI( bdv.getBdvHandle().getViewerPanel() );

		installBdvBehaviours();
	}

	private void installBdvBehaviours()
	{
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdv.getBdvHandle().getTriggerbindings(), "plate viewer" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {

			final RealPoint globalLocation = new RealPoint( 3 );
			bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates( globalLocation );
			final String siteName = getSiteName( globalLocation );
			final PlateLocation plateLocation = new PlateLocation();
			plateLocation.plateName = plateName;
			plateLocation.siteName = siteName;
			globalLocation.localize( plateLocation.pixelLocation );

			final PopupMenu popupMenu = new PopupMenu();

			popupMenu.addPopupAction( "Raise GitHub Issue...", e ->
			{
				final IssueRaiser issueRaiser = new IssueRaiser();
				issueRaiser.showDialogAndCreateIssue( plateLocation );
			} );

			popupMenu.addPopupAction( "Measure pixel value", e -> {
				logPixelValues( plateLocation );
			} );

			popupMenu.addPopupAction( "Measure region statistics...", e -> {
				// TODO out everything below in own class (in bdv-utils repo) and improve UI
				final GenericDialog gd = new GenericDialog( "Radius" );
				gd.addNumericField( "Radius", 5.0, 1 );
				gd.showDialog();
				if ( gd.wasCanceled() ) return;
				final double radius = gd.getNextNumber();
				logRegionStatistics( plateLocation, radius );
			} );

			popupMenu.show( bdv.getBdvHandle().getViewerPanel().getDisplay(), x, y );

		}, "context menu", "button3" ) ;
	}

	private void logPixelValues( PlateLocation plateLocation )
	{
		Utils.log( "Pixel values at " + plateLocation );
		final RealPoint realPoint = new RealPoint( 3 );
		bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates( realPoint );

		final int currentTimepoint =
				bdv.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint();

		final Map< Integer, Double > pixelValuesOfActiveSources =
				BdvUtils.getPixelValuesOfActiveSources(
						bdv, realPoint, currentTimepoint );

		for ( Map.Entry< Integer, Double > entry : pixelValuesOfActiveSources.entrySet() )
		{
			final String name = BdvUtils.getSource( bdv, entry.getKey() ).getName();
			Utils.log( name + ": " + entry.getValue() );
		}
	}

	private void logRegionStatistics( PlateLocation plateLocation, double radius )
	{
		Utils.log( "Region (r=" + (int) radius + ") statistics at " + plateLocation );

		final RealPoint realPoint = new RealPoint( 3 );
	 	bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates( realPoint );
		final int currentTimepoint =
				bdv.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint();

		final HashMap< Integer, PixelValueStatistics > statistics =
				BdvUtils.getPixelValueStatisticsOfActiveSources( bdv, realPoint, radius, currentTimepoint );

		for ( Map.Entry< Integer, PixelValueStatistics > entry : statistics.entrySet() )
		{
			final String name = BdvUtils.getSource( bdv, entry.getKey() ).getName();
			Utils.log( name + ": " + entry.getValue() );
		}
	}

	public void addWellOutlinesImages()
	{
		final OutlinesImage outlinesImage = new OutlinesImage( this, 0.01 );
		addToBdvAndPanel( outlinesImage );
	}

	public String getFileNamingScheme()
	{
		return fileNamingScheme;
	}

	private void addSiteAndWellNamesOverlay( MultiWellImg multiWellImg )
	{
		final WellNamesOverlay wellNamesOverlay = new WellNamesOverlay( this );

		addToBdvAndPanel( new OverlayBdvViewable( wellNamesOverlay, "well names" ) );

		BdvOverlay bdvOverlay = new BdvSiteAndWellNamesOverlay(
				bdv,
				multiWellImg.getLoader() );

		BdvFunctions.showOverlay(
				bdvOverlay,
				"site and well names mouse hover",
				BdvOptions.options().addTo( bdv ) );
	}

	public static String getImageNamingScheme( List< File > fileList )
	{
		final String namingScheme = Utils.getNamingScheme( fileList.get( 0 ) );
		Utils.log( "Detected naming scheme: " + namingScheme );
		return namingScheme;
	}

	public static List< File > getFiles( String inputDirectory, String filePattern, boolean includeSubFolders )
	{
		Utils.log( "Plate directory: " + inputDirectory );
		Utils.log( "Fetching files..." );
		final List< File > fileList = FileUtils.getFileList( new File( inputDirectory ), filePattern, includeSubFolders );
		Utils.log( "Number of files: " + fileList.size() );

		if ( fileList.size() == 0 )
		{
			Logger.error( "There were no files found in " + inputDirectory +
					", which match the pattern: \"" + filePattern + "\"" );
			throw new UnsupportedOperationException( "No files found" );
		}
		return fileList;
	}

	public void addChannels(
			List< File > fileList,
			String namingScheme,
			List< String > channelPatterns )
	{
		MultiWellImg wellImg;

		Utils.log( "Adding channels..." );
		for ( String channelPattern : channelPatterns )
		{
			final String channelName = channelPattern;

			// if ( ! channelName.equals( "nuclei" ) ) continue;

			Utils.log( "Adding channel: " + channelName );
			List< File > channelFiles = getChannelFiles( fileList, namingScheme, channelName );

			if ( namingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
			{
				final MultiResolutionBatchLibHdf5ChannelSourceCreator sourceCreator =
						new MultiResolutionBatchLibHdf5ChannelSourceCreator(
							namingScheme,
							channelName,
							channelFiles );

				sourceCreator.create();

				wellImg = sourceCreator.getMultiWellHdf5CachedCellImage();

				wellImg.setSource( sourceCreator.getVolatileSource() );

				multiWellImgs.add( wellImg );

				addToBdvAndPanel( wellImg );
			}
			else
			{
				wellImg = new MultiWellImagePlusImg(
								channelFiles,
								channelName,
								namingScheme,
								numIoThreads,
								0 );

				multiWellImgs.add( wellImg );

				addToBdvAndPanel( wellImg );
			}

			if ( isFirstChannel )
			{
				setPlateInterval( wellImg );

				mapSiteNamesToIntervals( wellImg );

				mapWellNamesToIntervals( wellImg );

				setSiteDimensions( wellImg );

				// TODO: fix this, as this should be in units of wells per plate and sites per well
//				Utils.log( "Site dimensions [ 0 ] : " +  siteDimensions[ 0 ] );
//				Utils.log( "Site dimensions [ 1 ] : " +  siteDimensions[ 1 ] );
//				Utils.log( "Well dimensions [ 0 ] : " +  wellDimensions[ 0 ] );
//				Utils.log( "Well dimensions [ 1 ] : " +  wellDimensions[ 1 ] );

				isFirstChannel = false;
			}
		}

		if ( multiWellImgs.size() == 0 )
		{
			throw new UnsupportedOperationException( "No multi-well images sources have been added." );
		}
	}

	public void setSiteDimensions( MultiWellImg wellImg )
	{
		siteDimensions = new long[ 2 ];
		wellImg.getLoader().getSingleSiteChannelFiles().get( 0 ).getInterval().dimensions( siteDimensions );
	}

	public long[] getSiteDimensions()
	{
		return siteDimensions;
	}

	public void setPlateInterval( MultiWellImg wellImg )
	{
		plateInterval = wellImg.getRAI();
	}

	public void mapSiteNamesToIntervals( MultiWellImg multiWellImg )
	{
		siteNameToInterval = new HashMap<>();
		intervalToSiteName = new HashMap<>();

		final ArrayList< SingleSiteChannelFile > siteChannelFiles =
				multiWellImg.getLoader().getSingleSiteChannelFiles();

		for ( SingleSiteChannelFile channelFile : siteChannelFiles )
		{
			siteNameToInterval.put( channelFile.getSiteName(), channelFile.getInterval() );
			intervalToSiteName.put( channelFile.getInterval(), channelFile.getSiteName() );
		}

		final Interval siteInterval = siteNameToInterval.values().iterator().next();
		final int[] numSites = new int[ 2 ];
		for ( int d = 0; d < 2; d++ )
		{
			numSites[ d ] = (int) (( plateInterval.max( d ) - plateInterval.min( d ) ) / siteInterval.dimension( d )) + 1;
		}

		siteNameMatrix = new String[ numSites[ 0 ] ][ numSites[ 1 ] ];

		for ( SingleSiteChannelFile channelFile : siteChannelFiles )
		{
			final int rowIndex = (int) (channelFile.getInterval().min( 0 ) / siteInterval.dimension( 0 ));
			final int colIndex = (int) (channelFile.getInterval().min( 1 ) / siteInterval.dimension( 1 ));
			siteNameMatrix[ rowIndex ][ colIndex ] = channelFile.getSiteName();
		}
	}

	public void mapWellNamesToIntervals( MultiWellImg< R > multiWellImg )
	{
		wellNameToInterval = new HashMap<>();

		final ArrayList< SingleSiteChannelFile > singleSiteChannelFiles =
				multiWellImg.getLoader().getSingleSiteChannelFiles();

		final ArrayList< String > wellNames = multiWellImg.getWellNames();

		for ( String wellName : wellNames )
		{
			FinalInterval union = null;

			for ( SingleSiteChannelFile singleSiteChannelFile : singleSiteChannelFiles )
			{
				if ( singleSiteChannelFile.getWellName().equals( wellName ) )
				{
					if ( union == null )
					{
						union = new FinalInterval( singleSiteChannelFile.getInterval() );
					} else
					{
						union = Intervals.union( singleSiteChannelFile.getInterval(), union );
					}
				}
			}
			wellNameToInterval.put( wellName, union );
		}

		wellDimensions = Intervals.dimensionsAsLongArray( wellNameToInterval.values().iterator().next() );
	}


	public String[][] getSiteNameMatrix()
	{
		return siteNameMatrix;
	}

	public List< File > getChannelFiles ( List < File > fileList, String namingScheme, String channelPattern )
	{
		List< File > channelFiles;
		if ( namingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			// each file contains all channels => we need all
			channelFiles = fileList;
		} else
		{
			// one channel per file => we need to filter the relevant files
			channelFiles = FileUtils.filterFiles( fileList, channelPattern );
		}

		return channelFiles;
	}

	public Bdv getBdv ( )
	{
		return bdv;
	}

	public SharedQueue getLoadingQueue ( )
	{
		return loadingQueue;
	}

	public void zoomToInterval( Interval interval )
	{
		final AffineTransform3D affineTransform3D = getImageZoomTransform( interval );

		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( affineTransform3D );
	}

	public ArrayList< String > getSiteNames ( )
	{
		final ArrayList< SingleSiteChannelFile > singleSiteChannelFiles =
				this.multiWellImgs.get( 0 ).getLoader().getSingleSiteChannelFiles();

		final ArrayList< String > imageNames = new ArrayList<>();

		for ( SingleSiteChannelFile singleSiteChannelFile : singleSiteChannelFiles )
		{
			imageNames.add( singleSiteChannelFile.getSiteName() );
		}

		return imageNames;
	}

	public ArrayList< String > getWellNames ( )
	{
		return multiWellImgs.get( 0 ).getWellNames();
	}

	public void zoomToWell ( String wellName )
	{
		zoomToInterval( wellNameToInterval.get( wellName ) );
	}

	public HashMap< String, Interval > getWellNameToInterval()
	{
		return wellNameToInterval;
	}

	public void zoomToSite( String siteName )
	{
		zoomToInterval( siteNameToInterval.get( siteName ) );

		if ( selectionModel != null )
			notifyImageSelectionModel( siteName );
	}

	public String getSiteName( RealPoint point )
	{
		for ( Interval interval : intervalToSiteName.keySet() )
		{
			if ( Intervals.contains( interval, point ) ) return intervalToSiteName.get( interval );
		}

		return null;
	}

	public void notifyImageSelectionModel ( String selectedSiteName )
	{
		/**
		 * We need to find the selectable object that corresponds to the the siteName
		 * // TODO: This should be taken care of by an adaptor
		 *
		 */
		for ( T name : siteNames )
		{
			final String siteName = name.getSiteName();
			if ( siteName.equals( selectedSiteName ) )
				selectionModel.focus( name );
		}
	}

	public boolean isImageExisting ( final SingleCellArrayImg< R, ? > cell )
	{
		final SingleSiteChannelFile imageFile = multiWellImgs.get( 0 ).getLoader().getChannelSource( cell );

		if ( imageFile != null ) return true;
		else return false;
	}

	public ArrayList< MultiWellImg > getMultiWellImgs( )
	{
		return multiWellImgs;
	}

	public AffineTransform3D getImageZoomTransform ( Interval interval )
	{
		int[] bdvWindowSize = getBdvWindowSize();

		final AffineTransform3D affineTransform3D = new AffineTransform3D();

		double[] shift = new double[ 3 ];

		for ( int d = 0; d < 2; ++d )
		{
			shift[ d ] = -( interval.min( d ) + interval.dimension( d ) / 2.0 );
		}

		affineTransform3D.translate( shift );


		final long minSize = Math.min( interval.dimension( 0 ), interval.dimension( 1 ) );

		// make is slightly smaller not to start fetching other images
		affineTransform3D.scale( 1.1 * bdvWindowSize[ 0 ] / minSize );

		double[] shiftToBdvWindowCenter = new double[ 3 ];

		for ( int d = 0; d < 2; ++d )
		{
			shiftToBdvWindowCenter[ d ] += bdvWindowSize[ d ] / 2.0;
		}

		affineTransform3D.translate( shiftToBdvWindowCenter );

		return affineTransform3D;
	}

	private int[] getBdvWindowSize ( )
	{
		int[] bdvWindowDimensions = new int[ 2 ];
		bdvWindowDimensions[ 0 ] = BdvUtils.getBdvWindowWidth( bdv );
		bdvWindowDimensions[ 1 ] = BdvUtils.getBdvWindowHeight( bdv );
		return bdvWindowDimensions;
	}

	private void initBdvAndPlateViewerUI( BdvViewable bdvViewable )
	{
		final ArrayImg< BitType, LongArray > dummyImageForInitialisation
				= ArrayImgs.bits( new long[]{ 100, 100 } );

		BdvSource bdvTmpSource = BdvFunctions.show(
				dummyImageForInitialisation,
				"",
				Bdv.options()
						.is2D().frameTitle( plateName )
						.preferredSize( 600, 600 )
						.doubleBuffered( false )
						.transformEventHandlerFactory(
								new BehaviourTransformEventHandlerPlanar
										.BehaviourTransformEventHandlerPlanarFactory() ) );

		bdv = bdvTmpSource.getBdvHandle();

		plateViewerMainPanel = new PlateViewerMainPanel( this );

		// This may interfere with loading of the resolution layers => TODO right click!
		// new BdvGrayValuesOverlay( bdv, Utils.bdvTextOverlayFontSize );

		setBdvBehaviors();

		// move to a region outside the plate, such that adding channels is faster
		final AffineTransform3D transform3D = new AffineTransform3D();
		transform3D.translate( 10000, 10000, 0 );
		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( transform3D );

		addToBdvAndPanel( bdvViewable );

		bdvTmpSource.removeFromBdv();
	}

	public void addToBdvAndPanel( BdvViewable bdvViewable )
	{
		if ( bdv == null )
		{
			initBdvAndPlateViewerUI( bdvViewable );
			return;
		}

		BdvSource bdvSource = addToBdv( bdvViewable );

		bdvSource.setActive( bdvViewable.isInitiallyVisible() );

		bdvSource.setDisplayRange( bdvViewable.getContrastLimits()[ 0 ], bdvViewable.getContrastLimits()[ 1 ] );

		bdvSource.setColor( bdvViewable.getColor() );

		plateViewerMainPanel.getSourcesPanel().addToPanel( bdvViewable, bdvSource );
	}

	public BdvSource addToBdv( BdvViewable bdvViewable )
	{
		if ( bdvViewable.getOverlay() != null )
		{
			return BdvFunctions.showOverlay(
					bdvViewable.getOverlay(),
					bdvViewable.getName(),
					BdvOptions.options().addTo( bdv ) );
		}
		else if ( bdvViewable.getSource() != null )
		{
			Source< ? > source = bdvViewable.getSource();

			if ( bdvViewable.getType().equals( Metadata.Type.Segmentation ) )
			{
				source = new ARGBConvertedRealSource(
						source,
						new LazyLabelsARGBConverter() );
			}

			return BdvFunctions.show( source, BdvOptions.options().addTo( bdv ) );
		}
		else
		{
			RandomAccessibleInterval< ? > rai = bdvViewable.getRAI();

			try
			{
				rai = VolatileViews.wrapAsVolatile( rai, loadingQueue );
			}
			catch ( Exception e )
			{
				Logger.info( "Could not wrap as Volatile: " + bdvViewable.getName());
			}

			if ( bdvViewable.getType().equals( Metadata.Type.Segmentation ) )
			{
				rai = Converters.convert(
						(RandomAccessibleInterval) rai,
						new RandomARGBConverter(),
						new VolatileARGBType() );
			}

			return BdvFunctions.show(
					rai,
					bdvViewable.getName(),
					BdvOptions.options().addTo( bdv ) );
		}
	}

	private void setBdvBehaviors ( )
	{
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdv.getBdvHandle().getTriggerbindings(), "my-new-behaviours" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			showImageName();
		}, "log image info", "P" );

	}

	private void showImageName ( )
	{
		final long[] coordinates = getMouseCoordinates();

		final SingleSiteChannelFile singleSiteChannelFile = multiWellImgs.get( 0 ).getLoader().getChannelSource( coordinates );

		if ( singleSiteChannelFile != null )
		{
			Utils.log( singleSiteChannelFile.getFile().getName() );
		}
	}

	private long[] getMouseCoordinates ( )
	{
		final RealPoint position = new RealPoint( 3 );

		bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates( position );

		long[] cellPos = new long[ 2 ];

		for ( int d = 0; d < 2; ++d )
		{
			cellPos[ d ] = ( long ) ( position.getDoublePosition( d ) );
		}

		return cellPos;
	}

	public void installImageSelectionModel ( List < T > siteNames, SelectionModel < T > selectionModel )
	{
		this.siteNames = siteNames;
		this.selectionModel = selectionModel;
		registerAsImageSelectionListener( selectionModel );
	}

	public void registerAsColoringListener( SelectionColoringModel< T > selectionColoringModel )
	{
		selectionColoringModel.listeners().add( () -> BdvUtils.repaint( bdv ) );
	}

	private void registerAsImageSelectionListener ( SelectionModel < T > selectionModel )
	{
		selectionModel.listeners().add( new SelectionListener< T >()
		{
			@Override
			public void selectionChanged()
			{
				//
			}

			@Override
			public void focusEvent( T selection )
			{
				int sourceIndex = 0; // TODO: this is because the siteName is the same for all channelSources, so we just take the first one.
				final SingleSiteChannelFile singleSiteChannelFile = multiWellImgs.get( sourceIndex ).getLoader().getChannelSource( selection.getSiteName() );

				if ( singleSiteChannelFile == null )
				{
					throw new UnsupportedOperationException( "Could not find image sources for site " + selection.getSiteName() );
				}

				zoomToInterval( singleSiteChannelFile.getInterval() );
			}
		} );
	}

	public Interval getPlateInterval()
	{
		return plateInterval;
	}

	public long[] getWellDimensions()
	{
		return wellDimensions;
	}

	// TODO: what minimal type is needed here? DefaultSiteNameTableRow?
	public void registerTableView( TableRowsTableView< DefaultSiteNameTableRow > tableView )
	{
		this.tableView = tableView;
	}

	public TableRowsTableView< DefaultSiteNameTableRow > getTableView()
	{
		return tableView;
	}
}
