package de.embl.cba.plateviewer.view.panel;

import bdv.util.BdvStackSource;
import de.embl.cba.plateviewer.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;
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

    public void addSourceToPanel(
            String sourceName,
            BdvStackSource< ? > bdvStackSource,
            ARGBType argb,
            boolean initiallyVisible )
    {
        if( ! sourceNameToPanel.containsKey( sourceName ) )
        {
            JPanel panel = new JPanel();
            sourceNameToPanel.put( sourceName, panel );

            panel.setLayout( new BoxLayout(panel, BoxLayout.LINE_AXIS) );
            panel.setBorder( BorderFactory.createEmptyBorder(0, 10, 0, 10 ) );
            panel.add( Box.createHorizontalGlue() );
            panel.setOpaque( true );
            panel.setBackground( Utils.asColor( argb ) );

            JLabel jLabel = new JLabel( sourceName );
            jLabel.setHorizontalAlignment( SwingConstants.CENTER );

            int[] buttonDimensions = new int[]{ 50, 30 };

            final Object type = bdvStackSource.getSources().get( 0 ).getSpimSource().getType();

            panel.add( jLabel );

            if ( ! ( type instanceof VolatileARGBType ) )
            {
                final JButton colorButton =
                        createColorButton( panel, buttonDimensions, bdvStackSource );
                panel.add( colorButton );
            }

            JButton brightnessButton =
                    getBrightnessButton( sourceName, bdvStackSource, buttonDimensions, type );
            panel.add( brightnessButton );

            final JCheckBox visibilityCheckbox =
                    createVisibilityCheckbox( buttonDimensions, bdvStackSource, initiallyVisible );
            panel.add( visibilityCheckbox );

            add( panel );
            refreshUI();
        }
    }

    public JButton getBrightnessButton( String sourceName, BdvStackSource< ? > bdvStackSource, int[] buttonDimensions, Object type )
    {
        JButton brightnessButton;
        if ( type instanceof VolatileUnsignedShortType )
            brightnessButton = createBrightnessButton(
                    buttonDimensions,
                    sourceName,
                    bdvStackSource,
                    0,
                    65535);
        else if ( type instanceof VolatileUnsignedByteType )
            brightnessButton = createBrightnessButton(
                    buttonDimensions,
                    sourceName,
                    bdvStackSource,
                    0,
                    255 );
        else
            brightnessButton = createBrightnessButton(
                    buttonDimensions,
                    sourceName,
                    bdvStackSource,
                    0,
                    65535 );
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