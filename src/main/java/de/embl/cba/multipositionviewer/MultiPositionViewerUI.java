package de.embl.cba.multipositionviewer;

import ij.IJ;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MultiPositionViewerUI extends JPanel implements ActionListener
{
	JFrame frame;
	JComboBox imageNamesComboBox;
	JComboBox wellNamesComboBox;
	JComboBox imagesSourceSimpleSegmentationComboBox;
	JComboBox backgroundRemovalImagesSourceComboBox;
	JCheckBox simpleSegmentationCheckBox;
	JCheckBox backgroundRemovalCheckBox;
	JTextField simpleSegmentationMinimalObjectSizeTextField;
	JTextField simpleSegmentationThresholdTextField;
	JTextField backgroundRemovalSizeTextField;
	JTextField backgroundRemovalOffsetTextField;

	final MultiPositionViewer multiPositionViewer;

	private SimpleSegmentation simpleSegmentation;
	private BackgroundRemoval backgroundRemoval;


	public MultiPositionViewerUI( MultiPositionViewer multiPositionViewer )
	{

		this.multiPositionViewer = multiPositionViewer;

		addImageNavigationComboBox( );

		addWellNavigationComboBox(  );

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

		addBackgroundRemovalOffsetTextField( panel );

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
		panel.add( simpleSegmentationThresholdTextField );
	}


	private void addBackgroundRemovalSizeTextField( JPanel panel )
	{
		panel.add( new JLabel( "Radius [pixels]" ) );
		backgroundRemovalSizeTextField = new JTextField( "5", 5 );
		backgroundRemovalSizeTextField.addActionListener( this );
		panel.add( backgroundRemovalSizeTextField );
	}

	private void addBackgroundRemovalOffsetTextField( JPanel panel )
	{
		panel.add( new JLabel( "Offset" ) );
		backgroundRemovalOffsetTextField = new JTextField( "0", 5 );
		backgroundRemovalOffsetTextField.addActionListener( this );
		panel.add( backgroundRemovalOffsetTextField );
	}

	private void addBackgroundRemovalImagesSourceComboBox( JPanel panel )
	{
		backgroundRemovalImagesSourceComboBox = new JComboBox();
		for( ImagesSource source : multiPositionViewer.getImagesSources() )
		{
			backgroundRemovalImagesSourceComboBox.addItem( source.getName() );
		}
		panel.add( backgroundRemovalImagesSourceComboBox );
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
				|| e.getSource() == backgroundRemovalSizeTextField
				|| e.getSource() == backgroundRemovalOffsetTextField )
		{
			if ( backgroundRemoval != null )
			{
				backgroundRemoval.dispose();
				backgroundRemoval = null;
			}

			if ( backgroundRemovalCheckBox.isSelected() )
			{
				backgroundRemoval = new BackgroundRemoval(
						multiPositionViewer.getImagesSources().get( backgroundRemovalImagesSourceComboBox.getSelectedIndex() ),
						Integer.parseInt( backgroundRemovalSizeTextField.getText() ),
						Double.parseDouble( backgroundRemovalOffsetTextField.getText() ),
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