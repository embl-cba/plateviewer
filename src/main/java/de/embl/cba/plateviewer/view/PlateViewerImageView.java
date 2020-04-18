package de.embl.cba.plateviewer.view;

import bdv.util.*;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.converters.RandomARGBConverter;
import de.embl.cba.bdv.utils.overlays.BdvGrayValuesOverlay;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.image.channel.BDViewable;
import de.embl.cba.plateviewer.image.source.MultiResolutionBatchLibHdf5ChannelSourceCreator;
import de.embl.cba.plateviewer.image.well.OutlinesImage;
import de.embl.cba.plateviewer.image.well.WellNamesOverlay;
import de.embl.cba.plateviewer.io.FileUtils;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.bdv.BdvSiteAndWellNamesOverlay;
import de.embl.cba.plateviewer.bdv.BehaviourTransformEventHandlerPlanar;
import de.embl.cba.plateviewer.image.*;
import de.embl.cba.plateviewer.image.channel.MultiWellImg;
import de.embl.cba.plateviewer.image.channel.MultiWellImagePlusImg;
import de.embl.cba.plateviewer.table.SiteName;
import de.embl.cba.plateviewer.view.panel.PlateViewerMainPanel;
import de.embl.cba.tables.Logger;
import de.embl.cba.tables.color.LazyLabelsARGBConverter;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.SelectionListener;
import de.embl.cba.tables.select.SelectionModel;
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
	private HashMap< Interval, String > intervalToSiteName;
	private String[][] siteNameMatrix;

	private boolean isFirstChannel = true;
	private long[] siteDimensions;
	private long[] wellDimensions;

	public PlateViewerImageView( String inputDirectory, String filterPattern, int numIoThreads )
	{
		this.multiWellImgs = new ArrayList<>();
		this.numIoThreads = numIoThreads;
		this.loadingQueue = new SharedQueue( numIoThreads );

		final List< File > fileList = getFiles( inputDirectory, filterPattern );

		fileNamingScheme = getImageNamingScheme( fileList );

		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
			this.numIoThreads = 1; // Hdf5 does not support multi-threading

		final List< String > channelPatterns =
				Utils.getChannelPatterns( fileList, fileNamingScheme );

		addChannels( fileList, fileNamingScheme, channelPatterns );

		if ( multiWellImgs.size() == 0 )
		{
			throw new UnsupportedOperationException( "No multi-well images sources have been added." );
		}

		final OutlinesImage outlinesImage = new OutlinesImage( this, 0.01 );
		addToBdvAndPanel( outlinesImage );

		plateViewerMainPanel.showUI( bdv.getBdvHandle().getViewerPanel() );
	}

	public String getFileNamingScheme()
	{
		return fileNamingScheme;
	}

	private void addSiteAndWellNamesOverlay( MultiWellImg multiWellImg )
	{
		final WellNamesOverlay wellNamesOverlay = new WellNamesOverlay( this );

		BdvFunctions.showOverlay(
				wellNamesOverlay,
				"well names",
				BdvOptions.options().addTo( bdv ) );

		BdvOverlay bdvOverlay = new BdvSiteAndWellNamesOverlay(
				bdv,
				multiWellImg.getLoader() );

		// TODO: wrap it into a BDViewable
		final BdvOverlaySource< BdvOverlay > overlaySource = BdvFunctions.showOverlay(
				bdvOverlay,
				"site and well names - overlay",
				BdvOptions.options().addTo( bdv ) );
	}

	public static String getImageNamingScheme( List< File > fileList )
	{
		final String namingScheme = Utils.getNamingScheme( fileList.get( 0 ) );
		Utils.log( "Detected naming scheme: " + namingScheme );
		return namingScheme;
	}

	public static List< File > getFiles( String inputDirectory, String filePattern )
	{
		Utils.log( "Fetching files..." );
		final List< File > fileList = FileUtils.getFileList( new File( inputDirectory ), filePattern );
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
		for ( String channelPattern : channelPatterns )
		{
			final String channelName = channelPattern;
			Utils.log( "Adding channel: " + channelName );
			List< File > channelFiles = getChannelFiles( fileList, namingScheme, channelName );


			MultiWellImg wellImg;

			if ( namingScheme.equals( NamingSchemes.PATTERN_NIKON_TI2_HDF5 ) )
			{
				final MultiResolutionBatchLibHdf5ChannelSourceCreator sourceCreator = new MultiResolutionBatchLibHdf5ChannelSourceCreator(
						namingScheme,
						channelName,
						channelFiles );

				sourceCreator.create();

				wellImg = sourceCreator.getMultiWellHdf5CachedCellImage();

				wellImg.setSource( sourceCreator.getSource() );

				multiWellImgs.add( wellImg );

				addToBdvAndPanel( wellImg );
			}
			else
			{
				wellImg =
						new MultiWellImagePlusImg(
								channelFiles,
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

				addSiteAndWellNamesOverlay( wellImg );

				isFirstChannel = false;
			}
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
		wellNameToInterval = new HashMap<>();
		intervalToSiteName = new HashMap<>();

		final ArrayList< SingleSiteChannelFile > siteChannelFiles =
				multiWellImg.getLoader().getSingleSiteChannelFiles();

		for ( SingleSiteChannelFile channelFile : siteChannelFiles )
		{
			wellNameToInterval.put( channelFile.getSiteName(), channelFile.getInterval() );
			intervalToSiteName.put( channelFile.getInterval(), channelFile.getSiteName() );
		}

		final Interval siteInterval = wellNameToInterval.values().iterator().next();
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
		intervalToSiteName = new HashMap<>();

		int sourceIndex = 0; // channel 0

		final ArrayList< SingleSiteChannelFile > singleSiteChannelFiles =
				multiWellImg.getLoader().getSingleSiteChannelFiles();

		FinalInterval union = null;

		final ArrayList< String > wellNames = multiWellImg.getWellNames();

		for ( String wellName : wellNames )
		{
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

	public void zoomToInterval ( Interval interval )
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
		zoomToInterval( wellNameToInterval.get( siteName ) );

		if ( selectionModel != null )
			notifyImageSelectionModel( siteName );
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

		double[] shiftToImage = new double[ 3 ];

		for ( int d = 0; d < 2; ++d )
		{
			shiftToImage[ d ] = -( interval.min( d ) + interval.dimension( d ) / 2.0 );
		}

		affineTransform3D.translate( shiftToImage );

		affineTransform3D.scale( 1.05 * bdvWindowSize[ 0 ] / interval.dimension( 0 ) );

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

	private void initBdvAndPlateViewerUI ( BDViewable bdViewable )
	{
		final ArrayImg< BitType, LongArray > dummyImageForInitialisation
				= ArrayImgs.bits( new long[]{ 100, 100 } );

		BdvSource bdvTmpSource = BdvFunctions.show(
				dummyImageForInitialisation,
				"",
				Bdv.options()
						.is2D()
						.preferredSize( 600, 600 )
						.doubleBuffered( false )
						.transformEventHandlerFactory(
								new BehaviourTransformEventHandlerPlanar
										.BehaviourTransformEventHandlerPlanarFactory() ) );

		bdv = bdvTmpSource.getBdvHandle();

		plateViewerMainPanel = new PlateViewerMainPanel( this );

		new BdvGrayValuesOverlay( bdv, Utils.bdvTextOverlayFontSize );

		setBdvBehaviors();

		addToBdvAndPanel( bdViewable );

		zoomToInterval( multiWellImgs.get( 0 ).getLoader().getChannelSource( 0 ).getInterval() );

		bdvTmpSource.removeFromBdv();
	}

	public void addToBdvAndPanel( BDViewable bdViewable )
	{
		if ( bdv == null )
		{
			initBdvAndPlateViewerUI( bdViewable );
			return;
		}

		BdvStackSource bdvStackSource = addToBdv( bdViewable );

		//multiWellCachedCellImg.setBdvSource( bdvStackSource );

		bdvStackSource.setDisplayRange( bdViewable.getContrastLimits()[ 0 ], bdViewable.getContrastLimits()[ 1 ] );

		bdvStackSource.setColor( bdViewable.getColor() );

		bdvStackSource.setActive( bdViewable.isInitiallyVisible() );

		// TODO: if it is a Segmentation, do not show the color button
		// => give the whole multiWellCachedCellImg to this function

		plateViewerMainPanel.getSourcesPanel().addToPanel(
				bdViewable.getName(),
				bdvStackSource,
				bdViewable.getColor(),
				bdViewable.isInitiallyVisible() );
	}

	public BdvStackSource addToBdv( BDViewable bdViewable )
	{
		if ( bdViewable.getSource() != null )
		{
			Source< ? > source = bdViewable.getSource();

			if ( bdViewable.getType().equals( Metadata.Type.Segmentation ) )
			{
				source = new ARGBConvertedRealSource(
						source,
						new LazyLabelsARGBConverter() );
			}

			return BdvFunctions.show(
					source,
					BdvOptions.options().addTo( bdv ) );
		}
		else
		{
			RandomAccessibleInterval< ? > rai = bdViewable.getRAI();

			try
			{
				rai = VolatileViews.wrapAsVolatile( rai, loadingQueue );
			}
			catch ( Exception e )
			{
				Logger.info( "Could not wrap as Volatile: " + bdViewable.getName());
			}

			if ( bdViewable.getType().equals( Metadata.Type.Segmentation ) )
			{
				rai = Converters.convert(
						(RandomAccessibleInterval) rai,
						new RandomARGBConverter(),
						new VolatileARGBType() );
			}

			return BdvFunctions.show(
					rai,
					bdViewable.getName(),
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
}
