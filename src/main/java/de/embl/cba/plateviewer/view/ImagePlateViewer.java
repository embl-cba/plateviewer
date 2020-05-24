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
import de.embl.cba.plateviewer.github.SiteIssueRaiser;
import de.embl.cba.plateviewer.location.LocationInformation;
import de.embl.cba.plateviewer.image.channel.BdvViewable;
import de.embl.cba.plateviewer.image.channel.MultiWellImgCreator;
import de.embl.cba.plateviewer.image.plate.QCOverlay;
import de.embl.cba.plateviewer.image.plate.WellAndSiteOutlinesSource;
import de.embl.cba.plateviewer.image.plate.OverlayBdvViewable;
import de.embl.cba.plateviewer.image.plate.WellNamesOverlay;
import de.embl.cba.plateviewer.image.source.RandomAccessibleIntervalPlateViewerSource;
import de.embl.cba.plateviewer.io.FileUtils;
import de.embl.cba.plateviewer.screenshot.PlateChannelRawDataFetcher;
import de.embl.cba.plateviewer.screenshot.SimpleScreenShotMaker;
import de.embl.cba.plateviewer.table.BatchLibHdf5CellFeatureProvider;
import de.embl.cba.plateviewer.util.Utils;
import de.embl.cba.plateviewer.image.*;
import de.embl.cba.plateviewer.image.channel.MultiWellImg;
import de.embl.cba.plateviewer.table.AnnotatedInterval;
import de.embl.cba.plateviewer.view.panel.PlateViewerMainPanel;
import de.embl.cba.tables.Logger;
import de.embl.cba.tables.color.LazyLabelsARGBConverter;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.SelectionListener;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.tablerow.TableRow;
import ij.CompositeImage;
import ij.IJ;
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
	private Map< String, Interval > wellNameToInterval;
	private Map< String, Interval > siteNameToInterval;
	private Map< Interval, String > intervalToSiteName;
	private Map< Interval, String > intervalToWellName;

	private long[] siteDimensions;
	private long[] wellDimensions;
	private String plateName;
	private BdvSource dummySource;
	private List< File > siteFiles;
	private Map< String, MultiWellImg< ? > > channelToMultiWellImg;
	private MultiWellImg referenceWellImg;
	private Map< String, ChannelProperties > channelNamesToProperties;
	private Set< BdvOverlay > overlays;
	private Map< String, BdvViewable > nameToBdvViewable;
	private List< T > wells;
	private SelectionModel< T > wellSelectionModel;
	private BatchLibHdf5CellFeatureProvider cellFeatureProvider;
	private CellFeatureDialog cellFeatureDialog;


	public ImagePlateViewer( String inputDirectory, String filterPattern, int numIoThreads )
	{
		this( inputDirectory, filterPattern, numIoThreads, true );
	}

	public ImagePlateViewer( String inputDirectory, String filterPattern, int numIoThreads, boolean includeSubFolders )
	{
		plateName = new File( inputDirectory ).getName();
		channelToMultiWellImg = new HashMap<>();
		nameToBdvViewable = new HashMap<>();

		overlays = new HashSet<>(  );

		this.loadingQueue = new SharedQueue( numIoThreads );

		siteFiles = getFiles( inputDirectory, filterPattern, includeSubFolders );

		fileNamingScheme = getImageNamingScheme( siteFiles );

		channelNamesToProperties = Channels.getChannels( siteFiles, fileNamingScheme );

		logChannelNames();

		fetchReferenceWellImg( );

		initMainPanel();

		mainPanel.show( null );

		addToPanelAndBdv( referenceWellImg );

		configPlateWellAndSiteIntervals();

		addWellOutlinesImages();

		addWellNamesOverlay();

		addWellAndSiteInformationOverlay( referenceWellImg );

		zoomToInterval( wellNameToInterval.get( wellNameToInterval.keySet().iterator().next() ), 0 );

		installBdvBehaviours();
	}



	public List< File > getSiteFiles()
	{
		return siteFiles;
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
		}, "context menu", "button3", "P" ) ;
	}

	private void showPopupMenu( int x, int y )
	{
		// TODO: refactor into PlatePopupMenuCreator
		final RealPoint globalLocation = new RealPoint( 3 );
		bdvHandle.getViewerPanel().getGlobalMouseCoordinates( globalLocation );
		final String siteName = getIntervalName( globalLocation, intervalToSiteName );
		final String wellName = getIntervalName( globalLocation, intervalToWellName );

		if ( siteName == null ) return;

		final double[] location = new double[ 3 ];
		globalLocation.localize( location );

		final LocationInformation locationInformation = new LocationInformation( plateName, siteName, location );

		if ( sites != null )
		{
			final T site = getAnnotatedInterval( sites, siteName );
			if ( site instanceof TableRow )
			{
				final TableRow tableRow = ( TableRow ) site;
				if ( tableRow.getColumnNames().contains( "version" ) )
				{
					locationInformation.setAnalysisVersion( tableRow.getCell( "version" ) );
				}
			}
		}

		final PopupMenu popupMenu = new PopupMenu();

		popupMenu.addPopupAction( "Report issue...", e ->
		{
			new Thread( () -> {
				final ImagePlus screenShot = SimpleScreenShotMaker.getSimpleScreenShot(
						bdvHandle.getViewerPanel(),
						getOverlays() );
				screenShot.setTitle( plateName + "-"  + siteName  );
				final SiteIssueRaiser siteIssueRaiser = new SiteIssueRaiser();
				siteIssueRaiser.showPlateIssueDialogAndCreateIssue( locationInformation, screenShot );
			}).start();
		} );

		popupMenu.addPopupAction( "Focus well", e -> {
			focusWell( wellName );
		} );

		popupMenu.addPopupAction( "Focus site", e -> {
			focusSite( siteName );
		} );

		popupMenu.addPopupAction( "Inspect cell features...", e -> {
			final String sourceName = "cell_segmentation";
			if ( ! nameToBdvViewable.containsKey( sourceName ) )
			{
				IJ.showMessage( "Please add the cell-segmentation channel first.");
				return;
			}
			final BdvViewable bdvViewable = nameToBdvViewable.get( sourceName );
			final HashMap< String, BdvViewable > cellSegmentation = new HashMap<>();
			cellSegmentation.put( sourceName, bdvViewable );
			final PlateChannelRawDataFetcher rawDataFetcher = new PlateChannelRawDataFetcher( cellSegmentation );
			final Map< String, Double > pixelValues = rawDataFetcher.fetchPixelValues( globalLocation, bdvHandle.getViewerPanel().getState().getCurrentTimepoint() );
			final int cellId = pixelValues.get( sourceName ).intValue();

			if ( ! cellFeatureDialog.showDialog( siteName, cellId ) ) return;

			Utils.log( "\nCell " + cellId +
					"; Table " + cellFeatureDialog.getTableGroupChoice() +
					"; Feature " + cellFeatureDialog.getFeatureChoice() +
					"; Value " + cellFeatureDialog.getFeatureValue() );

		} );

		popupMenu.addPopupAction( "Measure pixel values statistics...", e -> {
			// TODO out everything below in own class (in bdv-utils repo) and improve UI
			final GenericDialog gd = new GenericDialog( "Radius" );
			gd.addNumericField( "Radius [pixels]", 5.0, 1 );
			gd.showDialog();
			if ( gd.wasCanceled() ) return;
			final double radius = gd.getNextNumber();
			new Thread( () -> logPixelValueStatistics( locationInformation, radius ) ).start();
		} );

		popupMenu.addPopupAction( "View raw data", e -> {
			new Thread( () -> {
				Logger.info( "Fetching raw data, please wait..." );
				final CompositeImage compositeImage = new PlateChannelRawDataFetcher( nameToBdvViewable ).captureCurrentView( 0 );
				if ( compositeImage != null )
					compositeImage.show();
			}).start();
		} );

		if ( sites != null )
		{
			final T site = getAnnotatedInterval( sites, siteName );

			popupMenu.addPopupAction( "Modify site annotations...", e -> {
				showIntervalAnnotationDialog( site );
			} );
		}

		if ( wells != null )
		{
			final T well = getAnnotatedInterval( wells, wellName );

			popupMenu.addPopupAction( "Modify well annotations...", e -> {
				showIntervalAnnotationDialog( well );
			} );
		}

		popupMenu.show( bdvHandle.getViewerPanel().getDisplay(), x, y );
	}

	private void showIntervalAnnotationDialog( T interval )
	{
		final GenericDialog gd = new GenericDialog( "Annotations" );
		gd.addCheckbox( "Is outlier", interval.isOutlier() );
		gd.showDialog();
		if ( gd.wasCanceled() ) return;
		interval.setOutlier( gd.getNextBoolean() );
	}

	private void logPixelValues( LocationInformation locationInformation )
	{
		Utils.log( "Pixel values at " + locationInformation );
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

	private void logPixelValueStatistics( LocationInformation locationInformation, double radius )
	{
		Utils.log( "" );
		Utils.log( "Region (r=" + (int) radius + ") statistics at " + locationInformation );

		final RealPoint globalLocation = new RealPoint( 3 );
	 	bdvHandle.getViewerPanel().getGlobalMouseCoordinates( globalLocation );

		final PlateChannelRawDataFetcher rawDataFetcher = new PlateChannelRawDataFetcher( nameToBdvViewable );
		final Map< String, PixelValueStatistics > statistics =
				rawDataFetcher.computePixelValueStatistics( globalLocation, radius, bdvHandle.getViewerPanel().getState().getCurrentTimepoint() );

		for ( Map.Entry< String, PixelValueStatistics > entry : statistics.entrySet() )
		{
			Utils.log( entry.getKey() + ": " + entry.getValue() );
		}
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
				referenceWellImg = MultiWellImgCreator.create( siteFiles, fileNamingScheme, properties.regExp );

				referenceWellImg.setInitiallyVisible( true );

				channelToMultiWellImg.put( properties.name, referenceWellImg );

				return;
			}
		}
	}

	public void configPlateWellAndSiteIntervals()
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

	// TODO: base this on the list of sites rather than the multiWellImg?
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
	}

	public void mapWellNamesToIntervals( MultiWellImg< R > multiWellImg )
	{
		wellNameToInterval = new HashMap<>();
		intervalToWellName = new HashMap<>();

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
			intervalToWellName.put( union, wellName );
		}

		wellDimensions = Intervals.dimensionsAsLongArray( wellNameToInterval.values().iterator().next() );
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
		zoomToInterval( interval, 2000 );
	}

	public void zoomToInterval( Interval interval, int duration )
	{
		final AffineTransform3D affineTransform3D = getImageZoomTransform( interval );

		if ( duration == 0 )
		{
			bdvHandle.getViewerPanel().setCurrentViewerTransform( affineTransform3D );
		}
		else
		{
			BdvUtils.changeBdvViewerTransform( bdvHandle, affineTransform3D, duration );
		}
	}

	public ArrayList< String > getSiteNames( )
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

	public void focusWell( String wellName )
	{
		focusInterval( wellName, wellNameToInterval, wells, wellSelectionModel );
	}

	public Map< String, Interval > getWellNameToInterval()
	{
		return wellNameToInterval;
	}

	public Map< String, Interval > getSiteNameToInterval()
	{
		return siteNameToInterval;
	}

	public void focusSite( String siteName )
	{
		focusInterval( siteName, siteNameToInterval, sites, siteSelectionModel );
	}

	public void focusInterval( String intervalName, Map< String, Interval > nameToInterval, List< T > intervals, SelectionModel< T > selectionModel )
	{
		zoomToInterval( nameToInterval.get( intervalName ) );
		notifySelectionModel( intervalName, intervals, selectionModel );
	}

	private void notifySelectionModel( String siteName, List< T > interval, SelectionModel< T > siteSelectionModel )
	{
		if ( interval != null )
		{
			T selected = getAnnotatedInterval( this.sites, siteName );
			if ( selected != null && siteSelectionModel != null )
				this.siteSelectionModel.focus( selected );
		}
	}

	public String getIntervalName( RealPoint point, Map< Interval, String > intervalToName )
	{
		for ( Interval interval : intervalToName.keySet() )
		{
			if ( Intervals.contains( interval, point ) ) return intervalToName.get( interval );
		}

		return null;
	}

	public void notifySiteSelectionModel( String siteName )
	{
		T selectedSite = getAnnotatedInterval( sites, siteName );
		if ( selectedSite != null && siteSelectionModel != null )
			siteSelectionModel.focus( selectedSite );
	}

	private T getAnnotatedInterval( List< T > annotatedIntervals, String name )
	{
		for ( T interval : annotatedIntervals )
		{
			if ( interval.getName().equals( name ) )
				return interval;
		}
		return null;
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

		// move to a region outside the plate, such that adding channels is faster
//		final AffineTransform3D transform3D = new AffineTransform3D();
//		transform3D.translate( 10000, 10000, 0 );
//		bdvHandle.getViewerPanel().setCurrentViewerTransform( transform3D );

		BdvUtils.getViewerFrame( bdvHandle ).setLocation(
				mainPanel.getLocation().x + mainPanel.getWidth() + 10,
				mainPanel.getLocation().y );

		return bdvTmpSource;
	}

	public void addToPanelAndBdv( BdvViewable bdvViewable )
	{
		if ( nameToBdvViewable.containsKey( bdvViewable.getName() ) ) return;

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

	public void addAnnotatedSiteIntervals(
			List< T > annotatedIntervals,
			SelectionModel< T > selectionModel,
			SelectionColoringModel< T > selectionColoringModel )
	{
		this.sites = annotatedIntervals;
		this.siteSelectionModel = selectionModel;
		registerAsIntervalSelectionListener( selectionModel );
		selectionColoringModel.listeners().add( () -> BdvUtils.repaint( bdvHandle ) );
		addAnnotatedIntervalQCOverlay( sites, "site QC" );
	}

	// TODO: Do we really need both site and well or can we unify in a list
	public void addAnnotatedWellIntervals(
			List< T > annotatedIntervals,
			SelectionModel< T > selectionModel,
			SelectionColoringModel< T > selectionColoringModel )
	{
		this.wells = annotatedIntervals;
		this.wellSelectionModel = selectionModel;
		registerAsIntervalSelectionListener( selectionModel );
		selectionColoringModel.listeners().add( () -> BdvUtils.repaint( bdvHandle ) );
		addAnnotatedIntervalQCOverlay( wells, "well QC" );
	}

	private void registerAsIntervalSelectionListener( SelectionModel< T > selectionModel )
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
				zoomToInterval( selection.getInterval() );
			}
		} );
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

	private void addAnnotatedIntervalQCOverlay( List< T > intervals, String name )
	{
		final QCOverlay overlay = new QCOverlay( intervals );
		overlays.add( overlay );
		addToPanelAndBdv( new OverlayBdvViewable( overlay, name ) );
	}

	public void addToPanelAndBdv( String channel )
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
			multiWellImg = MultiWellImgCreator.create( siteFiles, fileNamingScheme, channel );
		}
		return multiWellImg;
	}

	public Map< String, MultiWellImg< ? > > getChannelToMultiWellImg()
	{
		return channelToMultiWellImg;
	}

	public void setCellFeatureProvider( BatchLibHdf5CellFeatureProvider cellFeatureProvider )
	{
		this.cellFeatureProvider = cellFeatureProvider;
		cellFeatureDialog = new CellFeatureDialog( cellFeatureProvider );
	}
}
