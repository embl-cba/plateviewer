package de.embl.cba.multipositionviewer;

import javax.swing.*;
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

	private SimpleSegmentation simpleSegmentation;


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
						multiPositionViewer.getImagesSources().get( imagesSourcesComboBox.getSelectedIndex() ),
						Double.parseDouble( simpleSegmentationThresholdTextField.getText() ),
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