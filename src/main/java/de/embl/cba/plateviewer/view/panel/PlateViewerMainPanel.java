package de.embl.cba.plateviewer.view.panel;

import bdv.util.*;
import bdv.util.volatiles.VolatileViews;
import de.embl.cba.bdv.utils.Logger;
import de.embl.cba.plateviewer.util.Utils;
import de.embl.cba.plateviewer.image.channel.MultiWellFilteredImg;
import de.embl.cba.plateviewer.view.ImagePlateViewer;
import de.embl.cba.plateviewer.bdv.SimpleScreenShotMaker;
import de.embl.cba.plateviewer.filter.ImageFilter;
import de.embl.cba.plateviewer.filter.ImageFilterSettings;
import de.embl.cba.plateviewer.image.channel.MultiWellImg;
import de.embl.cba.tables.SwingUtils;
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

import static de.embl.cba.plateviewer.filter.ImageFilterUI.getImageFilterSettingsFromUI;

public class PlateViewerMainPanel< R extends RealType< R > & NativeType< R > >
		extends JPanel implements ActionListener
{
	JFrame frame;
	JComboBox siteNamesComboBox;
	JComboBox wellNamesComboBox;

	JComboBox< String > imagesSourcesComboBox;
	JComboBox imageFiltersComboBox;
	JButton imageFiltersButton;

	final ImagePlateViewer< R, ? > imagePlateViewer;
	private final boolean showProcessingPanel;
	private ImageFilterSettings previousImageFilterSettings;
	private final PlateViewerSourcesPanel< R > sourcesPanel;

	public PlateViewerMainPanel( ImagePlateViewer< R, ? > imagePlateViewer, boolean showProcessingPanel )
	{
		this.imagePlateViewer = imagePlateViewer;
		this.showProcessingPanel = showProcessingPanel;
		sourcesPanel = new PlateViewerSourcesPanel< >( this );
	}

	public ImagePlateViewer getImagePlateViewer()
	{
		return imagePlateViewer;
	}

	private void createPanel()
	{
		addHeader( " " ,this );

		addSourceSelectionDialog( this );

		this.add( sourcesPanel.getPanel() );

		addHeader( " " ,this );

		addWellNavigationUI( this );

		addSiteNavigationUI( this );

		addHeader( " " ,this );

		addViewCaptureUI( this );

		if ( showProcessingPanel )
		{
			addHeader( " ", this );

			addImageProcessingPanel( this );

			previousImageFilterSettings = new ImageFilterSettings();
		}
	}

	public PlateViewerSourcesPanel< R > getSourcesPanel()
	{
		return sourcesPanel;
	}

	private void addViewCaptureUI( JPanel panel )
	{
		JPanel horizontalLayoutPanel = horizontalLayoutPanel();
//		final JTextField numPixelsTextField = new JTextField( "1000" );

		final JButton button = new JButton( "Make Screenshot" );

		button.addActionListener( e -> {
			SimpleScreenShotMaker.getSimpleScreenShot(
					imagePlateViewer.getBdvHandle().getViewerPanel(), imagePlateViewer.getOverlays() ).show();
//			BdvViewCaptures.captureView(
//					bdv.getBdvHandle(),
//					1.0,
//					"pixel",
//					true ).rgbImage.show();
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

		horizontalLayoutPanel.add( new JLabel( "Processing image:" ) );

		imagesSourcesComboBox = new JComboBox();

		updateImagesSourcesComboBoxItems();

		horizontalLayoutPanel.add( imagesSourcesComboBox );

		panel.add( horizontalLayoutPanel );
	}

	public void updateImagesSourcesComboBoxItems()
	{
		imagesSourcesComboBox.removeAllItems();

		for( MultiWellImg img : imagePlateViewer.getChannelToMultiWellImg().values() )
		{
			imagesSourcesComboBox.addItem( img.getName() );
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

		siteNamesComboBox = new JComboBox( );

		setComboBoxDimensions( siteNamesComboBox );

		final ArrayList< String > siteNames = imagePlateViewer.getSiteNames();

		for ( String siteName : siteNames )
		{
			siteNamesComboBox.addItem( siteName );
		}

		final JButton button = new JButton( "focus");
		button.addActionListener( e ->
		{
			SwingUtilities.invokeLater( () ->
			{
				imagePlateViewer.focusSite( ( String ) siteNamesComboBox.getSelectedItem() );
				updateBdv( 1000 );
			} );
		} );

		final JLabel jLabel = de.embl.cba.plateviewer.swing.SwingUtils.getJLabel( "Site" );
		horizontalLayoutPanel.add( jLabel );
		horizontalLayoutPanel.add( siteNamesComboBox );
		horizontalLayoutPanel.add( button );

		panel.add( horizontalLayoutPanel );
	}

	public void addWellNavigationUI( JPanel panel )
	{
		final JPanel horizontalLayoutPanel = SwingUtils.horizontalLayoutPanel();

		wellNamesComboBox = new JComboBox();

		setComboBoxDimensions( wellNamesComboBox );

		final ArrayList< String > wellNames = imagePlateViewer.getWellNames();

		Collections.sort( wellNames );

		for ( String wellName : wellNames )
		{
			wellNamesComboBox.addItem( wellName );
		}

		final JButton button = new JButton( "focus");
		button.addActionListener( e ->
		{
			SwingUtilities.invokeLater( () ->
			{
				imagePlateViewer.focusWell( ( String ) wellNamesComboBox.getSelectedItem() );
				updateBdv( 1000 );
			} );
		} );

		final JLabel jLabel = de.embl.cba.plateviewer.swing.SwingUtils.getJLabel( "Well" );
		horizontalLayoutPanel.add( jLabel );
		horizontalLayoutPanel.add( wellNamesComboBox );
		horizontalLayoutPanel.add( button );

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
			imagePlateViewer.getBdvHandle().getBdvHandle().getViewerPanel().requestRepaint();
		} )).start();
	}

	@Override
	public void actionPerformed( ActionEvent a )
	{
		if ( a.getSource() == imageFiltersButton )
		{
			SwingUtilities.invokeLater( () ->
			{
				new Thread( () ->
				{
					// TODO: Fix this and move into own class!
					final MultiWellImg inputSource =
							imagePlateViewer.getChannelToMultiWellImg().get( imagesSourcesComboBox.getSelectedItem() );

					ImageFilterSettings settings = configureImageFilterSettings( inputSource );
					settings = getImageFilterSettingsFromUI( settings );
					if ( settings == null ) return;

					final ImageFilter imageFilter = new ImageFilter( settings );

					final String imageFilterSourceName = imageFilter.getCachedFilterImgName();
					removeSource( imageFilterSourceName );

					final CachedCellImg filterImg = imageFilter.createCachedFilterImg();
					final BdvStackSource bdvStackSource = addToViewer( filterImg, imageFilterSourceName );

					// TODO: make all of this a Bdviewable
					bdvStackSource.setColor( inputSource.getColor() );

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

					final MultiWellImg filteredMultiWellImg =
							new MultiWellFilteredImg(
									filterImg,
									imageFilterSourceName,
									bdvStackSource,
									bdvOverlaySource );

					// TODO: fix this!!
					// multiWellImgs.add( filteredMultiWellImg );

					previousImageFilterSettings = new ImageFilterSettings( settings );

					SwingUtilities.invokeLater( () ->
					{
						updateImagesSourcesComboBoxItems();
					});
				}).start();
			} );
		}


	}

	public ImageFilterSettings configureImageFilterSettings( MultiWellImg inputSource )
	{
		ImageFilterSettings settings = new ImageFilterSettings( previousImageFilterSettings );
		settings.filterType = (String) imageFiltersComboBox.getSelectedItem();
		settings.rai = inputSource.getRAI();
		settings.inputName = ( String ) imagesSourcesComboBox.getSelectedItem();
		settings.imagePlateViewer = imagePlateViewer;
		return settings;
	}

	public BdvOverlaySource addToViewer(
			ImageFilterSettings settings, BdvOverlay bdvOverlay, String name )
	{
		BdvOverlaySource bdvOverlaySource =
				BdvFunctions.showOverlay(
						bdvOverlay,
						name +" - overlay",
						BdvOptions.options().addTo( settings.imagePlateViewer.getBdvHandle() ) );

		return bdvOverlaySource;
	}

	public void removeSource( String name )
	{
		// TODO: Fix this, only needed for the image filter

//		for ( MultiWellImg source : multiWellImgs )
//		{
//			if ( source.getName().equals( name ) )
//			{
//				source.dispose();
//				multiWellImgs.remove( source );
//				source = null;
//				System.gc();
//				break;
//			}
//		}
//
//		updateImagesSourcesComboBoxItems();
	}

	private BdvStackSource addToViewer(
			CachedCellImg< UnsignedByteType, ? > cachedCellImg,
			String cachedFilterImgName )
	{

		final BdvStackSource< Volatile< UnsignedByteType > > bdvStackSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( cachedCellImg, imagePlateViewer.getLoadingQueue() ),
				cachedFilterImgName,
				BdvOptions.options().addTo( imagePlateViewer.getBdvHandle() ) );

		return bdvStackSource;
	}

	private void addSourceSelectionDialog( JPanel panel )
	{
		final ArrayList< String > channels = Utils.getSortedList( imagePlateViewer.getChannelNames() );
		final JComboBox< String > comboBox = new JComboBox( channels.toArray( new String[]{} ) );
		setComboBoxDimensions( comboBox );
		addSourceSelectionComboBoxAndButton( panel, comboBox );
	}

	public static final String PROTOTYPE_DISPLAY_VALUE = "01234567890123456789";
	public static final int COMBOBOX_WIDTH = 200;

	private void setComboBoxDimensions( JComboBox< String > comboBox )
	{
		comboBox.setPrototypeDisplayValue( PROTOTYPE_DISPLAY_VALUE );
		comboBox.setPreferredSize( new Dimension( COMBOBOX_WIDTH, 20 ) );
		comboBox.setMaximumSize( new Dimension( COMBOBOX_WIDTH, 20 ) );
	}

	private void addSourceSelectionComboBoxAndButton(
			final JPanel panel,
			final JComboBox comboBox )
	{
		if ( comboBox.getModel().getSize() == 0 ) return;

		final JPanel horizontalLayoutPanel = SwingUtils.horizontalLayoutPanel();

		final JButton action = new JButton( "view");
		action.addActionListener( e ->
		{
			SwingUtilities.invokeLater( () ->
			{
				if ( imagePlateViewer.getBdvHandle() == null )
				{
					Logger.log( "Warning: Source cannot be added yet, because BigDataViewer is still being initialised..." );
					return;
				}

				final String selectedSource = ( String ) comboBox.getSelectedItem();
				imagePlateViewer.addToPanelAndBdv( selectedSource );
			} );
		} );


		final JLabel comp = de.embl.cba.plateviewer.swing.SwingUtils.getJLabel( "Channel" );

		horizontalLayoutPanel.add( comp );
		horizontalLayoutPanel.add( comboBox );
		horizontalLayoutPanel.add( action );

		panel.add( horizontalLayoutPanel );
	}


	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	public void show( Component parentComponent )
	{
		createPanel();

		frame = new JFrame( "Plate viewer" );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		if ( parentComponent != null )
		{
			frame.setLocation(
					parentComponent.getLocationOnScreen().x + parentComponent.getWidth() + 10,
					parentComponent.getLocationOnScreen().y
			);
		}

		//Create and set up the content pane.
		setOpaque( true ); //content panes must be opaque
		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS ) );

		frame.setContentPane( this );

		//Display the window.
		//frame.setMini(frame.getPreferredSize());
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