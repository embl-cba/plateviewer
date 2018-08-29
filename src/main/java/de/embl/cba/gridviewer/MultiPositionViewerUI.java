package de.embl.cba.gridviewer;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MultiPositionViewerUI extends JPanel implements ActionListener
{
	JFrame frame;
	JComboBox imageNamesComboBox;
	JComboBox wellNamesComboBox;

	JComboBox imagesSourcesComboBox;
	JComboBox imageFiltersComboBox;

	final MultiPositionViewer multiPositionViewer;

	private ImageFilter imageFilter;
	private ArrayList< String > derivedImagesSources;


	public MultiPositionViewerUI( MultiPositionViewer multiPositionViewer )
	{

		this.multiPositionViewer = multiPositionViewer;

		this.derivedImagesSources = new ArrayList<>( );

		addImageNavigationComboBox( );

		addWellNavigationComboBox(  );

		addImageFilterPanel();

		createAndShowUI( );
	}

	private void addImageFilterPanel()
	{
		JPanel panel = horizontalLayoutPanel();

		addImageFiltersComboBox( panel );

		addImagesSourceComboBox( panel );
	}


	private JPanel horizontalLayoutPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout(panel, BoxLayout.LINE_AXIS) );
		panel.setBorder( BorderFactory.createEmptyBorder(0, 10, 10, 10) );
		panel.add( Box.createHorizontalGlue() );
		return panel;
	}

	private void addImageFiltersComboBox( JPanel panel )
	{
		imageFiltersComboBox = new JComboBox();

		for( String filter : ImageFilter.getFilters() )
		{
			imageFiltersComboBox.addItem( filter );
		}

		panel.add( imageFiltersComboBox );
	}

	private void addImagesSourceComboBox( JPanel panel )
	{
		imagesSourcesComboBox = new JComboBox();

		updateImagesSourcesComboBoxItems();

		panel.add( imagesSourcesComboBox );
	}

	private void updateImagesSourcesComboBoxItems()
	{
		imagesSourcesComboBox.removeAllItems();

		for( ImagesSource source : multiPositionViewer.getImagesSources() )
		{
			imagesSourcesComboBox.addItem( source.getName() );
		}

		for( String source : derivedImagesSources )
		{
			imagesSourcesComboBox.addItem( source );
		}

		imagesSourcesComboBox.updateUI();
	}


	public void addImageNavigationComboBox( )
	{
		imageNamesComboBox = new JComboBox();

		final ArrayList< ImageSource > imageSources = multiPositionViewer.getImagesSources().get( 0 ).getLoader().getImageSources();
		for ( ImageSource imageSource : imageSources )
		{
			imageNamesComboBox.addItem( imageSource.getFile().getName() );
		}

		imageNamesComboBox.addActionListener( this );
		add( imageNamesComboBox );
	}

	public void addWellNavigationComboBox( )
	{
		wellNamesComboBox = new JComboBox();

		final ArrayList< String > wellNames = multiPositionViewer.getImagesSources().get( 0 ).getWellNames();
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

		final ImagesSource referenceImagesSource = multiPositionViewer.getImagesSources().get( 0 );

		final String selectedSourceName = ( String ) imagesSourcesComboBox.getSelectedItem();

		final CachedCellImg cachedCellImg = referenceImagesSource.getCachedCellImg();


		if ( e.getSource() == imageFiltersComboBox )
		{
			ImageFilterSettings settings = new ImageFilterSettings();
			settings.filterType = (String) imageFiltersComboBox.getSelectedItem();
			settings.imagesSource = multiPositionViewer.getImagesSources().get( 0 );
			settings.inputCachedCellImg = null;

			final ImageFilter imageFilter = new ImageFilter( settings );

			// Remove source from bdv if it exists already (by name)


			// Get cached cell img
			final CachedCellImg cachedFilterImg = imageFilter.getCachedFilterImg();
			final String cachedFilterImgName = imageFilter.getCachedFilterImgName();

			// Add cached img to viewer
			addToViewer( cachedFilterImg, cachedFilterImgName );

			// Hide the source image if appropriate
			settings.imagesSource.getBdvSource().setActive( false );

			//
			derivedImagesSources.add( cachedFilterImgName );
		}

		updateImagesSourcesComboBoxItems();
	}


	private BdvSource addToViewer( CachedCellImg< UnsignedShortType, ? > cachedCellImg, String cachedFilterImgName )
	{

		BdvSource bdvSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( cachedCellImg, multiPositionViewer.getLoadingQueue() ),
				cachedFilterImgName,
				BdvOptions.options().addTo( multiPositionViewer.getBdv() ) );

		// TODO: set color
//		bdvSource.setColor( settings.imagesSource.getArgbType() );

		// TODO: set lut
//		setLut();

		return bdvSource;

	}

	public static void setLut( BdvSource bdvSource, ImageFilterSettings settings )
	{
		final double[] lutMinMax = settings.imagesSource.getLutMinMax();

		if ( settings.filterType.equals( ImageFilter.SUBTRACT_MEDIAN ) )
		{
			bdvSource.setDisplayRange( 0, lutMinMax[ 1 ] - lutMinMax[ 0 ] );
		}
		else if ( settings.filterType.equals( ImageFilter.MAX_MINUS_MIN ) )
		{
			bdvSource.setDisplayRange( 0, lutMinMax[ 1 ] - lutMinMax[ 0 ] );
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
		frame = new JFrame( "Image Navigator" );
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