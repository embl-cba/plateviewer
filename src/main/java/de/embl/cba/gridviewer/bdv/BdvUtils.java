package de.embl.cba.gridviewer.bdv;

import bdv.cache.CacheControl;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.Bdv;
import bdv.util.BoundedValueDouble;
import bdv.util.Prefs;
import bdv.viewer.Interpolation;
import bdv.viewer.ViewerPanel;
import bdv.viewer.VisibilityAndGrouping;
import bdv.viewer.overlay.ScaleBarOverlayRenderer;
import bdv.viewer.render.MultiResolutionRenderer;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.Duplicator;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.*;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.RenderTarget;
import net.imglib2.view.Views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;

public abstract class BdvUtils
{

	public static final String OVERLAY = "overlay";

	public static BufferedImage captureView( Bdv bdv, int size )
	{
		int width = size;
		int height = size;

		final ViewerPanel viewer = bdv.getBdvHandle().getViewerPanel();
		final ViewerState renderState = viewer.getState();
		final int canvasW = viewer.getDisplay().getWidth();
		final int canvasH = viewer.getDisplay().getHeight();

		final AffineTransform3D affine = new AffineTransform3D();
		renderState.getViewerTransform( affine );
		affine.set( affine.get( 0, 3 ) - canvasW / 2, 0, 3 );
		affine.set( affine.get( 1, 3 ) - canvasH / 2, 1, 3 );
		affine.scale( ( double ) width / canvasW );
		affine.set( affine.get( 0, 3 ) + width / 2, 0, 3 );
		affine.set( affine.get( 1, 3 ) + height / 2, 1, 3 );
		renderState.setViewerTransform( affine );

		final ScaleBarOverlayRenderer scalebar = Prefs.showScaleBarInMovie() ? new ScaleBarOverlayRenderer() : null;

		class MyTarget implements RenderTarget
		{
			BufferedImage bi;

			@Override
			public BufferedImage setBufferedImage( final BufferedImage bufferedImage )
			{
				bi = bufferedImage;
				return null;
			}

			@Override
			public int getWidth()
			{
				return width;
			}

			@Override
			public int getHeight()
			{
				return height;
			}
		}
		final MyTarget target = new MyTarget();
		final MultiResolutionRenderer renderer = new MultiResolutionRenderer(
				target, new PainterThread( null ), new double[] { 1 }, 0, false, 1, null, false,
				viewer.getOptionValues().getAccumulateProjectorFactory(), new CacheControl.Dummy() );


		int timepoint = 0;
		renderState.setCurrentTimepoint( timepoint );
		renderer.requestRepaint();
		renderer.paint( renderState );

		if ( Prefs.showScaleBarInMovie() )
		{
			final Graphics2D g2 = target.bi.createGraphics();
			g2.setClip( 0, 0, width, height );
			scalebar.setViewerState( renderState );
			scalebar.paint( g2 );
		}

		return target.bi;

	}

	public static int getSourceId( Bdv bdv, String sourceName )
	{
		final List< SourceState< ? > > sources = bdv.getBdvHandle().getViewerPanel().getState().getSources();

		int sourceId = -1;
		for ( int i = 0; i < sources.size(); ++i )
		{
			if ( sources.get( i ).getSpimSource().getName().equals( sourceName ) )
			{
				sourceId = i;
			}
		}
		return sourceId;
	}

	public static String getName( Bdv bdv, int sourceId )
	{
		return bdv.getBdvHandle().getViewerPanel().getState().getSources().get( sourceId ).getSpimSource().getName();
	}


	public static VoxelDimensions getVoxelDimensions( Bdv bdv, int sourceId )
	{
		return bdv.getBdvHandle().getViewerPanel().getState().getSources().get( sourceId ).getSpimSource().getVoxelDimensions();
	}


	public static AffineTransform3D getSourceTransform( Bdv bdv, int sourceId )
	{
		final AffineTransform3D sourceTransform = new AffineTransform3D();
		bdv.getBdvHandle().getViewerPanel().getState().getSources().get( sourceId ).getSpimSource().getSourceTransform( 0, 0, sourceTransform );
		return sourceTransform;
	}


	public static RandomAccessibleInterval< ? > getRandomAccessibleInterval( Bdv bdv, int sourceId )
	{
		return bdv.getBdvHandle().getViewerPanel().getState().getSources().get( sourceId ).getSpimSource().getSource( 0, 0 );
	}

	public static RealRandomAccessible< ? > getRealRandomAccessible( Bdv bdv, int sourceId )
	{
		return bdv.getBdvHandle().getViewerPanel().getState().getSources().get( sourceId ).getSpimSource().getInterpolatedSource( 0, 0, Interpolation.NLINEAR );
	}

	public static void zoomToInterval( Bdv bdv, FinalInterval interval, double zoomFactor )
	{
		final AffineTransform3D affineTransform3D = getImageZoomTransform( bdv, interval, zoomFactor );

		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( affineTransform3D );
	}

