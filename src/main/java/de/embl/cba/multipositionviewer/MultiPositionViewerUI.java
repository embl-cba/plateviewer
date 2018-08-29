package de.embl.cba.multipositionviewer;

import net.imglib2.cache.img.CachedCellImg;

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
	JComboBox applyFilterImagesSourceComboBox;
	JComboBox applyFilterTypeComboBox;

	JCheckBox simpleSegmentationCheckBox;
	JCheckBox applyFilterCheckBox;
	JTextField simpleSegmentationMinimalObjectSizeTextField;
	JTextField simpleSegmentationThresholdTextField;
	JTextField applyFilterSizeTextField;
	JTextField applyFilterOffsetTextField;

	final MultiPositionViewer multiPositionViewer;

	private SimpleSegmentation simpleSegmentation;
	private ImageFilter imageFilter;
	private ArrayList< String > derivedImagesSources;


	public MultiPositionViewerUI( MultiPositionViewer multiPositionViewer )
	{

		this.multiPositionViewer = multiPositionViewer;

		this.derivedImagesSources = new ArrayList<>( );

		addImageNavigationComboBox( );

		addWellNavigationComboBox(  );

		addApplyFilterUI( );

		addSimpleSegmentationUI( );

		createAndShowUI( );
	}

	private void addApplyFilterUI( )
	{
		JPanel panel = createHorizontalLayoutPanel();

		addApplyFilterCheckBox( panel );

		addApplyFilterTypeComboBox( panel );

		addImagesSourceComboBox( panel );

		addApplyFilterSizeTextField( panel );

		addApplyFilterOffsetTextField( panel );

		add( panel );
	}

	private void addApplyFilterCheckBox( JPanel panel )
	{
		applyFilterCheckBox = new JCheckBox( "Background subtraction" );
		applyFilterCheckBox.addActionListener( this );
		panel.add( applyFilterCheckBox );
	}

	private void addSimpleSegmentationUI( )
	{
		JPanel panel = createHorizontalLayoutPanel();

		simpleSegmentationCheckBox = new JCheckBox( "Simple segmentation" );
		simpleSegmentationCheckBox.addActionListener( this );
		panel.add( simpleSegmentationCheckBox );

		addImagesSourceComboBox( panel );

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


	private void addApplyFilterSizeTextField( JPanel panel )
	{
		panel.add( new JLabel( "Radius [pixels]" ) );
		applyFilterSizeTextField = new JTextField( "5", 5 );
		applyFilterSizeTextField.addActionListener( this );
		panel.add( applyFilterSizeTextField );
	}

	private void addApplyFilterOffsetTextField( JPanel panel )
	{
		panel.add( new JLabel( "Offset" ) );
		applyFilterOffsetTextField = new JTextField( "0", 5 );
		applyFilterOffsetTextField.addActionListener( this );
		panel.add( applyFilterOffsetTextField );
	}

	private void addApplyFilterTypeComboBox( JPanel panel )
	{
		applyFilterTypeComboBox = new JComboBox();
		for( String filterType : ImageFilter.getFilterTypes() )
		{
			applyFilterTypeComboBox.addItem( filterType );
		}
		panel.add( applyFilterTypeComboBox );
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
						referenceImagesSource,
						cachedCellImg,
						selectedSourceName,
						Double.parseDouble( simpleSegmentationThresholdTextField.getText() ),
						Long.parseLong( simpleSegmentationMinimalObjectSizeTextField.getText() ),
						multiPositionViewer);

				derivedImagesSources.add( simpleSegmentation.getOutputName() );
			}
		}

		if ( e.getSource() == applyFilterCheckBox
				|| e.getSource() == applyFilterTypeComboBox
				|| e.getSource() == applyFilterSizeTextField
				|| e.getSource() == applyFilterOffsetTextField )
		{
			if ( imageFilter != null )
			{
				imageFilter.dispose();
				imageFilter = null;
			}

			if ( applyFilterCheckBox.isSelected() )
			{
				imageFilter = new ImageFilter(
						referenceImagesSource,
						cachedCellImg,
						selectedSourceName,
						(String) applyFilterTypeComboBox.getSelectedItem(),
						Integer.parseInt( applyFilterSizeTextField.getText() ),
						Double.parseDouble( applyFilterOffsetTextField.getText() ),
						multiPositionViewer );

				derivedImagesSources.add( imageFilter.getOutputName() );
			}
		}

		updateImagesSourcesComboBoxItems();
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