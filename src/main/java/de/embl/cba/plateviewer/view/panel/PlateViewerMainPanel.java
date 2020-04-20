package de.embl.cba.plateviewer.view.panel;

import bdv.util.*;
import bdv.util.volatiles.VolatileViews;
import de.embl.cba.plateviewer.image.channel.MultiWellFilteredImg;
import de.embl.cba.plateviewer.view.PlateViewerImageView;
import de.embl.cba.plateviewer.bdv.SimpleScreenShotMaker;
import de.embl.cba.plateviewer.filter.ImageFilter;
import de.embl.cba.plateviewer.filter.ImageFilterSettings;
import de.embl.cba.plateviewer.image.channel.MultiWellImg;
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

	JComboBox imagesSourcesComboBox;
	JComboBox imageFiltersComboBox;
	JButton imageFiltersButton;

	final PlateViewerImageView plateViewerImageView;
	private final Bdv bdv;
	private ArrayList< MultiWellImg< R > > multiWellImgs;
	private ImageFilterSettings previousImageFilterSettings;
	private final PlateViewerSourcesPanel< R > sourcesPanel;

	public PlateViewerMainPanel( PlateViewerImageView plateViewerImageView )
	{
		this.plateViewerImageView = plateViewerImageView;

		this.bdv = plateViewerImageView.getBdv();

		sourcesPanel = new PlateViewerSourcesPanel< >( this );
	}

	public PlateViewerImageView getPlateViewerImageView()
	{
		return plateViewerImageView;
	}

	public BdvHandle getBdv()
	{
		return bdv.getBdvHandle();
	}

	private void createUI()
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

		previousImageFilterSettings = new ImageFilterSettings( );
	}

	public PlateViewerSourcesPanel< R > getSourcesPanel()
	{
		return sourcesPanel;
	}


	private void setImagesSources( )
	{
		this.multiWellImgs = new ArrayList<>(  );

		final ArrayList< MultiWellImg > multiWellImgs = plateViewerImageView.getMultiWellImgs();

		for( MultiWellImg channelSource : multiWellImgs )
		{
			this.multiWellImgs.add( channelSource );
		}
	}

	private void addViewCaptureUI( JPanel panel )
	{
		JPanel horizontalLayoutPanel = horizontalLayoutPanel();
//		final JTextField numPixelsTextField = new JTextField( "1000" );

		final JButton button = new JButton( "Make Screenshot" );

		button.addActionListener( e -> {
			SimpleScreenShotMaker.getSimpleScreenShot( bdv.getBdvHandle().getViewerPanel() ).show();
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

		for( MultiWellImg img : multiWellImgs )
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

		final ArrayList< String > siteNames = plateViewerImageView.getSiteNames();

		for ( String siteName : siteNames )
		{
			siteNamesComboBox.addItem( siteName );
		}
		siteNamesComboBox.addActionListener( this );

		horizontalLayoutPanel.add( new JLabel( "Zoom to site: " ) );
		horizontalLayoutPanel.add( siteNamesComboBox );

		panel.add( horizontalLayoutPanel );
	}

	public void addWellNavigationUI( JPanel panel )
	{
		final JPanel horizontalLayoutPanel = horizontalLayoutPanel();

		wellNamesComboBox = new JComboBox();

		final ArrayList< String > wellNames = plateViewerImageView.getWellNames();

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
			plateViewerImageView.getBdv().getBdvHandle().getViewerPanel().requestRepaint();
		} )).start();
	}

	@Override
	public void actionPerformed( ActionEvent a )
	{
		if ( a.getSource() == siteNamesComboBox )
		{
			plateViewerImageView.zoomToSite( ( String ) siteNamesComboBox.getSelectedItem() );

			updateBdv( 1000 );
		}

		if ( a.getSource() == wellNamesComboBox )
		{
			plateViewerImageView.zoomToWell( ( String ) wellNamesComboBox.getSelectedItem() );
			updateBdv( 2000 );
		}

		if ( a.getSource() == imageFiltersButton )
		{
			SwingUtilities.invokeLater( () ->
			{
				new Thread( () ->
				{
					final MultiWellImg inputSource =
							multiWellImgs.get( imagesSourcesComboBox.getSelectedIndex() );

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

//					getSourcesPanel().addToPanel(
//							imageFilterSourceName,
//							bdvStackSource,
//							inputSource.getColor(),
//							true );

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

					multiWellImgs.add( filteredMultiWellImg );

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
		settings.plateViewerImageView = plateViewerImageView;
		return settings;
	}

	public BdvOverlaySource addToViewer(
			ImageFilterSettings settings, BdvOverlay bdvOverlay, String name )
	{
		BdvOverlaySource bdvOverlaySource =
				BdvFunctions.showOverlay(
						bdvOverlay,
						name +" - overlay",
						BdvOptions.options().addTo( settings.plateViewerImageView.getBdv() ) );

		return bdvOverlaySource;
	}

	public void removeSource( String name )
	{
		for ( MultiWellImg source : multiWellImgs )
		{
			if ( source.getName().equals( name ) )
			{
				source.dispose();
				multiWellImgs.remove( source );
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
				VolatileViews.wrapAsVolatile( cachedCellImg, plateViewerImageView.getLoadingQueue() ),
				cachedFilterImgName,
				BdvOptions.options().addTo( plateViewerImageView.getBdv() ) );

		// TODO: set color
//		bdvSource.setColor( settings.baseImagesSource.getColor() );

		return bdvStackSource;

	}

//	public static void setLut( BdvSource bdvSource, MultiWellCachedCellImg multiWellCachedCellImg, String filterType )
//	{
//		final double[] lutMinMax = multiWellCachedCellImg.getLutMinMax();
//
//		if ( filterType.equals( ImageFilter.MEDIAN_DEVIATION ) )
//		{
//			bdvSource.setDisplayRange( 0, lutMinMax[ 1 ] - lutMinMax[ 0 ] );
//		}
//		else if ( filterType.equals( ImageFilter.SIMPLE_SEGMENTATION ) )
//		{
//			bdvSource.setDisplayRange( 0, 255 );
//		}
//	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	public void showUI( Component parentComponent )
	{
		createUI();

		//Create and set up the window.
		frame = new JFrame( "Plate viewer" );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		if ( parentComponent != null )
		{
			frame.setLocation(
					parentComponent.getLocationOnScreen().x + parentComponent.getWidth() + 10,
					parentComponent.getLocationOnScreen().y
			);
		}


		getSourcesPanel().sourceNameToPanel.size();

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