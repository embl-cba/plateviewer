package de.embl.cba.plateviewer.view;

import bdv.util.*;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.converters.RandomARGBConverter;
import de.embl.cba.bdv.utils.overlays.BdvGrayValuesOverlay;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.io.FileUtils;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.bdv.BdvSiteAndWellNamesOverlay;
import de.embl.cba.plateviewer.bdv.BehaviourTransformEventHandlerPlanar;
import de.embl.cba.plateviewer.source.SingleSiteChannelFile;
import de.embl.cba.plateviewer.source.ChannelSource;
import de.embl.cba.plateviewer.source.MultiWellChannelCachedCellImgProvider;
import de.embl.cba.plateviewer.source.NamingSchemes;
import de.embl.cba.plateviewer.table.ImageName;
import de.embl.cba.plateviewer.view.panel.PlateViewerMainPanel;
import de.embl.cba.tables.Logger;
import de.embl.cba.tables.select.SelectionListener;
import de.embl.cba.tables.select.SelectionModel;
import net.imglib2.FinalInterval;
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

public class PlateViewerImageView < R extends NativeType< R > & RealType< R >, T extends ImageName >
{
	private final ArrayList< MultiWellChannelCachedCellImgProvider > multiWellChannelCachedCellImgProviders;
	private int numIoThreads;
	private final SharedQueue loadingQueue;

	private Bdv bdv;
	private PlateViewerMainPanel plateViewerMainPanel;
	private List< T > imageFileNames;
	private SelectionModel< T > selectionModel;
	private final String fileNamingScheme;

	public PlateViewerImageView( String inputDirectory, String filterPattern, int numIoThreads )
	{
		this.multiWellChannelCachedCellImgProviders = new ArrayList<>();
		this.numIoThreads = numIoThreads;
		this.loadingQueue = new SharedQueue( numIoThreads );

		final List< File > fileList = getFiles( inputDirectory, filterPattern );

		fileNamingScheme = getImageNamingScheme( fileList );

		if ( fileNamingScheme.equals( NamingSchemes.PATTERN_CORONA_HDF5 ) )
			this.numIoThreads = 1; // Hdf5 does not support multi-threading

		final List< String > channelPatterns =
				Utils.getChannelPatterns( fileList, fileNamingScheme );

		addChannelsToViewer( fileList, fileNamingScheme, channelPatterns );

		addSiteAndWellNamesOverlay();

		plateViewerMainPanel.showUI( bdv.getBdvHandle().getViewerPanel() );
	}

	public String getFileNamingScheme()
	{
		return fileNamingScheme;
	}

