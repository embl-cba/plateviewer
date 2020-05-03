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
import de.embl.cba.plateviewer.bdv.*;
import de.embl.cba.plateviewer.bdv.BehaviourTransformEventHandlerPlanar;
import de.embl.cba.plateviewer.channel.ChannelProperties;
import de.embl.cba.plateviewer.channel.Channels;
import de.embl.cba.plateviewer.github.IssueRaiser;
import de.embl.cba.plateviewer.github.PlateLocation;
import de.embl.cba.plateviewer.image.channel.BdvViewable;
import de.embl.cba.plateviewer.image.channel.MultiWellImgCreator;
import de.embl.cba.plateviewer.image.plate.QCOverlay;
import de.embl.cba.plateviewer.image.plate.WellAndSiteOutlinesSource;
import de.embl.cba.plateviewer.image.plate.OverlayBdvViewable;
import de.embl.cba.plateviewer.image.plate.WellNamesOverlay;
import de.embl.cba.plateviewer.image.source.RandomAccessibleIntervalPlateViewerSource;
import de.embl.cba.plateviewer.io.FileUtils;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.image.*;
import de.embl.cba.plateviewer.image.channel.MultiWellImg;
import de.embl.cba.plateviewer.table.DefaultAnnotatedIntervalTableRow;
import de.embl.cba.plateviewer.table.AnnotatedInterval;
import de.embl.cba.plateviewer.view.panel.PlateViewerMainPanel;
import de.embl.cba.tables.Logger;
import de.embl.cba.tables.color.LazyLabelsARGBConverter;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.SelectionListener;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.view.TableRowsTableView;
import ij.CompositeImage;
import ij.ImagePlus;
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
import java.util.*;
import java.util.List;

public class ImagePlateViewer< R extends NativeType< R > & RealType< R >, T extends AnnotatedInterval >
{
	private final SharedQueue loadingQueue;

	private BdvHandle bdvHandle;
	private PlateViewerMainPanel mainPanel;
	private List< T > sites;
	private SelectionModel< T > siteSelectionModel;
	private final String fileNamingScheme;
	private Interval plateInterval;
	private HashMap< String, Interval > wellNameToInterval;
	private HashMap< String, Interval > siteNameToInterval;
	private HashMap< Interval, String > intervalToSiteName;
	private String[][] siteNameMatrix;

	private long[] siteDimensions;
	private long[] wellDimensions;
	private String plateName;
	private TableRowsTableView< DefaultAnnotatedIntervalTableRow > tableView;
	private BdvSource dummySource;
	private List< File > fileList;
	private Map< String, MultiWellImg< ? > > channelToMultiWellImg;
	private MultiWellImg referenceWellImg;
	private Map< String, ChannelProperties > channelNamesToProperties;
	private Set< BdvOverlay > overlays;
	private Map< String, BdvViewable > nameToBdvViewable;

	public ImagePlateViewer( String inputDirectory, String filterPattern, int numIoThreads )
	{
		this( inputDirectory, filterPattern, numIoThreads, true );
	}

	public ImagePlateViewer( String inputDirectory, String filterPattern, int numIoThreads, boolean includeSubFolders )
	{
		this.plateName = new File( inputDirectory ).getName();
		//this.multiWellImgs = new ArrayList<>();
		channelToMultiWellImg = new HashMap<>();
		nameToBdvViewable = new HashMap<>();

		overlays = new HashSet<>(  );

		this.loadingQueue = new SharedQueue( numIoThreads );

		fileList = getFiles( inputDirectory, filterPattern, includeSubFolders );

		fileNamingScheme = getImageNamingScheme( fileList );

		channelNamesToProperties = Channels.getChannels( fileList, fileNamingScheme );

		logChannelNames();

		fetchReferenceWellImg( );

		initMainPanel();

		mainPanel.show( null );

		addToPanelAndBdv( referenceWellImg );

		configPlateDimensions();

		addWellOutlinesImages();

		addWellNamesOverlay();

		addWellAndSiteInformationOverlay( referenceWellImg );

		zoomToWell( wellNameToInterval.keySet().iterator().next());

		installBdvBehaviours();
	}

	public Set< BdvOverlay > getOverlays()
	{
		return overlays;
	}

	public PlateViewerMainPanel getMainPanel()
	{
		return mainPanel;
	}

