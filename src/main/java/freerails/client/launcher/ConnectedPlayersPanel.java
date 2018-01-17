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
 * ConnectedPlayersPanel.java
 *
 */

package freerails.client.launcher;

import freerails.network.FreerailsGameServer;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Shows the players currently logged in to the server.
 */
class ConnectedPlayersPanel extends JPanel implements PropertyChangeListener {

    private static final long serialVersionUID = 4049080453489111344L;
    FreerailsGameServer server = null;
    private JList list1;

    /**
     * Creates new form ConnectedPlayersPanel
     */
    public ConnectedPlayersPanel() {
        GridBagConstraints gridBagConstraints;

        JLabel title = new JLabel();
        JScrollPane jScrollPane1 = new JScrollPane();
        list1 = new JList();

        setLayout(new GridBagLayout());

        title.setText("Connected Players");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(title, gridBagConstraints);

        list1.setModel(new MyAbstractListModel());
        jScrollPane1.setViewportView(list1);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

    }


    void updateListOfPlayers() {
        if (null != server) {
            String[] playerNames = server.getPlayerNames();
            playerNames = playerNames.length == 0 ? new String[]{"No players are logged on!"} : playerNames;
            setListOfPlayers(playerNames);
        }
    }

    void setListOfPlayers(String[] players) {
        list1.setListData(players);
    }

    /**
     * Called by the server when a player is added or removed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(FreerailsGameServer.CONNECTED_PLAYERS)) {
            if (EventQueue.isDispatchThread()) {
                updateListOfPlayers();
            } else {
                EventQueue.invokeLater(this::updateListOfPlayers);
            }
        }
    }

}
