/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * TrainListPanel.java
 *
 */

package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.TrainListCellRenderer;
import freerails.client.ModelRoot;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.*;

/**
* Displays a list of trains, used for the train list window and the
* train roaster tab.
*/
public class TrainListPanel extends JPanel implements View {

    private static final long serialVersionUID = 3832905463863064626L;
    private UnmodifiableWorld world;
    private Player player;
    private int lastNumberOfTrains = -1;
    private boolean rhsjTabPane = false; // if the train list is for the rhsjTabPane then use the original renderer, if not use the trainsummaryjpanel
    // TODO empty showTrainDetails
    private ActionListener showTrainDetails = e -> {};
    private JButton closeJButton;
    private JList list1;
    private JScrollPane jScrollPane1;
    private JButton showDetails;
    private TrainSummaryPanel trainSummaryPanel1;
    private int trainViewHeight = 50;

    /**
     * Creates new form TrainListPanel.
     */
    public TrainListPanel() {
        GridBagConstraints gridBagConstraints;

        trainSummaryPanel1 = new TrainSummaryPanel();
        closeJButton = new JButton();
        showDetails = new JButton();
        jScrollPane1 = new JScrollPane();
        list1 = new JList();
        JLabel trainNumLabel = new JLabel();
        JLabel trainHeadingLabel = new JLabel();
        JLabel maintenanceLabel = new JLabel();
        JLabel incomeLabel = new JLabel();

        setLayout(new GridBagLayout());

        setPreferredSize(new Dimension(510, 300));
        closeJButton.setText("Close");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(closeJButton, gridBagConstraints);

        showDetails.setText("Show details");
        showDetails.addActionListener(this::showDetailsActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(showDetails, gridBagConstraints);

        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list1.setCellRenderer(trainSummaryPanel1);
        list1.setDoubleBuffered(true);
        list1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                list1KeyPressed(e);
            }
        });
        list1.addListSelectionListener(this::list1ValueChanged);
        list1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                list1MouseClicked(e);
            }
        });

        jScrollPane1.setViewportView(list1);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        trainNumLabel.setText("Train Number");
        trainNumLabel.setMaximumSize(new Dimension(500, 500));
        trainNumLabel.setPreferredSize(new Dimension(100, 14));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);
        add(trainNumLabel, gridBagConstraints);

        trainHeadingLabel.setText("Headed For");
        trainHeadingLabel.setPreferredSize(new Dimension(100, 14));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        add(trainHeadingLabel, gridBagConstraints);

        maintenanceLabel.setText("Maintenance YTD");
        maintenanceLabel.setPreferredSize(new Dimension(100, 14));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        add(maintenanceLabel, gridBagConstraints);

        incomeLabel.setText("Income YTD");
        incomeLabel.setPreferredSize(new Dimension(100, 14));
        add(incomeLabel, new GridBagConstraints());
    }

    public TrainListPanel(boolean isInRHSJTabPane) {
        this();
        rhsjTabPane = isInRHSJTabPane;
    }


    private void list1ValueChanged(ListSelectionEvent evt) {
        // if a train is selected, enable the 'show details' button.
        showDetails.setEnabled(list1.getSelectedIndex() != -1);
    }

    private void showDetailsActionPerformed(ActionEvent evt) {
        showTrainDetails.actionPerformed(evt);
    }

    private void list1MouseClicked(MouseEvent evt) {
        // Add your handling code here:
        if (evt.getClickCount() == 2) {
            showTrainDetails.actionPerformed(null);
        }
    }

    private void list1KeyPressed(KeyEvent evt) {
        // Add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            showTrainDetails.actionPerformed(null);
        }
    }

    @Override
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        world = modelRoot.getWorld();
        trainSummaryPanel1.setup(modelRoot, rendererRoot, null);

        if (rhsjTabPane) {
            // TODO this was broken with the transition of trains, need to remake
            // list1.setModel(new WorldToListModelAdapter(modelRoot.getWorld(), PlayerKey.Trains, modelRoot.getPlayer()));
            TrainListCellRenderer trainView = new TrainListCellRenderer(modelRoot, rendererRoot);
            list1.setCellRenderer(trainView);
            trainView.setHeight(trainViewHeight);
        }

        ActionListener[] oldListeners = closeJButton.getActionListeners();
        for (ActionListener oldListener : oldListeners) {
            closeJButton.removeActionListener(oldListener);
        }
        closeJButton.addActionListener(closeAction);
        player = modelRoot.getPlayer();
    }

    void setShowTrainDetailsActionListener(ActionListener l) {
        showTrainDetails = l;
    }

    int getSelectedTrainID() {
        /*
         * Note, the selected index is not the train id since trains that have
         * been removed are not shown on the list.
         */
        int row = list1.getSelectedIndex();
        // TODO this is broken with the transition of Trains from PlayerKey and playerlists, make new
        //return NonNullElementWorldIterator.rowToIndex(world, PlayerKey.Trains, player, row);
        return 0;
    }


    /**
     * When the train list is shown on a tab we don't want the buttons.
     */
    void removeButtons() {
        removeAll();

        GridBagConstraints gridBagConstraints;
        setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);
    }

    /**
     * @param trainViewHeight
     */
    public void setTrainViewHeight(int trainViewHeight) {
        this.trainViewHeight = trainViewHeight;
    }

    @Override
    public void paint(Graphics g) {
        if (null != world) {
            int newNumberOfTrains = world.getTrains(player).size();
            if (newNumberOfTrains != lastNumberOfTrains) {
                // TODO we should set a different model there, was broken
                // list1.setModel(new WorldToListModelAdapter(world, PlayerKey.Trains, player));
                if (newNumberOfTrains > 0) {
                    list1.setSelectedIndex(0);
                }
                lastNumberOfTrains = newNumberOfTrains;
            }
        }
        super.paint(g);
    }

}
