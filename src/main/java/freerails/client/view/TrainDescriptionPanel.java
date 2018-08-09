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
 * TrainDetailsJPanel.java
 *
 */

package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.TrainListCellRenderer;
import freerails.client.ModelRoot;
import freerails.model.cargo.Cargo;
import freerails.model.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.train.Train;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.Serializable;

/**
 * Displays a side-on view of a train and a summary of the cargo
 * that it is carrying.
 */
public class TrainDescriptionPanel extends javax.swing.JPanel implements View {

    private static final long serialVersionUID = 3977018444325664049L;
    private UnmodifiableWorld world;
    private Player player;
    private int trainNumber = -1;
    private Serializable lastTrain, lastCargoBundle;
    private JLabel label1;
    private TrainListCellRenderer trainViewJPanel1;

    public TrainDescriptionPanel() {
        GridBagConstraints gridBagConstraints;

        label1 = new JLabel();
        trainViewJPanel1 = new TrainListCellRenderer();

        setLayout(new GridBagLayout());

        setBorder(new TitledBorder("Current Details"));
        setPreferredSize(new Dimension(250, 97));
        label1.setFont(new Font("Dialog", 0, 12));
        label1.setText("<html><head></head><body>Trains X: 20 passengers, 15 tons of mfg goods, 12 sacks of mail, and 7 tons of livestock.</body></html>");
        label1.setMinimumSize(new Dimension(250, 17));
        label1.setHorizontalTextPosition(SwingConstants.LEADING);
        label1.setVerticalTextPosition(SwingConstants.TOP);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(label1, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(trainViewJPanel1, gridBagConstraints);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Check whether the train or its cargo have changed since the last call
        // to this method.
        updateIfNecessary();

        super.paintComponent(g);
    }

    private void updateIfNecessary() {
        Train train = world.getTrain(player, trainNumber);

        Serializable cb = train.getCargoBatchBundle();

        if (train != lastTrain || cb != lastCargoBundle) displayTrain(trainNumber);
    }


    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {

        trainViewJPanel1.setup(modelRoot, rendererRoot, closeAction);
        trainViewJPanel1.setHeight(30);
        trainViewJPanel1.setCenterTrain(true);
        world = modelRoot.getWorld();
        player = modelRoot.getPlayer();
    }

    public void displayTrain(int newTrainNumber) {

        trainNumber = newTrainNumber;

        trainViewJPanel1.display(newTrainNumber);
        Train train = world.getTrain(player, newTrainNumber);

        UnmodifiableCargoBatchBundle cargoBatchBundle = train.getCargoBatchBundle();
        // TODO natural number is the number in the list/set
        // StringBuilder s = new StringBuilder("Train #" + it.getNaturalNumber() + ": ");
        StringBuilder s = new StringBuilder("Train #" + ": ");
        int numberOfTypesInBundle = 0;
        for (Cargo cargo : world.getCargos()) {
            int id = cargo.getId();
            int amount = cargoBatchBundle.getAmountOfType(id);
            if (0 != amount) {
                String cargoTypeName = cargo.getName();
                if (0 != numberOfTypesInBundle) {
                    s.append("; ");
                }
                numberOfTypesInBundle++;

                s.append(cargoTypeName).append(" (").append(amount).append(')');
            }
        }
        if (0 == numberOfTypesInBundle) {
            s.append("no cargo");
        }
        s.append('.');
        label1.setText(s.toString());
        lastCargoBundle = cargoBatchBundle;
        lastTrain = train;
    }

}
