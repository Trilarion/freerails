package freerails.client.view;

import freerails.client.ClientConfig;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.TrainListCellRenderer;
import freerails.controller.ModelRoot;

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class TrainOrderPanelSingle extends JPanel implements View {

    private static final long serialVersionUID = 3516604388665786813L;
    private TrainOrderPanel trainOrderPanel;
    public JPanel consistChangeJPanel;
    public JLabel gotoIcon;
    public JLabel noChangeJLabel;
    public JLabel ordersJLabel;
    public JLabel stationNameJLabel;

    TrainOrderPanelSingle(TrainOrderPanel trainOrderPanel) {
        this.trainOrderPanel = trainOrderPanel;
        GridBagConstraints gridBagConstraints;

        gotoIcon = new JLabel();
        consistChangeJPanel = new TrainListCellRenderer();
        noChangeJLabel = new JLabel();
        stationNameJLabel = new JLabel();
        ordersJLabel = new JLabel();

        setLayout(new GridBagLayout());

        gotoIcon.setIcon(new ImageIcon(getClass().getResource(ClientConfig.GRAPHIC_ARROW_SELECTED)));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(gotoIcon, gridBagConstraints);

        consistChangeJPanel.setLayout(new GridBagLayout());

        noChangeJLabel.setText("No Change");
        consistChangeJPanel.add(noChangeJLabel, new GridBagConstraints());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(consistChangeJPanel, gridBagConstraints);

        stationNameJLabel.setText("Some Station");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        add(stationNameJLabel, gridBagConstraints);

        ordersJLabel.setText("wait until full / don't wait");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 6;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 6, 0, 5);
        add(ordersJLabel, gridBagConstraints);

        setBackground(trainOrderPanel.backgoundColor);
    }


    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        trainOrderPanel.world = modelRoot.getWorld();
        TrainListCellRenderer trainViewJPanel = (TrainListCellRenderer) consistChangeJPanel;
        trainViewJPanel.setHeight(15);
        trainViewJPanel.setup(modelRoot, rendererRoot, null);
        trainOrderPanel.principal = modelRoot.getPrincipal();
    }
}
