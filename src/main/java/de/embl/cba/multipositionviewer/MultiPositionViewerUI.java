package de.embl.cba.multipositionviewer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MultiPositionViewerUI extends JPanel implements ActionListener
{
	JFrame frame;
	JComboBox imageNamesComboBox;
	JComboBox imagesSourceSimpleSegmentationComboBox;
	JComboBox imagesSourceBackgroundRemovalComboBox;
	JCheckBox simpleSegmentationCheckBox;
	JCheckBox backgroundRemovalCheckBox;
	JTextField simpleSegmentationMinimalObjectSizeTextField;
	JTextField simpleSegmentationThresholdTextField;
	JTextField backgroundRemovalSizeTextField;


	final ArrayList< String > imageNames;
	final MultiPositionViewer multiPositionViewer;

	private SimpleSegmentation simpleSegmentation;


	public MultiPositionViewerUI( ArrayList< String > imageNames, MultiPositionViewer multiPositionViewer )
	{
		this.imageNames = imageNames;
		this.multiPositionViewer = multiPositionViewer;

		addImageNamesComboBox( );

		addSimpleSegmentationUI( );

		addBackgroundRemovalUI( );

		createAndShowUI( );
	}

	private void addBackgroundRemovalUI( )
	{
		JPanel panel = createHorizontalLayoutPanel();

		addBackgroundRemovalCheckBox( panel );

		addBackgroundRemovalImagesSourceComboBox( panel );

		addBackgroundRemovalSizeTextField( panel );

		add( panel );
	}

	private void addBackgroundRemovalCheckBox( JPanel panel )
	{
		backgroundRemovalCheckBox = new JCheckBox( "Background subtraction" );
		backgroundRemovalCheckBox.addActionListener( this );
		panel.add( backgroundRemovalCheckBox );
	}

	private void addSimpleSegmentationUI( )
	{
		JPanel panel = createHorizontalLayoutPanel();

		simpleSegmentationCheckBox = new JCheckBox( "Simple segmentation" );
		simpleSegmentationCheckBox.addActionListener( this );
		panel.add( simpleSegmentationCheckBox );

		addImagesSourceSimpleSegmentationComboBox( panel );

		addThresholdTextField( panel );

		addMinimalObjectSizeTextField( panel );

		add( panel );
	}

	private JPanel createHorizontalLayoutPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout(panel, BoxLayout.LINE_AXIS) );
		panel.setBorder( BorderFactory.createEmptyBorder(0, 10, 10, 10) );
		panel.add( Box.createHorizontalGlue() );
		return panel;
	}

	private void addMinimalObjectSizeTextField( JPanel panel )
	{
		panel.add( new JLabel( "Minimal radius [pixels]" ) );
		simpleSegmentationMinimalObjectSizeTextField = new JTextField( "100", 6 );
		simpleSegmentationMinimalObjectSizeTextField.addActionListener( this );
		panel.add( simpleSegmentationMinimalObjectSizeTextField );
	}

	private void addThresholdTextField( JPanel panel )
	{
		panel.add( new JLabel( "Threshold" ) );
		simpleSegmentationThresholdTextField = new JTextField( "1.0", 5 );
		simpleSegmentationThresholdTextField.addActionListener( this );
		panel.add( backgroundRemovalSizeTextField );
	}


	private void addBackgroundRemovalSizeTextField( JPanel panel )
	{
		panel.add( new JLabel( "Radius [pixels]" ) );
		backgroundRemovalSizeTextField = new JTextField( "5", 5 );
		backgroundRemovalSizeTextField.addActionListener( this );
		panel.add( backgroundRemovalSizeTextField );
	}

	private void addBackgroundRemovalImagesSourceComboBox( JPanel panel )
	{
		imagesSourceBackgroundRemovalComboBox = new JComboBox();
		for( ImagesSource source : multiPositionViewer.getImagesSources() )
		{
			imagesSourceBackgroundRemovalComboBox.addItem( source.getName() );
		}
		panel.add( imagesSourceBackgroundRemovalComboBox );
	}


	private void addImagesSourceSimpleSegmentationComboBox( JPanel panel )
	{
		imagesSourceSimpleSegmentationComboBox = new JComboBox();
		for( ImagesSource source : multiPositionViewer.getImagesSources() )
		{
			imagesSourceSimpleSegmentationComboBox.addItem( source.getName() );
		}
		panel.add( imagesSourceSimpleSegmentationComboBox );
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

		if ( e.getSource() == simpleSegmentationCheckBox
				|| e.getSource() == simpleSegmentationThresholdTextField
				|| e.getSource() == simpleSegmentationMinimalObjectSizeTextField )
		{

			if ( simpleSegmentation != null )
			{
				simpleSegmentation.dispose();
				simpleSegmentation = null;
			}

			if ( simpleSegmentationCheckBox.isSelected() )
			{
				simpleSegmentation = new SimpleSegmentation(
						multiPositionViewer.getImagesSources().get( imagesSourceSimpleSegmentationComboBox.getSelectedIndex() ),
						Double.parseDouble( simpleSegmentationThresholdTextField.getText() ),
						Long.parseLong( simpleSegmentationMinimalObjectSizeTextField.getText() ),
						multiPositionViewer );


			}

		}


		if ( e.getSource() == backgroundRemovalCheckBox
				|| e.getSource() == backgroundRemovalSizeTextField )
		{

			if ( simpleSegmentation != null )
			{
				simpleSegmentation.dispose();
				simpleSegmentation = null;
			}

			if ( simpleSegmentationCheckBox.isSelected() )
			{
				simpleSegmentation = new SimpleSegmentation(
						multiPositionViewer.getImagesSources().get( imagesSourceSimpleSegmentationComboBox.getSelectedIndex() ),
						Double.parseDouble( backgroundRemovalSizeTextField.getText() ),
						Long.parseLong( simpleSegmentationMinimalObjectSizeTextField.getText() ),
						multiPositionViewer );


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