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
import freerails.model.world.PlayerKey;
import freerails.model.NonNullElementWorldIterator;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.SharedKey;
import freerails.model.cargo.CargoType;
import freerails.model.cargo.ImmutableCargoBatchBundle;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.train.Train;

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
    private ReadOnlyWorld world;
    private FreerailsPrincipal principal;
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
        Train train = (Train) world.get(principal, PlayerKey.Trains, trainNumber);

        int cargoBundleID = train.getCargoBundleID();
        Serializable cb = world.get(principal, PlayerKey.CargoBundles, cargoBundleID);

        if (train != lastTrain || cb != lastCargoBundle) displayTrain(trainNumber);
    }


    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {

        trainViewJPanel1.setup(modelRoot, rendererRoot, closeAction);
        trainViewJPanel1.setHeight(30);
        trainViewJPanel1.setCenterTrain(true);
        world = modelRoot.getWorld();
        principal = modelRoot.getPrincipal();
    }

    public void displayTrain(int newTrainNumber) {

        NonNullElementWorldIterator it = new NonNullElementWorldIterator(PlayerKey.Trains, world, principal);
        it.gotoIndex(newTrainNumber);

        trainNumber = newTrainNumber;

        trainViewJPanel1.display(newTrainNumber);
        Train train = (Train) world.get(principal, PlayerKey.Trains, newTrainNumber);

        int cargoBundleID = train.getCargoBundleID();
        ImmutableCargoBatchBundle cb = (ImmutableCargoBatchBundle) world.get(principal, PlayerKey.CargoBundles, cargoBundleID);
        StringBuilder s = new StringBuilder("Train #" + it.getNaturalNumber() + ": ");
        int numberOfTypesInBundle = 0;
        for (int i = 0; i < world.size(SharedKey.CargoTypes); i++) {
            int amount = cb.getAmountOfType(i);
            if (0 != amount) {
                CargoType ct = (CargoType) world.get(SharedKey.CargoTypes, i);
                String cargoTypeName = ct.getDisplayName();
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
        lastCargoBundle = cb;
        lastTrain = train;
    }

}
