package de.embl.cba.multipositionviewer;

import bdv.util.*;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.Volatile;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MultiPositionViewerUI extends JPanel implements ActionListener
{
	JFrame frame;
	JComboBox imageNamesComboBox;
	JComboBox imagesSourcesComboBox;
	JCheckBox simpleSegmentationCheckBox;
	JTextField simpleSegmentationMinimalObjectSizeTextField;
	JTextField simpleSegmentationThresholdTextField;

	final ArrayList< String > imageNames;
	final MultiPositionViewer multiPositionViewer;

	private BdvSource bdvSimpleSegmentationSource;


	public MultiPositionViewerUI( ArrayList< String > imageNames, MultiPositionViewer multiPositionViewer )
	{
		this.imageNames = imageNames;
		this.multiPositionViewer = multiPositionViewer;

		addImageNamesComboBox( );

		addSimpleSegmentationUI( );

		createAndShowUI( );
	}

	private void addSimpleSegmentationUI( )
	{
		JPanel panel = createSimpleSegmentationPanel();

		addSimpleSegmentationCheckBox( panel );

		addImageSourcesComboBox( panel );

		addThresholdTextField( panel );

		addMinimalObjectSizeTextField( panel );

		add( panel );
	}

	private JPanel createSimpleSegmentationPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout(panel, BoxLayout.LINE_AXIS) );
		panel.setBorder( BorderFactory.createEmptyBorder(0, 10, 10, 10) );
		panel.add( Box.createHorizontalGlue() );
		return panel;
	}

	private void addMinimalObjectSizeTextField( JPanel panel )
	{
		panel.add( new JLabel( "Minimal size [pixels]" ) );
		simpleSegmentationMinimalObjectSizeTextField = new JTextField( "100", 6 );
		simpleSegmentationMinimalObjectSizeTextField.addActionListener( this );
		panel.add( simpleSegmentationMinimalObjectSizeTextField );
	}

	private void addThresholdTextField( JPanel panel )
	{
		panel.add( new JLabel( "Threshold" ) );
		simpleSegmentationThresholdTextField = new JTextField( "1.0", 5 );
		simpleSegmentationThresholdTextField.addActionListener( this );
//		simpleSegmentationMinimalObjectSizeTextField.setHorizontalAlignment( SwingConstants.CENTER );
		panel.add( simpleSegmentationThresholdTextField );
	}

	private void addSimpleSegmentationCheckBox( JPanel panel )
	{
		simpleSegmentationCheckBox = new JCheckBox( "Simple segmentation" );
//		simpleSegmentationCheckBox.setHorizontalAlignment( SwingConstants.CENTER );
		simpleSegmentationCheckBox.addActionListener( this );
		panel.add( simpleSegmentationCheckBox );
	}

	private void addImageSourcesComboBox( JPanel panel )
	{
		imagesSourcesComboBox = new JComboBox();

		for( ImagesSource source : multiPositionViewer.getImagesSources() )
		{
			imagesSourcesComboBox.addItem( source.getName() );
		}
		panel.add( imagesSourcesComboBox );
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

		// TODO: refresh segmentation upon object size or threshold change

		if ( e.getSource() == simpleSegmentationCheckBox )
		{
			if ( simpleSegmentationCheckBox.isSelected() )
			{
				final ImagesSource imagesSource = multiPositionViewer.getImagesSources().get( imagesSourcesComboBox.getSelectedIndex() );

				final CachedCellImg< UnsignedByteType, ? > segmentationImg = createCachedSegmentationImg( imagesSource );

				bdvSimpleSegmentationSource = addCachedSegmentationImgToViewer( segmentationImg, multiPositionViewer );
			}
			else
			{
				bdvSimpleSegmentationSource.removeFromBdv();
				bdvSimpleSegmentationSource = null;
			}
		}

	}

	public static BdvSource addCachedSegmentationImgToViewer( CachedCellImg< UnsignedByteType, ? > thresholdImg, MultiPositionViewer multiPositionViewer )
	{
		Bdv bdv = multiPositionViewer.getBdv();
		SharedQueue loadingQueue = multiPositionViewer.getLoadingQueue();

		final BdvStackSource< Volatile< UnsignedByteType > > source = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( thresholdImg, loadingQueue ),
				"",
				BdvOptions.options().addTo( bdv ) );

		source.setColor( new ARGBType( ARGBType.rgba( 0, 255,0,127 )));

		return source;
	}

	public CachedCellImg< UnsignedByteType, ? > createCachedSegmentationImg( ImagesSource imagesSource )
	{
		final CachedCellImg cachedCellImg = imagesSource.getCachedCellImg();

		int[] cellDimensions = new int[ cachedCellImg.getCellGrid().numDimensions() ];
		cachedCellImg.getCellGrid().cellDimensions( cellDimensions );

		final long[] imgDimensions = cachedCellImg.getCellGrid().getImgDimensions();

		final CellLoader< UnsignedByteType > loader = new SimpleSegmentationLoader(
				imagesSource,
				Double.parseDouble( simpleSegmentationThresholdTextField.getText() ),
				Long.parseLong( simpleSegmentationMinimalObjectSizeTextField.getText() ),
				multiPositionViewer.getBdv() );

		return new ReadOnlyCachedCellImgFactory().create(
				imgDimensions,
				new UnsignedByteType(),
				loader,
				ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions ) );
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