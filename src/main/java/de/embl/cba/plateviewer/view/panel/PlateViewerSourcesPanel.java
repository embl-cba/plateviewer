package de.embl.cba.plateviewer.view.panel;

import bdv.util.BdvOverlaySource;
import bdv.util.BdvSource;
import bdv.util.BdvStackSource;
import bdv.util.BdvVirtualChannelSource;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.image.channel.BdvViewable;
import de.embl.cba.plateviewer.image.table.TableImage;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileFloatType;
import net.imglib2.type.volatiles.VolatileUnsignedByteType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.embl.cba.bdv.utils.BdvDialogs.*;

public class PlateViewerSourcesPanel < R extends RealType< R > & NativeType< R > > extends JPanel
{
    private final PlateViewerMainPanel< R > plateViewerMainPanel;
    public List< Color > colors;
    protected Map< String, JPanel > sourceNameToPanel;

    public PlateViewerSourcesPanel( PlateViewerMainPanel< R > plateViewerMainPanel )
    {
        this.plateViewerMainPanel = plateViewerMainPanel;

        this.setLayout( new BoxLayout(this, BoxLayout.Y_AXIS ) );
        this.setAlignmentX( Component.LEFT_ALIGNMENT );
        sourceNameToPanel = new LinkedHashMap<>(  );
        initDefaultColors();
    }

    public JPanel getPanel()
    {
        return this;
    }

    private void initDefaultColors()
    {
        colors = new ArrayList<>(  );

        colors.add( Color.BLUE );
        colors.add( Color.GREEN );
        colors.add( Color.YELLOW );
        colors.add( Color.MAGENTA );
        colors.add( Color.CYAN );
        colors.add( Color.ORANGE );
        colors.add( Color.PINK );

    }

    public void addToPanel( BdvViewable bdvViewable, BdvSource bdvSource )
    {
        if( ! sourceNameToPanel.containsKey( bdvViewable.getName() ) )
        {
            JPanel panel = new JPanel();
            sourceNameToPanel.put( bdvViewable.getName() , panel );

            panel.setLayout( new BoxLayout(panel, BoxLayout.LINE_AXIS) );
            panel.setBorder( BorderFactory.createEmptyBorder(0, 10, 0, 10 ) );
            panel.add( Box.createHorizontalGlue() );
            panel.setOpaque( true );
            panel.setBackground( Utils.asColor( bdvViewable.getColor() ) );

            JLabel jLabel = new JLabel( bdvViewable.getName() );
            jLabel.setHorizontalAlignment( SwingConstants.CENTER );

            int[] buttonDimensions = new int[]{ 50, 30 };

            panel.add( jLabel );

            if ( ! ( bdvSource instanceof BdvOverlaySource ) &&
                    ! ( bdvViewable.getType().equals( Metadata.Type.Segmentation )) &&
                    ! ( bdvViewable instanceof TableImage  ) )
            {
                final JButton colorButton = createColorButton( panel, buttonDimensions, bdvSource );
                panel.add( colorButton );
            }

            if ( bdvViewable instanceof TableImage )
            {
                final JButton colorButton = createColorByColumnButton( panel, buttonDimensions, bdvSource );
                panel.add( colorButton );
            }

            if ( bdvSource instanceof BdvStackSource )
            {
                JButton brightnessButton = getBrightnessButton(
                        bdvViewable.getName(), ( BdvStackSource ) bdvSource, buttonDimensions );
                panel.add( brightnessButton );
            }

            final JCheckBox visibilityCheckbox =
                    createVisibilityCheckbox( buttonDimensions, bdvSource, bdvViewable.isInitiallyVisible() );
            panel.add( visibilityCheckbox );

            add( panel );
            refreshUI();
        }
    }

    private JButton createColorByColumnButton( JPanel panel,
                                             int[] buttonDimensions,
                                             BdvSource bdvSource )
    {
        JButton colorButton;
        colorButton = new JButton( "C" );

        colorButton.setPreferredSize(
                new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

        colorButton.addActionListener( e -> {
            // TODO: think about who knows about what
            plateViewerMainPanel.getPlateViewerImageView().getTableView().showColorByColumnDialog();
        } );

        return colorButton;
    }

    public JButton getBrightnessButton(
            String sourceName,
            BdvStackSource< ? > bdvSource, // TODO: could this also be a bdvSource?
            int[] buttonDimensions )
    {
        JButton brightnessButton;

        final double displayRangeMax =
                bdvSource.getConverterSetups().get( 0 ).getDisplayRangeMax();

        brightnessButton = createBrightnessButton(
                buttonDimensions,
                sourceName,
                bdvSource,
                0,
                displayRangeMax * 5 ); // TODO: What makes sense here?

//        if ( type instanceof VolatileUnsignedShortType
//                || type instanceof UnsignedShortType )
//            brightnessButton = createBrightnessButton(
//                    buttonDimensions,
//                    sourceName,
//                    bdvStackSource,
//                    0,
//                    65535);
//        else if ( type instanceof VolatileUnsignedByteType
//                    || type instanceof UnsignedByteType )
//            brightnessButton = createBrightnessButton(
//                    buttonDimensions,
//                    sourceName,
//                    bdvStackSource,
//                    0,
//                    255 );
//        else if ( type instanceof VolatileFloatType
//                    || type instanceof FloatType )
//        {
//            final double displayRangeMin = bdvStackSource.getConverterSetups().get( 0 ).getDisplayRangeMin();
//            final double displayRangeMax = bdvStackSource.getConverterSetups().get( 0 ).getDisplayRangeMax();
//            brightnessButton = createBrightnessButton(
//                    buttonDimensions,
//                    sourceName,
//                    bdvStackSource,
//                    0,
//                    displayRangeMax * 5 );
//        }
//        else
//            brightnessButton = createBrightnessButton(
//                    buttonDimensions,
//                    sourceName,
//                    bdvStackSource,
//                    0,
//                    65535 );

        return brightnessButton;
    }

    private JButton createRemoveButton(
            String sourceName,
            BdvStackSource< R > bdvStackSource,
            int[] buttonDimensions )
    {
        JButton removeButton = new JButton( "X" );
        removeButton.setPreferredSize(
                new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

        removeButton.addActionListener(
                e -> removeSource(
                        sourceName,
                        bdvStackSource ) );

        return removeButton;
    }


    private void removeSource( String sourceName, BdvStackSource< R > source )
    {
        // remove from bdv
        plateViewerMainPanel.getBdv().getViewerPanel().removeSource(
                source.getSources().get( 0 ).getSpimSource() );

        // remove from this panel
        remove( sourceNameToPanel.get( sourceName ) );
        sourceNameToPanel.remove( sourceName );

        // remove from image list
        plateViewerMainPanel.removeSource( sourceName );

        refreshUI();
    }

    private void refreshUI()
    {
        SwingUtilities.invokeLater( () -> {
            this.revalidate();
            this.repaint();
        });
    }

}