package de.embl.cba.plateviewer.ui;

import bdv.util.BdvStackSource;
import de.embl.cba.plateviewer.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.embl.cba.bdv.utils.BdvUserInterfaceUtils.*;

public class PlateViewerSourcesPanel < R extends RealType< R > & NativeType< R > > extends JPanel
{
    private final PlateViewerUI< R > plateViewerUI;
    public List< Color > colors;
    protected Map< String, JPanel > sourceNameToPanel;

    public PlateViewerSourcesPanel( PlateViewerUI< R > plateViewerUI )
    {
        this.plateViewerUI = plateViewerUI;

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
            BdvStackSource bdvStackSource,
            ARGBType argb )
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

            final JButton colorButton =
                    createColorButton( panel, buttonDimensions, bdvStackSource );
            final JButton brightnessButton =
                    createBrightnessButton( buttonDimensions, sourceName, bdvStackSource );
            final JButton removeButton =
                    createRemoveButton( sourceName, bdvStackSource, buttonDimensions );
            final JCheckBox visibilityCheckbox =
                    createVisibilityCheckbox( buttonDimensions, bdvStackSource, true );

            panel.add( jLabel );
            panel.add( colorButton );
            panel.add( brightnessButton );
            panel.add( removeButton );
            panel.add( visibilityCheckbox );

            add( panel );
            refreshUI();
        }
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
        plateViewerUI.getBdv().getViewerPanel().removeSource(
                source.getSources().get( 0 ).getSpimSource() );

        // remove from this panel
        remove( sourceNameToPanel.get( sourceName ) );
        sourceNameToPanel.remove( sourceName );

        // remove from source list
        plateViewerUI.removeSource( sourceName );

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