	public static AffineTransform3D getImageZoomTransform( Bdv bdv, FinalInterval interval, double zoomFactor )
	{

		final AffineTransform3D affineTransform3D = new AffineTransform3D();

		double[] shiftToImage = new double[ 3 ];

		for ( int d = 0; d < 3; ++d )
		{
			shiftToImage[ d ] = -( interval.min( d ) + interval.dimension( d ) / 2.0 );
		}

		affineTransform3D.translate( shiftToImage );

		int[] bdvWindowDimensions = new int[ 2 ];
		bdvWindowDimensions[ 0 ] = bdv.getBdvHandle().getViewerPanel().getWidth();
		bdvWindowDimensions[ 1 ] = bdv.getBdvHandle().getViewerPanel().getHeight();

		affineTransform3D.scale( zoomFactor * bdvWindowDimensions[ 0 ] / interval.dimension( 0 ) );

		double[] shiftToBdvWindowCenter = new double[ 3 ];

		for ( int d = 0; d < 2; ++d )
		{
			shiftToBdvWindowCenter[ d ] += bdvWindowDimensions[ d ] / 2.0;
		}

		affineTransform3D.translate( shiftToBdvWindowCenter );

		return affineTransform3D;
	}


	public static void addSourcesDisplaySettingsUI( JPanel panel,
													String name,
													Bdv bdv,
													ArrayList< Integer > sourceIndexes,
													Color color )
	{
		int[] buttonDimensions = new int[]{ 50, 30 };

		JPanel channelPanel = new JPanel();
		channelPanel.setLayout( new BoxLayout( channelPanel, BoxLayout.LINE_AXIS ) );
		channelPanel.setBorder( BorderFactory.createEmptyBorder(0,10,0,10) );
		channelPanel.add( Box.createHorizontalGlue() );
		channelPanel.setOpaque( true );
		channelPanel.setBackground( color );

		JLabel jLabel = new JLabel( name );
		jLabel.setHorizontalAlignment( SwingConstants.CENTER );

		channelPanel.add( jLabel );
		channelPanel.add( createColorButton( channelPanel, buttonDimensions, bdv, sourceIndexes ) );
		channelPanel.add( createBrightnessButton( buttonDimensions,  name, bdv, sourceIndexes ) );
		channelPanel.add( createToggleButton( buttonDimensions,  bdv, sourceIndexes ) );

		panel.add( channelPanel );

	}

	public static ARGBType asArgbType( Color color )
	{
		return new ARGBType( ARGBType.rgba( color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() ) );
	}

