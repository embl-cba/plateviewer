package de.embl.cba.plateviewer.ui;

import bdv.util.*;
import bdv.util.volatiles.VolatileViews;
import de.embl.cba.bdv.utils.capture.BdvViewCaptures;
import de.embl.cba.plateviewer.PlateViewer;
import de.embl.cba.plateviewer.imagefilter.ImageFilter;
import de.embl.cba.plateviewer.imagefilter.ImageFilterSettings;
import de.embl.cba.plateviewer.imagesources.ImagesSource;
import net.imglib2.Volatile;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import static de.embl.cba.plateviewer.imagefilter.ImageFilterUI.getImageFilterSettingsFromUI;

public class PlateViewerUI< R extends RealType< R > & NativeType< R > >
		extends JPanel implements ActionListener
{
	JFrame frame;
	JComboBox imageNamesComboBox;
	JComboBox wellNamesComboBox;

	JComboBox imagesSourcesComboBox;
	JComboBox imageFiltersComboBox;
	JButton imageFiltersButton;

	final PlateViewer plateViewer;
	private final Bdv bdv;
	private ArrayList< ImagesSource< R > > imagesSources;
	private ImageFilterSettings previousImageFilterSettings;
	private final PlateViewerSourcesPanel< R > sourcesPanel;


	public PlateViewerUI( PlateViewer plateViewer )
	{
		this.plateViewer = plateViewer;

		this.bdv = plateViewer.getBdv();

		sourcesPanel = new PlateViewerSourcesPanel< >( this );
	}

	public BdvHandle getBdv()
	{
		return bdv.getBdvHandle();
	}

	public void showUI()
	{
		setImagesSources( );

		this.add( sourcesPanel.getPanel() );

		addHeader( " " ,this );

		// addNavigationMessages( this );

		addWellNavigationUI( this );

		addSiteNavigationUI( this );

		addHeader( " " ,this );

		addViewCaptureUI( this );

		addHeader( " " ,this );

		addImageProcessingPanel( this );

		createAndShowUI( );

		previousImageFilterSettings = new ImageFilterSettings( );
	}

	public PlateViewerSourcesPanel< R > getSourcesPanel()
	{
		return sourcesPanel;
	}


	private void setImagesSources( )
	{
		this.imagesSources = new ArrayList<>(  );

		final ArrayList< ImagesSource > imagesSources = plateViewer.getImagesSources();

		for( ImagesSource imagesSource : imagesSources )
		{
			this.imagesSources.add( imagesSource );
		}
	}

	private void addViewCaptureUI( JPanel panel )
	{

		JPanel horizontalLayoutPanel = horizontalLayoutPanel();
//		final JTextField numPixelsTextField = new JTextField( "1000" );

		final JButton button = new JButton( "Capture current view" );

		button.addActionListener( e -> {
			BdvViewCaptures.captureView(
					bdv.getBdvHandle(),
					1.0,
					"pixel",
					true );
		} );

		horizontalLayoutPanel.add( button );
//		horizontalLayoutPanel.add( new JLabel( "Size [pixels]" ) );
//		horizontalLayoutPanel.add( numPixelsTextField );

		panel.add( horizontalLayoutPanel );
	}

	private void addImageProcessingPanel( JPanel panel )
	{
		addImageProcessingComboBox( panel );
		addImagesSourceSelectionPanel( panel);
		addImageProcessingButton( panel );
	}

	private JPanel horizontalLayoutPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout(panel, BoxLayout.LINE_AXIS) );
		panel.setBorder( BorderFactory.createEmptyBorder(0, 10, 10, 10) );
		panel.add( Box.createHorizontalGlue() );
		panel.setAlignmentX( Component.LEFT_ALIGNMENT );
		return panel;
	}

	private void addImageProcessingButton( JPanel panel )
	{
		final JPanel horizontalLayoutPanel = horizontalLayoutPanel();

		imageFiltersButton = new JButton();
		imageFiltersButton.setText( "Process" );
		imageFiltersButton.addActionListener( this );
		horizontalLayoutPanel.add( imageFiltersButton );

		panel.add( horizontalLayoutPanel );
	}


	private void addImageProcessingComboBox( JPanel panel )
	{
		final JPanel horizontalLayoutPanel = horizontalLayoutPanel();

		horizontalLayoutPanel.add( new JLabel( "Processing method: " ) );

		imageFiltersComboBox = new JComboBox();

		for( String filter : ImageFilter.getFilters() )
		{
			imageFiltersComboBox.addItem( filter );
		}

		horizontalLayoutPanel.add( imageFiltersComboBox );

		panel.add( horizontalLayoutPanel );
	}

	private void addHeader( String text, JPanel panel )
	{
		final JPanel horizontalLayoutPanel = horizontalLayoutPanel();

		horizontalLayoutPanel.add( new JLabel( text ) );

		panel.add( horizontalLayoutPanel );
	}

	private void addImagesSourceSelectionPanel( JPanel panel )
	{
		final JPanel horizontalLayoutPanel = horizontalLayoutPanel();

		horizontalLayoutPanel.add( new JLabel( "Processing source:" ) );

		imagesSourcesComboBox = new JComboBox();

		updateImagesSourcesComboBoxItems();

		horizontalLayoutPanel.add( imagesSourcesComboBox );

		panel.add( horizontalLayoutPanel );
	}

	public void updateImagesSourcesComboBoxItems()
	{
		imagesSourcesComboBox.removeAllItems();

		for( ImagesSource source : imagesSources )
		{
			imagesSourcesComboBox.addItem( source.getName() );
		}

		imagesSourcesComboBox.updateUI();
	}

	public void addNavigationMessages( JPanel panel )
	{
		final JPanel horizontalLayoutPanel = horizontalLayoutPanel();
		horizontalLayoutPanel.add( new JLabel( "Zoom in/ out: ARROW UP/ DOWN" ));
		panel.add( horizontalLayoutPanel );

		final JPanel horizontalLayoutPanel3 = horizontalLayoutPanel();
		horizontalLayoutPanel3.add( new JLabel( "Translate: DRAG MOUSE with LEFT (or RIGHT) BUTTON PRESSED") );
		panel.add( horizontalLayoutPanel3 );
	}

	public void addSiteNavigationUI( JPanel panel )
	{
		final JPanel horizontalLayoutPanel = horizontalLayoutPanel();

		imageNamesComboBox = new JComboBox( );

		final ArrayList< String > siteNames = plateViewer.getSiteNames();

		for ( String siteName : siteNames )
		{
			imageNamesComboBox.addItem( siteName );
		}
		imageNamesComboBox.addActionListener( this );

		horizontalLayoutPanel.add( new JLabel( "Zoom to image: " ) );
		horizontalLayoutPanel.add( imageNamesComboBox );

		panel.add( horizontalLayoutPanel );
	}

	public void addWellNavigationUI( JPanel panel )
	{
		final JPanel horizontalLayoutPanel = horizontalLayoutPanel();

		wellNamesComboBox = new JComboBox();

		final ArrayList< String > wellNames = plateViewer.getWellNames();

		Collections.sort( wellNames );

		for ( String wellName : wellNames )
		{
			wellNamesComboBox.addItem( wellName );
		}

		wellNamesComboBox.addActionListener( this );

		horizontalLayoutPanel.add( new JLabel( "Zoom to well: " ) );
		horizontalLayoutPanel.add( wellNamesComboBox );

		panel.add( horizontalLayoutPanel );
	}

	public void updateBdv( long msecs )
	{
		(new Thread( () -> {
			try
			{
				Thread.sleep( msecs );
			} catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
			plateViewer.getBdv().getBdvHandle().getViewerPanel().requestRepaint();
		} )).start();
	}

	@Override
	public void actionPerformed( ActionEvent a )
	{
		if ( a.getSource() == imageNamesComboBox )
		{
			plateViewer.zoomToImage( ( String ) imageNamesComboBox.getSelectedItem() );
			updateBdv( 1000 );
		}

		if ( a.getSource() == wellNamesComboBox )
		{
			plateViewer.zoomToWell( ( String ) wellNamesComboBox.getSelectedItem() );
			updateBdv( 2000 );
		}

		if ( a.getSource() == imageFiltersButton )
		{
			SwingUtilities.invokeLater( () ->
			{
				new Thread( () ->
				{
					final ImagesSource inputSource =
							imagesSources.get( imagesSourcesComboBox.getSelectedIndex() );

					ImageFilterSettings settings = configureImageFilterSettings( inputSource );
					settings = getImageFilterSettingsFromUI( settings );
					if ( settings == null ) return;

					final ImageFilter imageFilter = new ImageFilter( settings );

					final String imageFilterSourceName = imageFilter.getCachedFilterImgName();
					removeSource( imageFilterSourceName );

					final CachedCellImg filterImg = imageFilter.createCachedFilterImg();
					final BdvStackSource bdvStackSource =
							addToViewer( filterImg, imageFilterSourceName );

					bdvStackSource.setColor( inputSource.getColor() );

					getSourcesPanel().addSourceToPanel(
							imageFilterSourceName,
							bdvStackSource,
							inputSource.getColor(),
							true );

					if ( !settings.filterType.equals( ImageFilter.SIMPLE_SEGMENTATION ) )
					{
						// TODO: do this via checkbox
						// inputSource.getBdvSource().setActive( false );
					}

					BdvOverlaySource bdvOverlaySource = null;
					if ( imageFilter.getBdvOverlay() != null )
					{
							bdvOverlaySource =
									addToViewer( settings,
									imageFilter.getBdvOverlay(), imageFilterSourceName );
					}

					final ImagesSource filteredImagesSource = new ImagesSource( filterImg,
							imageFilterSourceName, bdvStackSource, bdvOverlaySource );

					imagesSources.add( filteredImagesSource );

					previousImageFilterSettings = new ImageFilterSettings( settings );

					SwingUtilities.invokeLater( () ->
					{
						updateImagesSourcesComboBoxItems();
					});
				}).start();
			} );
		}


	}

	public ImageFilterSettings configureImageFilterSettings( ImagesSource inputSource )
	{
		ImageFilterSettings settings = new ImageFilterSettings( previousImageFilterSettings );
		settings.filterType = (String) imageFiltersComboBox.getSelectedItem();
		settings.inputCachedCellImg = inputSource.getCachedCellImg();
		settings.inputName = ( String ) imagesSourcesComboBox.getSelectedItem();
		settings.plateViewer = plateViewer;
		return settings;
	}

	public BdvOverlaySource addToViewer(
			ImageFilterSettings settings, BdvOverlay bdvOverlay, String name )
	{
		BdvOverlaySource bdvOverlaySource =
				BdvFunctions.showOverlay(
						bdvOverlay,
						name +" - overlay",
						BdvOptions.options().addTo( settings.plateViewer.getBdv() ) );

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

		updateImagesSourcesComboBoxItems();
	}


	private BdvStackSource addToViewer(
			CachedCellImg< UnsignedByteType, ? > cachedCellImg,
			String cachedFilterImgName )
	{

		final BdvStackSource< Volatile< UnsignedByteType > > bdvStackSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( cachedCellImg, plateViewer.getLoadingQueue() ),
				cachedFilterImgName,
				BdvOptions.options().addTo( plateViewer.getBdv() ) );

		// TODO: set color
//		bdvSource.setColor( settings.baseImagesSource.getColor() );

		return bdvStackSource;

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
		frame = new JFrame( "Plate viewer" );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		getSourcesPanel().sourceNameToPanel.size();

		int height = 0; // TODO: actually, I only want to set the width
		// TODO: this depends on the number of channels!
//		this.setPreferredSize( new Dimension(700, 600) );
//		frame.setPreferredSize( new Dimension(700, 600) );

		//Create and set up the content pane.
		setOpaque( true ); //content panes must be opaque
		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS ) );

		frame.setContentPane( this );

		//Display the window.
		frame.pack();
		frame.setVisible( true );
	}

	public void refreshUI()
	{
		frame.revalidate();
		frame.repaint();
		frame.pack();
	}



}