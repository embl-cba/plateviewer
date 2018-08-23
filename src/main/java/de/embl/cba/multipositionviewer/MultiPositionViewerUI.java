package de.embl.cba.multipositionviewer;

import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MultiPositionViewerUI extends JPanel implements ActionListener
{
	JFrame frame;
	JComboBox imageNamesComboBox;
	JComboBox imagesSourcesComboBox;
	JComboBox actionComboBox;

	final ArrayList< String > imageNames;
	final MultiPositionViewer multiPositionViewer;

	private static final String CONNECTED_COMPONENTS_ACTION = "Compute connected components";


	public MultiPositionViewerUI( ArrayList< String > imageNames, MultiPositionViewer multiPositionViewer )
	{
		this.imageNames = imageNames;
		this.multiPositionViewer = multiPositionViewer;

		addImageNamesComboBox( );

		addImageSourcesComboBox( );

		addActionComboBox( );

		createAndShowUI( );
	}

	private void addActionComboBox( )
	{
		actionComboBox = new JComboBox();
		actionComboBox.addItem( CONNECTED_COMPONENTS_ACTION );
		actionComboBox.addActionListener( this );
		add( actionComboBox );
	}

	private void addImageSourcesComboBox( )
	{
		imagesSourcesComboBox = new JComboBox();
		for( ImagesSource source : multiPositionViewer.getImagesSources() )
		{
			imagesSourcesComboBox.addItem( source.getName() );
		}
		imagesSourcesComboBox.addActionListener( this );
		add( imagesSourcesComboBox );
	}

	public void addImageNamesComboBox( )
	{
		imageNamesComboBox = new JComboBox();
		for ( String imageName : imageNames )
		{
			imageNamesComboBox.addItem( imageName );
		}
		imageNamesComboBox.addActionListener( this );
		add( imageNamesComboBox );
	}

	@Override
	public void actionPerformed( ActionEvent e )
	{
		if ( e.getSource() == imageNamesComboBox )
		{
			final String imageName = ( String ) imageNamesComboBox.getSelectedItem();
			multiPositionViewer.zoomToImage( imageName );
		}

		if ( e.getSource() == actionComboBox )
		{
			final int imagesSourceIndex = imagesSourcesComboBox.getSelectedIndex();
			final ImagesSource imagesSource = multiPositionViewer.getImagesSources().get( imagesSourceIndex );

			if ( actionComboBox.getSelectedItem().equals( CONNECTED_COMPONENTS_ACTION ) )
			{
				final CachedCellImg cachedCellImg = imagesSource.getCachedCellImg();

				double realThreshold = 1.0;

				final ThresholdLoader thresholdLoader = new ThresholdLoader( cachedCellImg, realThreshold );

				int[] cellDimensions = new int[ cachedCellImg.getCellGrid().numDimensions() ];
				cachedCellImg.getCellGrid().cellDimensions( cellDimensions );

				final long[] imgDimensions = cachedCellImg.getCellGrid().getImgDimensions();

				final CachedCellImg< BitType, ? > thresholdImg
						= new ReadOnlyCachedCellImgFactory().create(
						imgDimensions,
						new UnsignedByteType(),
						new ThresholdLoader( cachedCellImg, realThreshold ),
						ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions )
				);

				// TODO: add thresholdImg to Bdv (or multiPositionViewer?)xuu

			}
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