	public static JButton createColorButton( JPanel panel,
											 int[] buttonDimensions,
											 Bdv bdv,
											 ArrayList< Integer > sourceIndices )
	{
		JButton colorButton = new JButton( "C" );
		colorButton.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

		colorButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				Color color = JColorChooser.showDialog( null, "", null );

				for ( int i : sourceIndices )
				{
					bdv.getBdvHandle().getSetupAssignments().getConverterSetups().get( i ).setColor( asArgbType( color ) );
				}

				panel.setBackground( color );
			}
		} );


		return colorButton;
	}

	public static JButton createBrightnessButton( int[] buttonDimensions,
												  String name, Bdv bdv,
												  ArrayList< Integer > sourceIndices )
	{
		JButton button = new JButton( "B" );
		button.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

		button.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();

				for ( int i : sourceIndices )
				{
					converterSetups.add( bdv.getBdvHandle().getSetupAssignments().getConverterSetups().get( i ) );
				}

				showBrightnessDialog( name, converterSetups );
			}
		} );

		return button;
	}


	public static void showBrightnessDialog( String name, ArrayList< ConverterSetup > converterSetups )
	{

		JFrame frame = new JFrame( name );

		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		final BoundedValueDouble min = new BoundedValueDouble( 0, 65535, ( int ) converterSetups.get(0).getDisplayRangeMin() );
		final BoundedValueDouble max = new BoundedValueDouble( 0, 65535, ( int ) converterSetups.get(0).getDisplayRangeMax() );

		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
		final SliderPanelDouble minSlider = new SliderPanelDouble( "Min", min, 1 );
		final SliderPanelDouble maxSlider = new SliderPanelDouble( "Max", max, 1 );

		final BrightnessUpdateListener brightnessUpdateListener =
				new BrightnessUpdateListener( min, max, converterSetups );

		min.setUpdateListener( brightnessUpdateListener );
		max.setUpdateListener( brightnessUpdateListener );

		panel.add( minSlider );
		panel.add( maxSlider );

		frame.setContentPane( panel );

		//Display the window.
		frame.setBounds( MouseInfo.getPointerInfo().getLocation().x,
				MouseInfo.getPointerInfo().getLocation().y,
				120, 10);
		frame.pack();
		frame.setVisible( true );

	}

	public static void showGenericBrightnessDialog( ConverterSetup converterSetup )
	{
		GenericDialog gd = new GenericDialog( "LUT max value" );
		gd.addNumericField( "LUT max value: ", converterSetup.getDisplayRangeMax(), 0 );
		gd.showDialog();
		converterSetup.setDisplayRange( converterSetup.getDisplayRangeMin(), ( int ) gd.getNextNumber() );
	}

	public static JButton createToggleButton( int[] buttonDimensions,
											  Bdv bdv,
											  ArrayList< Integer > sourceIndices )
	{
		JButton button = new JButton( "T" );
		button.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

		button.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				final VisibilityAndGrouping visibilityAndGrouping = bdv.getBdvHandle().getViewerPanel().getVisibilityAndGrouping();
				for ( int i : sourceIndices )
				{
					visibilityAndGrouping.setSourceActive( i, !visibilityAndGrouping.isSourceActive( i ) );
				}
			}
		} );

		return button;
	}



	public static < T extends RealType< T > & NativeType< T > > void showAsIJ1MultiColorImage( Bdv bdv, double resolution, ArrayList< RandomAccessibleInterval< T > > randomAccessibleIntervals )
	{
		final ImagePlus imp = ImageJFunctions.wrap( Views.stack( randomAccessibleIntervals ), "capture" );
		final ImagePlus dup = new Duplicator().run( imp ); // otherwise it is virtual and cannot be modified
		IJ.run( dup, "Subtract...", "value=32768 slice");
		VoxelDimensions voxelDimensions = getVoxelDimensions( bdv, 0 );
		IJ.run( dup, "Properties...", "channels="+randomAccessibleIntervals.size()+" slices=1 frames=1 unit="+voxelDimensions.unit()+" pixel_width="+resolution+" pixel_height="+resolution+" voxel_depth=1.0");
		final CompositeImage compositeImage = new CompositeImage( dup );
		for ( int channel = 1; channel <= compositeImage.getNChannels(); ++channel )
		{
			compositeImage.setC( channel );
			switch ( channel )
			{
				case 1: // tomogram
					compositeImage.setChannelLut( compositeImage.createLutFromColor( Color.GRAY ) );
					compositeImage.setDisplayRange( 0, 1000 ); // TODO: get from bdv
					break;
				case 2: compositeImage.setChannelLut( compositeImage.createLutFromColor( Color.GRAY ) ); break;
				case 3: compositeImage.setChannelLut( compositeImage.createLutFromColor( Color.RED ) ); break;
				case 4: compositeImage.setChannelLut( compositeImage.createLutFromColor( Color.GREEN ) ); break;
				default: compositeImage.setChannelLut( compositeImage.createLutFromColor( Color.BLUE ) ); break;
			}
		}
		compositeImage.show();
		compositeImage.setTitle( "capture" );
		IJ.run(compositeImage, "Make Composite", "");
	}

	public static void addDisplaySettingsUI( Bdv bdv, JPanel panel )
	{
		final List< ConverterSetup > converterSetups = bdv.getBdvHandle().getSetupAssignments().getConverterSetups();
		final List< SourceState< ? > > sources = bdv.getBdvHandle().getViewerPanel().getState().getSources();
		final List< Integer > nonOverlaySourceIndices = getNonOverlaySourceIndices( bdv, sources );
		ArrayList< Color > defaultColors = getColors( nonOverlaySourceIndices );

		for ( int sourceIndex : nonOverlaySourceIndices )
		{
			final Color color = defaultColors.get( sourceIndex );

			converterSetups.get( sourceIndex ).setColor( asArgbType( color ) );

			final ArrayList< Integer > indices = new ArrayList<>( );
			indices.add( sourceIndex );

			String name = getName( bdv, sourceIndex );

			addSourcesDisplaySettingsUI( panel, name, bdv, indices, color );
		}

	}

	public static ArrayList< Color > getColors( List< Integer > nonOverlaySources )
	{
		ArrayList< Color > defaultColors = new ArrayList<>(  );
		if ( nonOverlaySources.size() > 1 )
		{
			defaultColors.add( Color.BLUE );
			defaultColors.add( Color.GREEN );
			defaultColors.add( Color.RED );
			defaultColors.add( Color.MAGENTA );
			defaultColors.add( Color.GRAY );
			defaultColors.add( Color.GRAY );
			defaultColors.add( Color.GRAY );
			defaultColors.add( Color.GRAY );
			defaultColors.add( Color.GRAY );
		}
		else
		{
			defaultColors.add( Color.GRAY );
		}
		return defaultColors;
	}

	public static List< Integer > getNonOverlaySourceIndices( Bdv bdv, List< SourceState< ? > > sources )
	{
		final List< Integer > nonOverlaySources = new ArrayList<>(  );

		for ( int sourceIndex = 0; sourceIndex < sources.size(); ++sourceIndex )
		{
			String name = getName( bdv, sourceIndex );
			if ( ! name.contains( OVERLAY ) )
			{
				nonOverlaySources.add( sourceIndex );
			}
		}

		return nonOverlaySources;
	}
}