	private void addSiteAndWellNamesOverlay()
	{
		BdvOverlay bdvOverlay = new BdvSiteAndWellNamesOverlay( bdv, multiWellChannelCachedCellImgProviders );
		BdvFunctions.showOverlay(
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

	public void addChannelsToViewer(
			List< File > fileList,
			String namingScheme,
			List< String > channelPatterns )
	{
		for ( String channelPattern : channelPatterns )
		{
			Utils.log( "Adding channel: " + channelPattern );
			List< File > channelFiles = getChannelFiles( fileList, namingScheme, channelPattern );

			final MultiWellChannelCachedCellImgProvider multiWellChannelCachedCellImgProvider =
					new MultiWellChannelCachedCellImgProvider( channelFiles, namingScheme, numIoThreads, channelPattern );

			final int numResolutionLevels = getNumResolutionLevels( channelFiles.get( 0 ), channelPattern );
			if ( numResolutionLevels > 1 )
			{
				addMultiResolutionChannelSource( namingScheme, channelPattern, channelFiles, multiWellChannelCachedCellImgProvider, numResolutionLevels );
			}
			else
			{
				addSourceToBdvAndPanel( multiWellChannelCachedCellImgProvider, null );
			}
		}
	}

	public void addMultiResolutionChannelSource( String namingScheme, String channelPattern, List< File > channelFiles, MultiWellChannelCachedCellImgProvider highestResolutionSource, int numResolutionLevels )
	{
		final HashMap< Double, RandomAccessibleInterval< R > > scaleToRai = new HashMap<>();
		scaleToRai.put( 1.0, highestResolutionSource.getCachedCellImg() );

		for ( int resolutionLevel = 2; resolutionLevel <= numResolutionLevels; resolutionLevel++ )
		{
			String resolutionLevelChannelPattern = "";
			if ( namingScheme.equals( NamingSchemes.PATTERN_CORONA_HDF5 ) )
			{
				resolutionLevelChannelPattern = channelPattern + "_scale";
			}

			final MultiWellChannelCachedCellImgProvider multiWellChannelCachedCellImgProvider = new MultiWellChannelCachedCellImgProvider( channelFiles, namingScheme, numIoThreads, channelPattern, resolutionLevelChannelPattern  );
			//	scaleToRai.put( 1.0, multiSiteChannelSource.getCachedCellImg() );
		}

		final ChannelSource< R > channelSource =
				new ChannelSource<>( scaleToRai );

		addSourceToBdvAndPanel( highestResolutionSource, channelSource );
	}

	public List< File > getChannelFiles ( List < File > fileList, String namingScheme, String channelPattern )
	{
		List< File > channelFiles;
		if ( namingScheme.equals( NamingSchemes.PATTERN_CORONA_HDF5 ) )
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

	public void zoomToInterval ( FinalInterval interval )
	{
		final AffineTransform3D affineTransform3D = getImageZoomTransform( interval );

		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( affineTransform3D );
	}

	public ArrayList< String > getSiteNames ( )
	{
		final ArrayList< SingleSiteChannelFile > singleSiteChannelFiles =
				this.multiWellChannelCachedCellImgProviders.get( 0 ).getLoader().getSingleSiteChannelFiles();

		final ArrayList< String > imageNames = new ArrayList<>();

		for ( SingleSiteChannelFile singleSiteChannelFile : singleSiteChannelFiles )
		{
			imageNames.add( singleSiteChannelFile.getImageName() );
		}

		return imageNames;
	}

	public ArrayList< String > getWellNames ( )
	{
		return multiWellChannelCachedCellImgProviders.get( 0 ).getWellNames();
	}

	public void zoomToWell ( String wellName )
	{
		int sourceIndex = 0; // channel 0

		final ArrayList< SingleSiteChannelFile > singleSiteChannelFiles = this.multiWellChannelCachedCellImgProviders.get( sourceIndex ).getLoader().getSingleSiteChannelFiles();

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

		zoomToInterval( union );
	}

	public void zoomToSite ( String siteName )
	{
		int sourceIndex = 0;

		final SingleSiteChannelFile singleSiteChannelFile = multiWellChannelCachedCellImgProviders.get( sourceIndex ).getLoader().getChannelSource( siteName );

		zoomToInterval( singleSiteChannelFile.getInterval() );

		if ( selectionModel != null )
			notifyImageSelectionModel( singleSiteChannelFile );
	}

	public void notifyImageSelectionModel ( SingleSiteChannelFile singleSiteChannelFile )
	{
		final String selectedImageFileName = singleSiteChannelFile.getFile().getName();
		for ( T fileName : imageFileNames )
		{
			final String imageFileName = fileName.getSiteName();
			if ( imageFileName.equals( selectedImageFileName ) )
				selectionModel.focus( fileName );
		}
	}

	public boolean isImageExisting ( final SingleCellArrayImg< R, ? > cell )
	{
		final SingleSiteChannelFile imageFile = multiWellChannelCachedCellImgProviders.get( 0 ).getLoader().getChannelSource( cell );

		if ( imageFile != null ) return true;
		else return false;
	}

	public ArrayList< MultiWellChannelCachedCellImgProvider > getMultiWellChannelCachedCellImgProviders( )
	{
		return multiWellChannelCachedCellImgProviders;
	}

	public AffineTransform3D getImageZoomTransform ( FinalInterval interval )
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

	private void initBdvAndPlateViewerUI ( MultiWellChannelCachedCellImgProvider
	source, ChannelSource< R > channelSource )
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

		addSourceToBdvAndPanel( source, channelSource );

		zoomToInterval( source.getLoader().getChannelSource( 0 ).getInterval() );

		bdvTmpSource.removeFromBdv();
	}

	public void addSourceToBdvAndPanel
	( MultiWellChannelCachedCellImgProvider< R > multiWellChannelCachedCellImgProvider, ChannelSource< R > channelSource )
	{
		if ( bdv == null )
		{
			initBdvAndPlateViewerUI( multiWellChannelCachedCellImgProvider, channelSource );
			return;
		}

		if ( channelSource != null )
		{
			// Show this instead of the single resolution one
		}

		RandomAccessibleInterval volatileRai =
				VolatileViews.wrapAsVolatile(
						multiWellChannelCachedCellImgProvider.getCachedCellImg(),
						loadingQueue );

		if ( multiWellChannelCachedCellImgProvider.getType().equals( Metadata.Type.Segmentation ) )
		{
			volatileRai = Converters.convert(
					volatileRai,
					new RandomARGBConverter(),
					new VolatileARGBType() );
		}

		final BdvStackSource bdvStackSource = BdvFunctions.show(
				volatileRai,
				multiWellChannelCachedCellImgProvider.getChannelName(),
				BdvOptions.options().addTo( bdv ) );

		multiWellChannelCachedCellImgProvider.setBdvSource( bdvStackSource );
		multiWellChannelCachedCellImgProviders.add( multiWellChannelCachedCellImgProvider );

		bdvStackSource.setDisplayRange(
				multiWellChannelCachedCellImgProvider.getLutMinMax()[ 0 ], multiWellChannelCachedCellImgProvider.getLutMinMax()[ 1 ] );

		bdvStackSource.setColor( multiWellChannelCachedCellImgProvider.getColor() );

		bdvStackSource.setActive( multiWellChannelCachedCellImgProvider.isInitiallyVisible() );

		plateViewerMainPanel.getSourcesPanel().addSourceToPanel(
				multiWellChannelCachedCellImgProvider.getChannelName(),
				bdvStackSource,
				multiWellChannelCachedCellImgProvider.getColor(),
				multiWellChannelCachedCellImgProvider.isInitiallyVisible() );

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

		final SingleSiteChannelFile singleSiteChannelFile = multiWellChannelCachedCellImgProviders.get( 0 ).getLoader().getChannelSource( coordinates );

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

	public void installImageSelectionModel ( List < T > imageFileNames, SelectionModel < T > selectionModel )
	{
		this.imageFileNames = imageFileNames;
		this.selectionModel = selectionModel;
		registerAsImageSelectionListener( selectionModel );
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
				final SingleSiteChannelFile singleSiteChannelFile = multiWellChannelCachedCellImgProviders.get( sourceIndex ).getLoader().getChannelSource( selection.getSiteName() );

				if ( singleSiteChannelFile == null )
				{
					throw new UnsupportedOperationException( "Could not find image sources for site " + selection.getSiteName() );
				}

				zoomToInterval( singleSiteChannelFile.getInterval() );
			}
		} );
	}

	private int getNumResolutionLevels ( File file, String channelPattern )
	{
		return 1;
	}

}
