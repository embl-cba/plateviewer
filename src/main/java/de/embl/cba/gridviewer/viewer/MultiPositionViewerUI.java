package de.embl.cba.gridviewer.viewer;

import bdv.util.*;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.render.TransformAwareBufferedImageOverlayRenderer;
import de.embl.cba.gridviewer.bdv.BdvUtils;
import de.embl.cba.gridviewer.imagefilter.ImageFilter;
import de.embl.cba.gridviewer.imagefilter.ImageFilterSettings;
import de.embl.cba.gridviewer.imagesources.ImagesSource;
import ij.ImagePlus;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MultiPositionViewerUI < T extends NativeType< T > & RealType< T > > extends JPanel implements ActionListener
{
	JFrame frame;
	JComboBox imageNamesComboBox;
	JComboBox wellNamesComboBox;

	JComboBox imagesSourcesComboBox;
	JComboBox imageFiltersComboBox;
	JButton imageFiltersButton;
	JButton imageSourceRemovalButton;


	final MultiPositionViewer multiPositionViewer;
	private final Bdv bdv;
	private ArrayList< ImagesSource< T > > imagesSources;
	private ImageFilterSettings previousImageFilterSettings;


	public MultiPositionViewerUI( MultiPositionViewer multiPositionViewer )
	{

		this.multiPositionViewer = multiPositionViewer;

		this.bdv = multiPositionViewer.getBdv();

		setImagesSources( );

		addSiteNavigationComboBox( );

		addWellNavigationComboBox(  );

		addImagesSourceSelectionPanel( this);

		addImageFilterPanel();

		addCaptureViewPanel( this );

		createAndShowUI( );

		previousImageFilterSettings = new ImageFilterSettings( );
	}

	public void setImagesSources( )
	{
		this.imagesSources = new ArrayList<>(  );

		final ArrayList< ImagesSource > imagesSources = multiPositionViewer.getImagesSources();

		for( ImagesSource imagesSource : imagesSources )
		{
			this.imagesSources.add( imagesSource );
		}
	}



	private void addCaptureViewPanel( JPanel panel )
	{

		JPanel horizontalLayoutPanel = horizontalLayoutPanel();
		final JTextField numPixelsTextField = new JTextField( "1000" );


		final JButton button = new JButton( "Capture current view" );

		button.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				final BufferedImage bufferedImage = BdvUtils.captureView( bdv, Integer.parseInt( numPixelsTextField.getText() ) );
				new ImagePlus( "Capture", bufferedImage ).show();
				//ImageIO.write( target.bi, "png", new File( String.format( "%s/img-%03d.png", dir, timepoint ) ) );
			}
		} );

		horizontalLayoutPanel.add( button );
		horizontalLayoutPanel.add( new JLabel( "Size [pixels]" ) );
		horizontalLayoutPanel.add( numPixelsTextField );

		panel.add( horizontalLayoutPanel );
	}

	private void addImageFilterPanel()
	{
		JPanel panel = horizontalLayoutPanel();

		addImageFiltersButton( panel );

		addImageFiltersComboBox( panel );

		add( panel );
	}


	private JPanel horizontalLayoutPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout(panel, BoxLayout.LINE_AXIS) );
		panel.setBorder( BorderFactory.createEmptyBorder(0, 10, 10, 10) );
		panel.add( Box.createHorizontalGlue() );
		return panel;
	}

	private void addImageFiltersButton( JPanel panel )
	{
		imageFiltersButton = new JButton();
		imageFiltersButton.setText( "Compute" );
		imageFiltersButton.addActionListener( this );
		panel.add( imageFiltersButton );
	}

	private void addImageSourceRemovalButton( JPanel panel )
	{
		imageSourceRemovalButton = new JButton();
		imageSourceRemovalButton.setText( "Remove" );
		imageSourceRemovalButton.addActionListener( this );
		panel.add( imageSourceRemovalButton );
	}

	private void addImageFiltersComboBox( JPanel panel )
	{
		imageFiltersComboBox = new JComboBox();

		for( String filter : ImageFilter.getFilters() )
		{
			imageFiltersComboBox.addItem( filter );
		}

		imageFiltersComboBox.addActionListener( this );

		panel.add( imageFiltersComboBox );
	}

	private void addImagesSourceSelectionPanel( JPanel panel )
	{
		final JPanel horizontalLayoutPanel = horizontalLayoutPanel();

		horizontalLayoutPanel.add( new JLabel( "Image source" ) );

		imagesSourcesComboBox = new JComboBox();

		updateImagesSourcesComboBoxItems();

		horizontalLayoutPanel.add( imagesSourcesComboBox );

		addImageSourceRemovalButton( horizontalLayoutPanel );

		add( horizontalLayoutPanel );
	}

	private void updateImagesSourcesComboBoxItems()
	{
		imagesSourcesComboBox.removeAllItems();

		for( ImagesSource source : imagesSources )
		{
			imagesSourcesComboBox.addItem( source.getName() );
		}

		imagesSourcesComboBox.updateUI();
	}


	public void addSiteNavigationComboBox( )
	{
		imageNamesComboBox = new JComboBox( );

		final ArrayList< String > siteNames = multiPositionViewer.getSiteNames();

		for ( String siteName : siteNames )
		{
			imageNamesComboBox.addItem( siteName );
		}
		imageNamesComboBox.addActionListener( this );

		add( imageNamesComboBox );
	}

	public void addWellNavigationComboBox( )
	{
		wellNamesComboBox = new JComboBox();

		final ArrayList< String > wellNames = multiPositionViewer.getWellNames();
		Collections.sort( wellNames );

		for ( String wellName : wellNames )
		{
			wellNamesComboBox.addItem( wellName );
		}

		wellNamesComboBox.addActionListener( this );
		add( wellNamesComboBox );
	}

	public void updateBdv( long msecs )
	{
		(new Thread(new Runnable(){
			public void run(){
				try
				{
					Thread.sleep( msecs );
				} catch ( InterruptedException e )
				{
					e.printStackTrace();
				}
				multiPositionViewer.getBdv().getBdvHandle().getViewerPanel().requestRepaint();
			}
		})).start();
	}

	@Override
	public void actionPerformed( ActionEvent e )
	{
		if ( e.getSource() == imageNamesComboBox )
		{
			multiPositionViewer.zoomToImage( ( String ) imageNamesComboBox.getSelectedItem() );
			updateBdv( 1000 );
		}

		if ( e.getSource() == wellNamesComboBox )
		{
			multiPositionViewer.zoomToWell( ( String ) wellNamesComboBox.getSelectedItem() );
			updateBdv( 2000 );
		}

		if ( e.getSource() == imageSourceRemovalButton )
		{
			final ImagesSource inputSource = imagesSources.get( imagesSourcesComboBox.getSelectedIndex() );
			removeSource( inputSource.getName() );
			updateImagesSourcesComboBoxItems();
		}

		if ( e.getSource() == imageFiltersButton )
		{

			final ImagesSource inputSource = imagesSources.get( imagesSourcesComboBox.getSelectedIndex() );

			ImageFilterSettings settings = new ImageFilterSettings( previousImageFilterSettings );

			settings.filterType = (String) imageFiltersComboBox.getSelectedItem();
			settings.inputCachedCellImg = inputSource.getCachedCellImg();
			settings.inputName = ( String ) imagesSourcesComboBox.getSelectedItem();
			settings.multiPositionViewer = multiPositionViewer;

			final ImageFilter imageFilter = new ImageFilter( settings );
			final String name = imageFilter.getCachedFilterImgName();

			removeSource( name );

			final CachedCellImg img = imageFilter.createCachedFilterImg();

			final BdvSource bdvSource = addToViewer( img, name );

			// TODO: setLut( bdvSource, inputSource, settings.filterType );

			if ( !settings.filterType.equals( ImageFilter.SIMPLE_SEGMENTATION ) )
			{
				inputSource.getBdvSource().setActive( false );
			}

			BdvOverlaySource bdvOverlaySource = addToViewer( settings, imageFilter.getBdvOverlay(), name );

			imagesSources.add( new ImagesSource( img, name, bdvSource, bdvOverlaySource ) );

			previousImageFilterSettings = new ImageFilterSettings( settings );

			updateImagesSourcesComboBoxItems();

		}


	}

	public BdvOverlaySource addToViewer( ImageFilterSettings settings, BdvOverlay bdvOverlay, String name )
	{
		BdvOverlaySource bdvOverlaySource = null;

		if ( bdvOverlay != null )
		{
			bdvOverlaySource = BdvFunctions.showOverlay( bdvOverlay,  name +" - overlay", BdvOptions.options().addTo( settings.multiPositionViewer.getBdv() ) );
		}

		return bdvOverlaySource;
	}

	public void removeSource( String name )
	{
		for ( ImagesSource source : imagesSources )
		{
			if ( source.getName().equals( name ) )
			{
				source.dispose();
				imagesSources.remove( source );
				source = null;
				System.gc();
				break;
			}
		}
	}


	private BdvSource addToViewer( CachedCellImg< UnsignedByteType, ? > cachedCellImg, String cachedFilterImgName )
	{

		BdvSource bdvSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( cachedCellImg, multiPositionViewer.getLoadingQueue() ),
				cachedFilterImgName,
				BdvOptions.options().addTo( multiPositionViewer.getBdv() ) );

		// TODO: set color
//		bdvSource.setColor( settings.baseImagesSource.getArgbType() );

		return bdvSource;

	}

	public static void setLut( BdvSource bdvSource, ImagesSource imagesSource, String filterType )
	{
		final double[] lutMinMax = imagesSource.getLutMinMax();

		if ( filterType.equals( ImageFilter.MEDIAN_DEVIATION ) )
		{
			bdvSource.setDisplayRange( 0, lutMinMax[ 1 ] - lutMinMax[ 0 ] );
		}
		else if ( filterType.equals( ImageFilter.SIMPLE_SEGMENTATION ) )
		{
			bdvSource.setDisplayRange( 0, 255 );
		}
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private void createAndShowUI( )
	{
		//Create and set up the window.
		frame = new JFrame( "Multiposition viewer" );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		//Create and set up the content pane.
		setOpaque( true ); //content panes must be opaque
		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS ) );

		frame.setContentPane( this );

		//Display the window.
		frame.pack();
		frame.setVisible( true );
	}

	private void refreshUI()
	{
		this.revalidate();
		this.repaint();
		frame.pack();
	}



}