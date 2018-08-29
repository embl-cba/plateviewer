package de.embl.cba.gridviewer;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MultiPositionViewerUI < T extends NativeType< T > & RealType< T > > extends JPanel implements ActionListener
{
	JFrame frame;
	JComboBox imageNamesComboBox;
	JComboBox wellNamesComboBox;

	JComboBox imagesSourcesComboBox;
	JComboBox imageFiltersComboBox;

	final MultiPositionViewer multiPositionViewer;

	private ImageFilter imageFilter;

	private ArrayList< ImagesSource< T > > imagesSources;


	public MultiPositionViewerUI( MultiPositionViewer multiPositionViewer )
	{

		this.multiPositionViewer = multiPositionViewer;

		setImagesSources( );

		addSiteNavigationComboBox( );

		addWellNavigationComboBox(  );

		addImageFilterPanel();

		createAndShowUI( );
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


		if ( e.getSource() == imageFiltersComboBox )
		{

			final ImagesSource inputSource = imagesSources.get( imagesSourcesComboBox.getSelectedIndex() );

			ImageFilterSettings settings = new ImageFilterSettings();
			settings.filterType = (String) imageFiltersComboBox.getSelectedItem();
			settings.inputCachedCellImg = inputSource.getCachedCellImg();
			settings.inputName = ( String ) imagesSourcesComboBox.getSelectedItem();
			settings.multiPositionViewer = multiPositionViewer;

			final ImageFilter imageFilter = new ImageFilter( settings );
			final String name = imageFilter.getCachedFilterImgName();

			removeSourceOfSameNameIfExistsAlready( name );

			final CachedCellImg img = imageFilter.createCachedFilterImg();

			final BdvSource bdvSource = addToViewer( img, name );

			setLut( bdvSource, inputSource, settings.filterType );

			if ( ! settings.filterType.equals( ImageFilter.SIMPLE_SEGMENTATION ) )
			{
				inputSource.getBdvSource().setActive( false );
			}

			imagesSources.add( new ImagesSource( img, name, bdvSource ) );

			updateImagesSourcesComboBoxItems();
		}


	}

	public void removeSourceOfSameNameIfExistsAlready( String cachedFilterImgName )
	{
		for ( ImagesSource source : imagesSources )
		{
			if ( source.getName().equals( cachedFilterImgName ) )
			{
				source.dispose();
				imagesSources.remove( source );
				source = null;
				System.gc();
				break;
			}
		}
	}


	private BdvSource addToViewer( CachedCellImg< UnsignedShortType, ? > cachedCellImg, String cachedFilterImgName )
	{

		BdvSource bdvSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( cachedCellImg, multiPositionViewer.getLoadingQueue() ),
				cachedFilterImgName,
				BdvOptions.options().addTo( multiPositionViewer.getBdv() ) );

		// TODO: set color
//		bdvSource.setColor( settings.baseImagesSource.getArgbType() );

		// TODO: set lut


		return bdvSource;

	}

	public static void setLut( BdvSource bdvSource, ImagesSource imagesSource, String filterType )
	{
		final double[] lutMinMax = imagesSource.getLutMinMax();

		if ( filterType.equals( ImageFilter.SUBTRACT_MEDIAN ) )
		{
			bdvSource.setDisplayRange( 0, lutMinMax[ 1 ] - lutMinMax[ 0 ] );
		}
		else if ( filterType.equals( ImageFilter.MAX_MINUS_MIN ) )
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