	public String getPlateName()
	{
		return plateName;
	}

	public void initMainPanel()
	{
		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
		{
			mainPanel = new PlateViewerMainPanel( this, false );
		}
		else
		{
			mainPanel = new PlateViewerMainPanel( this, true );
		}
	}


	public void logChannelNames()
	{
		Logger.info( "Detected channels: " );
		for ( String channelName : channelNamesToProperties.keySet() )
		{
			Logger.info( "- " + channelName );
		}
	}

	public Set< String > getChannelNames()
	{
		return channelNamesToProperties.keySet();
	}

	private void installBdvBehaviours()
	{
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdvHandle.getTriggerbindings(), "plate viewer" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			showPopupMenu( x, y );
		}, "context menu", "button3" ) ;
	}

	private void showPopupMenu( int x, int y )
	{
		final RealPoint globalLocation = new RealPoint( 3 );
		bdvHandle.getViewerPanel().getGlobalMouseCoordinates( globalLocation );
		final String siteName = getSiteName( globalLocation );
		if ( siteName == null ) return;

		final double[] location = new double[ 3 ];
		globalLocation.localize( location );

		final PlateLocation plateLocation = new PlateLocation( plateName, siteName, location );

		final PopupMenu popupMenu = new PopupMenu();

		popupMenu.addPopupAction( "Report issue...", e ->
		{
			new Thread( () -> {
				final ImagePlus screenShot = SimpleScreenShotMaker.getSimpleScreenShot(
						bdvHandle.getViewerPanel(),
						getOverlays() );
				screenShot.setTitle( plateName + "-"  + siteName  );
				final IssueRaiser issueRaiser = new IssueRaiser();
				issueRaiser.showPlateIssueDialogAndCreateIssue( plateLocation, screenShot );
			}).start();
		} );

		popupMenu.addPopupAction( "Focus site", e -> {
			zoomToSite( siteName );
		} );

		popupMenu.addPopupAction( "Mark site as outlier", e -> {
			final T site = getSite( siteName );
			site.setOutlier( true );
		} );

		popupMenu.addPopupAction( "Mark site as no outlier", e -> {
			final T site = getSite( siteName );
			site.setOutlier( false );
		} );

		popupMenu.addPopupAction( "Measure pixel values", e -> {
			logPixelValues( plateLocation );
		} );

//		popupMenu.addPopupAction( "Measure pixel values statistics...", e -> {
//			// TODO out everything below in own class (in bdv-utils repo) and improve UI
//			final GenericDialog gd = new GenericDialog( "Radius" );
//			gd.addNumericField( "Radius", 5.0, 1 );
//			gd.showDialog();
//			if ( gd.wasCanceled() ) return;
//			final double radius = gd.getNextNumber();
//			logRegionStatistics( plateLocation, radius );
//		} );

		popupMenu.addPopupAction( "View raw data", e -> {
			new Thread( () -> {
				Logger.info( "Fetching raw data, please wait..." );
				final CompositeImage compositeImage = new PlateChannelRawDataFetcher( nameToBdvViewable ).captureCurrentView( 0 );
				if ( compositeImage != null )
					compositeImage.show();
			}).start();
		} );

		popupMenu.show( bdvHandle.getViewerPanel().getDisplay(), x, y );
	}

	private void logPixelValues( PlateLocation plateLocation )
	{
		Utils.log( "Pixel values at " + plateLocation );
		final RealPoint globalLocation = new RealPoint( 3 );
		bdvHandle.getViewerPanel().getGlobalMouseCoordinates( globalLocation );
		final int t = bdvHandle.getViewerPanel().getState().getCurrentTimepoint();

		final PlateChannelRawDataFetcher rawDataFetcher = new PlateChannelRawDataFetcher( nameToBdvViewable );
		final Map< String, Double > pixelValuesOfActiveSources = rawDataFetcher.fetchPixelValues( globalLocation, t );

		for ( Map.Entry< String, Double > entry : pixelValuesOfActiveSources.entrySet() )
		{
			Utils.log( entry.getKey() + ": " + entry.getValue() );
		}
	}

	private void logRegionStatistics( PlateLocation plateLocation, double radius )
	{
//		Utils.log( "Region (r=" + (int) radius + ") statistics at " + plateLocation );
//
//		final RealPoint globalLocation = new RealPoint( 3 );
//	 	bdvHandle.getViewerPanel().getGlobalMouseCoordinates( globalLocation );
//
//		final RawDataFetcher rawDataFetcher = new RawDataFetcher( bdvHandle );
//		final HashMap< Integer, PixelValueStatistics > statistics =
//				rawDataFetcher.computePixelValueStatistics( globalLocation, radius, 0 );
//
//
//		//		BdvUtils.getPixelValueStatisticsOfActiveSources( bdvHandle, globalLocation, radius, currentTimepoint );
//
//		for ( Map.Entry< Integer, PixelValueStatistics > entry : statistics.entrySet() )
//		{
//			final String name = BdvUtils.getSource( bdvHandle, entry.getKey() ).getName();
//			Utils.log( name + ": " + entry.getValue() );
//		}
	}

	public void addWellOutlinesImages()
	{
		final WellAndSiteOutlinesSource wellAndSiteOutlinesSource =
				new WellAndSiteOutlinesSource( this, 0.01, 0.005 );

		addToPanelAndBdv( wellAndSiteOutlinesSource );
	}

	public String getFileNamingScheme()
	{
		return fileNamingScheme;
	}

	private void addWellAndSiteInformationOverlay( MultiWellImg multiWellImg )
	{
		// Add overlay showing the site and well information in the bottom
		//
		BdvOverlay bdvOverlay = new BdvSiteAndWellInformationOverlay(
				bdvHandle,
				multiWellImg.getLoader() );

		overlays.add( bdvOverlay );

		BdvFunctions.showOverlay(
				bdvOverlay,
				"site and plate information",
				BdvOptions.options().addTo( bdvHandle ) );
	}

	private void addWellNamesOverlay()
	{
		final WellNamesOverlay wellNamesOverlay = new WellNamesOverlay( this );
		this.overlays.add( wellNamesOverlay  );
		addToPanelAndBdv( new OverlayBdvViewable( wellNamesOverlay, "well names" ) );
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

	public void fetchReferenceWellImg( )
	{
		for ( ChannelProperties properties : channelNamesToProperties.values() )
		{
			if ( properties.isInitiallyVisible || properties.name.equals( "nuclei" ) )
			{
				referenceWellImg = MultiWellImgCreator.create( fileList, fileNamingScheme, properties.regExp );

				referenceWellImg.setInitiallyVisible( true );

				channelToMultiWellImg.put( properties.name, referenceWellImg );

				return;
			}
		}
	}

	public void configPlateDimensions()
	{
		setPlateInterval( referenceWellImg );

		mapSiteNamesToIntervals( referenceWellImg );

		mapWellNamesToIntervals( referenceWellImg );

		setSiteDimensions( referenceWellImg );
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

	public BdvHandle getBdvHandle( )
	{
		return bdvHandle;
	}

	public SharedQueue getLoadingQueue ( )
	{
		return loadingQueue;
	}

	public void zoomToInterval( Interval interval )
	{
		final AffineTransform3D affineTransform3D = getImageZoomTransform( interval );

		BdvUtils.changeBdvViewerTransform( bdvHandle, affineTransform3D, 2000 );
	}

	public ArrayList< String > getSites( )
	{
		final ArrayList< SingleSiteChannelFile > singleSiteChannelFiles =
				referenceWellImg.getLoader().getSingleSiteChannelFiles();

		final ArrayList< String > siteNames = new ArrayList<>();

		for ( SingleSiteChannelFile singleSiteChannelFile : singleSiteChannelFiles )
		{
			siteNames.add( singleSiteChannelFile.getSiteName() );
		}

		return siteNames;
	}

	public ArrayList< String > getWellNames ( )
	{
		return referenceWellImg.getWellNames();
	}

	public void zoomToWell ( String wellName )
	{
		zoomToInterval( wellNameToInterval.get( wellName ) );
	}

	public Map< String, Interval > getWellNameToInterval()
	{
		return wellNameToInterval;
	}

	public Map< String, Interval > getSiteNameToInterval()
	{
		return siteNameToInterval;
	}

	public void zoomToSite( String siteName )
	{
		zoomToInterval( siteNameToInterval.get( siteName ) );

		if ( siteSelectionModel != null )
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

	public void notifyImageSelectionModel ( String siteName )
	{
		T selectedSite = getSite( siteName );
		if ( selectedSite != null )
			siteSelectionModel.focus( selectedSite );
	}

	private T getSite( String siteName )
	{
		T selectedSite = null;
		for ( T site : sites )
		{
			if ( site.getName().equals( siteName ) )
				selectedSite = site;
		}
		return selectedSite;
	}

	public boolean isImageExisting ( final SingleCellArrayImg< R, ? > cell )
	{
		final SingleSiteChannelFile imageFile = referenceWellImg.getLoader().getChannelSource( cell );

		if ( imageFile != null ) return true;
		else return false;
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
		bdvWindowDimensions[ 0 ] = BdvUtils.getBdvWindowWidth( bdvHandle );
		bdvWindowDimensions[ 1 ] = BdvUtils.getBdvWindowHeight( bdvHandle );
		return bdvWindowDimensions;
	}

	private BdvSource initBdvAndBehaviours()
	{
		final ArrayImg< BitType, LongArray > dummyImageForInitialisation
				= ArrayImgs.bits( new long[]{ 100, 100 } );

		BdvSource bdvTmpSource = BdvFunctions.show(
				dummyImageForInitialisation,
				"",
				Bdv.options()
						.is2D().frameTitle( plateName )
						.preferredSize( Utils.getBdvWindowSize(),  Utils.getBdvWindowSize() )
						.doubleBuffered( false )
						.transformEventHandlerFactory(
								new BehaviourTransformEventHandlerPlanar
										.BehaviourTransformEventHandlerPlanarFactory() ) );

		bdvHandle = bdvTmpSource.getBdvHandle();

		// This may interfere with loading of the resolution layers => TODO right click!
		// new BdvGrayValuesOverlay( bdv, Utils.bdvTextOverlayFontSize );

		setBdvBehaviors();

		// move to a region outside the plate, such that adding channels is faster
		final AffineTransform3D transform3D = new AffineTransform3D();
		transform3D.translate( 10000, 10000, 0 );
		bdvHandle.getViewerPanel().setCurrentViewerTransform( transform3D );

		BdvUtils.getViewerFrame( bdvHandle ).setLocation(
				mainPanel.getLocation().x + mainPanel.getWidth() + 10,
				mainPanel.getLocation().y );

		return bdvTmpSource;
	}

	public void addToPanelAndBdv( BdvViewable bdvViewable )
	{
		if ( bdvHandle == null )
		{
			dummySource = initBdvAndBehaviours();
		}

		BdvSource bdvSource = addToBdv( bdvViewable );

		bdvViewable.setBdvSource( bdvSource );

		bdvSource.setActive( bdvViewable.isInitiallyVisible() );

		bdvSource.setDisplayRange( bdvViewable.getContrastLimits()[ 0 ], bdvViewable.getContrastLimits()[ 1 ] );

		bdvSource.setColor( bdvViewable.getColor() );

		mainPanel.getSourcesPanel().addToPanel( bdvViewable, bdvSource );

		nameToBdvViewable.put( bdvViewable.getName(), bdvViewable );

		removeDummySource();
	}

	public Map< String, BdvViewable > getNameToBdvViewable()
	{
		return nameToBdvViewable;
	}

	public void removeDummySource()
	{
		try{
			dummySource.removeFromBdv();
		} catch ( Exception e )
		{
			//
		}
	}

	public BdvSource addToBdv( BdvViewable bdvViewable )
	{
		if ( bdvViewable.getOverlay() != null )
		{
			return BdvFunctions.showOverlay(
					bdvViewable.getOverlay(),
					bdvViewable.getName(),
					BdvOptions.options().addTo( bdvHandle ) );
		}
		else if ( bdvViewable.getSource() != null )
		{
			Source< ? > source = bdvViewable.getSource();

			if ( source instanceof RandomAccessibleIntervalPlateViewerSource )
			{
				source = (( RandomAccessibleIntervalPlateViewerSource ) source ).asVolatile( new SharedQueue( 1 ) );
			}

			if ( bdvViewable.getType().equals( Metadata.Type.Segmentation ) )
			{
				source = new ARGBConvertedRealSource(
						source,
						new LazyLabelsARGBConverter() );
			}

			return BdvFunctions.show( source, BdvOptions.options().addTo( bdvHandle ) );
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
					BdvOptions.options().addTo( bdvHandle ) );
		}
	}

	private void setBdvBehaviors ( )
	{
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdvHandle.getTriggerbindings(), "my-new-behaviours" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			showImageName();
		}, "log image info", "P" );

	}

	private void showImageName ( )
	{
		final long[] coordinates = getMouseCoordinates();

		final SingleSiteChannelFile singleSiteChannelFile = referenceWellImg.getLoader().getChannelSource( coordinates );

		if ( singleSiteChannelFile != null )
		{
			Utils.log( singleSiteChannelFile.getFile().getName() );
		}
	}

	private long[] getMouseCoordinates ( )
	{
		final RealPoint position = new RealPoint( 3 );

		bdvHandle.getViewerPanel().getGlobalMouseCoordinates( position );

		long[] cellPos = new long[ 2 ];

		for ( int d = 0; d < 2; ++d )
		{
			cellPos[ d ] = ( long ) ( position.getDoublePosition( d ) );
		}

		return cellPos;
	}

	public void addSiteImageIntervals(
			List < T > annotatedSiteIntervals,
			SelectionModel < T > selectionModel )
	{
		this.sites = annotatedSiteIntervals;
		this.siteSelectionModel = selectionModel;
		registerAsSiteSelectionListener( selectionModel );
		addSiteQCOverlay( sites );
	}

	public void registerAsColoringListener( SelectionColoringModel< T > selectionColoringModel )
	{
		selectionColoringModel.listeners().add( () -> BdvUtils.repaint( bdvHandle ) );
	}

	private void registerAsSiteSelectionListener( SelectionModel < T > siteSelectionModel )
	{
		siteSelectionModel.listeners().add( new SelectionListener< T >()
		{
			@Override
			public void selectionChanged()
			{
				//
			}

			@Override
			public void focusEvent( T selection )
			{
				final SingleSiteChannelFile singleSiteChannelFile = referenceWellImg.getLoader().getChannelSource( selection.getName() );

				if ( singleSiteChannelFile == null )
				{
					throw new UnsupportedOperationException( "Could not find image sources for site " + selection.getName() );
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

	// TODO: maybe this is not needed?
	public void registerTableView( TableRowsTableView< DefaultAnnotatedIntervalTableRow > tableView )
	{
		this.tableView = tableView;
	}

	private void addSiteQCOverlay( List< T > sites )
	{
		final QCOverlay siteQCOverlay = new QCOverlay( sites );
		overlays.add( siteQCOverlay );
		addToPanelAndBdv( new OverlayBdvViewable( siteQCOverlay, "image QC" ) );
	}

	// TODO: this should not be here!
	public TableRowsTableView< DefaultAnnotatedIntervalTableRow > getTableView()
	{
		return tableView;
	}

	public void addToPanelAndBdv( String channel )
	{
		MultiWellImg multiWellImg = getMultiWellImg( channel );

		addToPanelAndBdv( multiWellImg );
	}

	public void addToPanelAndBdvAndSetVisible( String channel )
	{
		MultiWellImg multiWellImg = getMultiWellImg( channel );

		multiWellImg.setInitiallyVisible( true );

		addToPanelAndBdv( multiWellImg );
	}

	public MultiWellImg getMultiWellImg( String channel )
	{
		if ( channelToMultiWellImg.containsKey( channel ) )
		{
			return channelToMultiWellImg.get( channel );
		}
		else
		{
			MultiWellImg multiWellImg = createMultiWellImg( channel );
			channelToMultiWellImg.put( channel, multiWellImg );
			return  multiWellImg;
		}
	}

	public MultiWellImg createMultiWellImg( String channel )
	{
		MultiWellImg multiWellImg;
		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5  ) )
		{
			// All channels are in the same files, thus we do not have to fetch them again.
			multiWellImg = MultiWellImgCreator.createFromChannelFiles( referenceWellImg.getChannelFiles(), fileNamingScheme, channel );
		}
		else
		{
			multiWellImg = MultiWellImgCreator.create( fileList, fileNamingScheme, channel );
		}
		return multiWellImg;
	}

	public Map< String, MultiWellImg< ? > > getChannelToMultiWellImg()
	{
		return channelToMultiWellImg;
	}
}
