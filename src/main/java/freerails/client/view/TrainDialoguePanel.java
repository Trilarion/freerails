/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
import freerails.client.ModelRoot;
import freerails.model.operations.train.Train;
import freerails.model.player.Player;
import freerails.model.world.*;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
* Displays info on a train; it is composed of a
 * {@link TrainSchedulePanel} and {@link TrainDescriptionPanel}.
 */
public class TrainDialoguePanel extends JPanel implements View {

    @Serial
    private static final long serialVersionUID = 3257005466801157938L;
    private static final TaggedLogger logger = Logger.tag("TrainDialoguePanel");
    private JButton closeJButton;
    private TrainSchedulePanel newTrainSchedulePanel1;
    private JButton nextJButton;
    private JButton previousJButton;
    private TrainDescriptionPanel trainDetailsJPanel1;
    private JButton trainListJButton;
    private UnmodifiableWorld world;
    private Player player;
    private final List<Train> trains = new ArrayList<>();
    private int currentTrainIndex = -1;

    /**
     * Creates a train dialogue panel instance.
     */
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


    /**
     * Previous j button action performed.
     * @param evt the evt
     */
    private void previousJButtonActionPerformed(ActionEvent evt) {
        if (currentTrainIndex > 0) {
            currentTrainIndex--;
            display(trains.get(currentTrainIndex).getId());
        } else {
            logger.warn("Couldn't get previous");
        }
    }

    /**
     * Next j button action performed.
     * @param evt the evt
     */
    private void nextJButtonActionPerformed(ActionEvent evt) {
        if (currentTrainIndex >= 0 && currentTrainIndex < trains.size() - 1) {
            currentTrainIndex++;
            display(trains.get(currentTrainIndex).getId());
        } else {
            logger.warn("Couldn't get next");
        }
    }

    /**
     * Sets the up.
     * @param modelRoot the model root
     * @param rendererRoot the renderer root
     * @param closeAction the close action
     */
    @Override
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        newTrainSchedulePanel1.setup(modelRoot, rendererRoot, closeAction);
        trainDetailsJPanel1.setup(modelRoot, rendererRoot, closeAction);
        setCancelButtonActionListener(closeAction);
        player = modelRoot.getPlayer();
        world = modelRoot.getWorld();
        trains.clear();
        trains.addAll(world.getTrains(player));
        currentTrainIndex = trains.isEmpty() ? -1 : 0;
    }

    /**
     * Display.
     * @param trainNumber the train number
     */
    public void display(int trainNumber) {
        newTrainSchedulePanel1.display(trainNumber);
        trainDetailsJPanel1.displayTrain(trainNumber);
    }

    /**
     * Sets the train details button action listener.
     * @param l the l
     */
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
