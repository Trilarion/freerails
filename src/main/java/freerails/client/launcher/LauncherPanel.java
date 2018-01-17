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
 * LauncherPanel.java
 *
 */

package freerails.client.launcher;

import freerails.client.ClientConfig;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * The first launcher panel, lets you choose 'single player', 'start network
 * game' etc.
 */
final class LauncherPanel extends JPanel {

    private static final long serialVersionUID = 3257850965422913590L;
    private final ButtonModel[] buttonModels = new ButtonModel[4];
    private ButtonGroup buttonGroup1;

    public LauncherPanel() {
        GridBagConstraints gridBagConstraints;

        buttonGroup1 = new ButtonGroup();
        JRadioButton singlePlayerButton = new JRadioButton();
        JRadioButton startNetworkButton = new JRadioButton();
        JRadioButton joinNetworkButton = new JRadioButton();
        JRadioButton serverOnlyButton = new JRadioButton();
        JPanel paddingJPanel = new JPanel();

        setLayout(new GridBagLayout());

        setBorder(new TitledBorder(new EtchedBorder(), "Select Game Type"));
        buttonGroup1.add(singlePlayerButton);
        singlePlayerButton.setSelected(true);
        singlePlayerButton.setText("Single-Player");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(singlePlayerButton, gridBagConstraints);

        buttonGroup1.add(startNetworkButton);
        startNetworkButton.setText("Start a network game");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(startNetworkButton, gridBagConstraints);

        buttonGroup1.add(joinNetworkButton);
        joinNetworkButton.setText("Join a network game");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(joinNetworkButton, gridBagConstraints);

        buttonGroup1.add(serverOnlyButton);
        serverOnlyButton.setText("Server only");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(serverOnlyButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(paddingJPanel, gridBagConstraints);
        buttonModels[ClientConfig.MODE_SINGLE_PLAYER] = singlePlayerButton.getModel();
        buttonModels[ClientConfig.MODE_START_NETWORK_GAME] = startNetworkButton.getModel();
        buttonModels[ClientConfig.MODE_JOIN_NETWORK_GAME] = joinNetworkButton.getModel();
        buttonModels[ClientConfig.MODE_SERVER_ONLY] = serverOnlyButton.getModel();
    }

    int getMode() {
        for (int i = 0; i < buttonModels.length; i++) {
            if (buttonGroup1.getSelection() == buttonModels[i]) {
                return i;
            }
        }
        assert false;
        return 0;
    }


}
