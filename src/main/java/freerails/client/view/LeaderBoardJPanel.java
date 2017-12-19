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
 * Created on Sep 8, 2004
 *
 */
package freerails.client.view;

import freerails.client.renderer.RenderersRoot;
import freerails.controller.ModelRoot;
import freerails.controller.NetWorthCalculator;
import freerails.world.finances.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.top.NonNullElements;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.TransactionAggregator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A JPanel that displays the details of the players ordered by net worth.
 */
public class LeaderBoardJPanel extends JPanel implements View {

    private static final long serialVersionUID = 3258131375298066229L;
    private final List<PlayerDetails> values;
    private JList playersList = null;
    private ActionListener submitButtonCallBack = null;

    /**
     * This method initializes
     */
    public LeaderBoardJPanel() {
        super();

        values = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            PlayerDetails p = new PlayerDetails();
            p.networth = new Money(rand.nextInt(100));
            values.add(p);
        }
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.add(getPlayersList(), null);
        java.awt.event.MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (null == submitButtonCallBack) {
                    System.err.println("mouseClicked");
                } else {
                    submitButtonCallBack.actionPerformed(new ActionEvent(this,
                            0, null));
                }
            }
        };
        this.addMouseListener(mouseAdapter);
        this.playersList.addMouseListener(mouseAdapter);
        this.setSize(getPreferredSize());

    }

    /**
     * This method initializes jList
     *
     * @return javax.swing.JList
     */
    private JList getPlayersList() {
        if (playersList == null) {
            playersList = new JList();
            playersList
                    .setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            playersList.setRequestFocusEnabled(false);
            playersList.setEnabled(true);

            Collections.sort(values);
            playersList.setListData(values.toArray());
        }
        return playersList;
    }

    /**
     * @param modelRoot
     * @param vl
     * @param closeAction
     */
    public void setup(ModelRoot modelRoot, RenderersRoot vl, Action closeAction) {
        ReadOnlyWorld w = modelRoot.getWorld();
        values.clear();
        this.submitButtonCallBack = closeAction;
        for (int player = 0; player < w.getNumberOfPlayers(); player++) {
            PlayerDetails details = new PlayerDetails();
            FreerailsPrincipal principle = w.getPlayer(player).getPrincipal();
            details.name = principle.getName();
            NonNullElements stations = new NonNullElements(KEY.STATIONS, w,
                    principle);
            details.stations = stations.size();
            TransactionAggregator networth = new NetWorthCalculator(w,
                    principle);
            details.networth = networth.calculateValue();
            values.add(details);
        }
        Collections.sort(values);
        playersList.setListData(values.toArray());
        setSize(getPreferredSize());
    }

    /**
     * Stores the details a player that are shown on the leaderboard.
     */
    static class PlayerDetails implements Comparable<PlayerDetails> {

        String name = "player";

        Money networth = new Money(0);

        int stations = 0;

        @Override
        public String toString() {
            return name +
                    ", " +
                    networth.toString() +
                    " net worth, " +
                    stations +
                    "  stations.";
        }

        public int compareTo(PlayerDetails test) {
            long l = test.networth.getAmount() - networth.getAmount();
            return (int) l;
        }

    }
} // @jve:decl-index=0:visual-constraint="67,32"
