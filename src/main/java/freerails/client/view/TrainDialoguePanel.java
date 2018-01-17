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
 * TrainDialoguePanel.java
 *
 */

package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot;
import freerails.world.*;
import freerails.world.player.FreerailsPrincipal;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
* Displays info on a train; it is composed of a
 * {@link TrainSchedulePanel} and {@link TrainDescriptionPanel}.
 */
public class TrainDialoguePanel extends JPanel implements View, WorldListListener {

    private static final long serialVersionUID = 3257005466801157938L;
    private static final Logger logger = Logger.getLogger(TrainDialoguePanel.class.getName());
    private JButton closeJButton;
    private TrainSchedulePanel newTrainSchedulePanel1;
    private JButton nextJButton;
    private JButton previousJButton;
    private TrainDescriptionPanel trainDetailsJPanel1;
    private JButton trainListJButton;
    private WorldIterator worldIterator;
    private ReadOnlyWorld world;
    private FreerailsPrincipal principal;

    public TrainDialoguePanel() {
        GridBagConstraints gridBagConstraints;

        newTrainSchedulePanel1 = new TrainSchedulePanel();
        trainDetailsJPanel1 = new TrainDescriptionPanel();
        previousJButton = new JButton();
        nextJButton = new JButton();
        trainListJButton = new JButton();
        closeJButton = new JButton();

        setLayout(new GridBagLayout());

        setPreferredSize(new Dimension(510, 400));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(newTrainSchedulePanel1, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(trainDetailsJPanel1, gridBagConstraints);

        previousJButton.setText("last");
        previousJButton.addActionListener(this::previousJButtonActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(previousJButton, gridBagConstraints);

        nextJButton.setText("next");
        nextJButton.addActionListener(this::nextJButtonActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(nextJButton, gridBagConstraints);

        trainListJButton.setText("Train list");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(trainListJButton, gridBagConstraints);

        closeJButton.setText("Close");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(closeJButton, gridBagConstraints);

    }


    private void previousJButtonActionPerformed(ActionEvent evt) {
        // Add your handling code here:
        if (worldIterator.previous()) {
            display(worldIterator.getIndex());
        } else {
            logger.warn("Couldn't get previous");
        }
    }

    private void nextJButtonActionPerformed(ActionEvent evt) {
        // Add your handling code here:
        if (worldIterator.next()) {
            display(worldIterator.getIndex());
        } else {
            logger.warn("Couldn't get next");
        }
    }

    public void setup(ModelRoot modelRoot, RendererRoot vl, Action closeAction) {
        newTrainSchedulePanel1.setup(modelRoot, vl, closeAction);
        trainDetailsJPanel1.setup(modelRoot, vl, closeAction);
        setCancelButtonActionListener(closeAction);
        principal = modelRoot.getPrincipal();
        world = modelRoot.getWorld();
    }

    public void display(int trainNumber) {
        worldIterator = new NonNullElementWorldIterator(KEY.TRAINS, world, principal);
        worldIterator.gotoIndex(trainNumber);
        if (worldIterator.getRowID() > 0) {
            previousJButton.setEnabled(true);
        } else {
            previousJButton.setEnabled(false);
        }

        if (worldIterator.getRowID() < (worldIterator.size() - 1)) {
            nextJButton.setEnabled(true);
        } else {
            nextJButton.setEnabled(false);
        }

        newTrainSchedulePanel1.display(trainNumber);
        trainDetailsJPanel1.displayTrain(trainNumber);
    }

    public void listUpdated(KEY key, int index, FreerailsPrincipal principal) {
        newTrainSchedulePanel1.listUpdated(key, index, principal);
    }

    public void itemAdded(KEY key, int index, FreerailsPrincipal principal) {
    }

    public void itemRemoved(KEY key, int index, FreerailsPrincipal principal) {
    }

    void setTrainDetailsButtonActionListener(ActionListener l) {
        ActionListener[] oldListeners = trainListJButton.getActionListeners();
        for (ActionListener oldListener : oldListeners) {
            trainListJButton.removeActionListener(oldListener);
        }
        trainListJButton.addActionListener(l);
    }

    /**
     * Removes any existing ActionListener listeners from the cancel button,
     * then adds the specified one.
     */
    void setCancelButtonActionListener(ActionListener l) {
        ActionListener[] oldListeners = closeJButton.getActionListeners();
        for (ActionListener oldListener : oldListeners) {
            closeJButton.removeActionListener(oldListener);
        }
        closeJButton.addActionListener(l);
    }

}
