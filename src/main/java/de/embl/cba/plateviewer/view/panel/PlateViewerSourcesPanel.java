package de.embl.cba.plateviewer.view.panel;

import bdv.util.BdvOverlaySource;
import bdv.util.BdvSource;
import bdv.util.BdvStackSource;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.image.channel.BdvViewable;
import de.embl.cba.plateviewer.image.table.TableRowsIntervalImage;
import de.embl.cba.plateviewer.table.DefaultAnnotatedIntervalTableRow;
import de.embl.cba.tables.view.TableRowsTableView;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.embl.cba.bdv.utils.BdvDialogs.*;

public class PlateViewerSourcesPanel < R extends RealType< R > & NativeType< R > > extends JPanel
{
    private final PlateViewerMainPanel< R > mainPanel;
    public List< Color > colors;
    protected Map< String, JPanel > sourceNameToPanel;

    public PlateViewerSourcesPanel( PlateViewerMainPanel< R > mainPanel )
    {
        this.mainPanel = mainPanel;

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
        final String channelName = bdvViewable.getName();

        if( ! sourceNameToPanel.containsKey( channelName ) )
        {
            JPanel panel = new JPanel();
            sourceNameToPanel.put( channelName, panel );

            panel.setLayout( new BoxLayout(panel, BoxLayout.LINE_AXIS) );
            panel.setBorder( BorderFactory.createEmptyBorder(0, 10, 0, 10 ) );
            panel.add( Box.createHorizontalGlue() );
            panel.setOpaque( true );
            //panel.setBackground( Utils.asColor( bdvViewable.getColor() ) );

            JLabel jLabel = new JLabel( channelName + "   " );
            jLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

            panel.add( jLabel );

            int[] buttonDimensions = new int[]{ 50, 30 };

            if ( ! ( bdvSource instanceof BdvOverlaySource ) &&
                    ! ( bdvViewable.getType().equals( Metadata.Type.Segmentation )) &&
                    ! ( bdvViewable instanceof TableRowsIntervalImage ) )
            {
                final JButton colorButton = createColorButtonWithColoredBackground( buttonDimensions, bdvSource, Utils.asColor( bdvViewable.getColor() ) );
                panel.add( colorButton );
            }
            else if ( bdvViewable instanceof TableRowsIntervalImage )
            {
                final JButton colorButton = createColorByColumnButton( buttonDimensions, ( ( TableRowsIntervalImage ) bdvViewable ).getTableView() );
                panel.add( colorButton );
            }
            else
            {
                addDummyButton( panel, buttonDimensions );
            }

            if ( bdvSource instanceof BdvStackSource )
            {
                JButton brightnessButton = getBrightnessButton(
                        channelName, ( BdvStackSource ) bdvSource, buttonDimensions );
                panel.add( brightnessButton );
            }
            else
            {
                addDummyButton( panel, buttonDimensions );
            }


            final JCheckBox visibilityCheckbox =
                    createVisibilityCheckbox( buttonDimensions, bdvSource, bdvViewable.isInitiallyVisible() );
            //visibilityCheckbox.setHorizontalAlignment( SwingConstants.RIGHT );
            panel.add( visibilityCheckbox );

            add( panel );
            refreshUIs();
        }
    }

    private void refreshUIs()
    {
        this.refreshUI();

        try{
            mainPanel.refreshUI();
        } catch ( Exception e )
        {
            // panel not yet visible.
        }
    }

    private static JButton createColorButtonWithColoredBackground(
            int[] buttonDimensions,
            BdvSource bdvSource,
            Color initialColor )
    {
        JButton colorButton;
        colorButton = new JButton( "C" );
        colorButton.setOpaque( true );
        colorButton.setBackground( initialColor );

        colorButton.setPreferredSize(
                new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

        colorButton.addActionListener( e -> {
            Color color = JColorChooser.showDialog( null, "", null );
            if ( color == null ) return;
            bdvSource.setColor( BdvUtils.asArgbType( color ) );
            colorButton.setBackground( color );
        } );

        return colorButton;
    }

    public void addDummyButton( JPanel panel, int[] buttonDimensions )
    {
        final JButton jButton = new JButton( "-" );
        jButton.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );
        panel.add( jButton );
    }

    private JButton createColorByColumnButton( int[] buttonDimensions, TableRowsTableView< DefaultAnnotatedIntervalTableRow > tableView )
    {
        JButton colorButton;
        colorButton = new JButton( "C" );
        colorButton.setPreferredSize(
                new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

        colorButton.addActionListener( e -> {
            tableView.showColorByColumnDialog();
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
        mainPanel.getImagePlateViewer().getBdvHandle().getViewerPanel().removeSource(
                source.getSources().get( 0 ).getSpimSource() );

        // remove from this panel
        remove( sourceNameToPanel.get( sourceName ) );
        sourceNameToPanel.remove( sourceName );

        // remove from image list
        mainPanel.removeSource( sourceName );

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