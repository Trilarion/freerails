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
 *
 */
package freerails.client.view;

import freerails.client.MoneyFormatter;
import freerails.client.renderer.RendererRoot;
import freerails.client.ModelRoot;
import freerails.model.economy.transaction.aggregator.NetWorthAggregator;
import freerails.model.economy.Money;
import freerails.model.economy.PlayerDetails;
import freerails.model.economy.transaction.aggregator.TransactionAggregator;
import freerails.model.simulation.Time;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Displays the details of the players ordered by net worth.
 */
public class LeaderBoardPanel extends JPanel implements View {

    @Serial
    private static final long serialVersionUID = 3258131375298066229L;
    private final List<PlayerDetails> values;
    private JList<PlayerDetails> playersList = null;
    private ActionListener submitButtonCallBack = null;

    /**
     * This method initializes
     */
    public LeaderBoardPanel() {
        super();

        values = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            PlayerDetails p = new PlayerDetails();
            p.setNetworth(new Money(rand.nextInt(100)));
            values.add(p);
        }
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        add(getPlayersList(), null);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(event -> {
            if (submitButtonCallBack != null) {
                submitButtonCallBack.actionPerformed(event);
            }
        });
        add(closeButton);
        setSize(getPreferredSize());
    }

    /**
     * This method initializes list
     *
     * @return javax.swing.JList
     */
    private JList<PlayerDetails> getPlayersList() {
        if (playersList == null) {
            playersList = new JList<>();
            playersList.setCellRenderer(new DefaultListCellRenderer() {
                @Serial
                private static final long serialVersionUID = 1L;

                /**
                 * Returns the list cell renderer component.
                 * @param list the list
                 * @param value the value
                 * @param index the index
                 * @param isSelected the is selected
                 * @param cellHasFocus the cell has focus
                 * @return the list cell renderer component
                 */
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                              boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus);
                    if (value instanceof PlayerDetails details) {
                        label.setText(details.getName() + ", "
                                + MoneyFormatter.format(details.getNetworth())
                                + " net worth, " + details.getStations() + "  stations.");
                    }
                    return label;
                }
            });
            playersList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            playersList.setRequestFocusEnabled(false);
            playersList.setEnabled(true);

            Collections.sort(values);
            playersList.setListData(values.toArray(new PlayerDetails[0]));
        }
        return playersList;
    }

    /**
     * Sets the up.
     *
     * @param modelRoot the client model that provides current game state
     * @param rendererRoot the renderer root parameter
     * @param closeAction the action invoked when the panel is closed
     */
    @Override
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        UnmodifiableWorld world = modelRoot.getWorld();
        values.clear();
        submitButtonCallBack = closeAction;
        Time[] times = {Time.ZERO, world.getClock().getCurrentTime()};
        for (Player player: world.getPlayers()) {
            PlayerDetails details = new PlayerDetails();
            details.setName(player.getName());
            details.setStations(world.getStations(player).size());
            TransactionAggregator netWorth = new NetWorthAggregator(world, player, times);
            netWorth.aggregate();
            details.setNetworth(netWorth.getValues()[0]);
            values.add(details);
        }
        Collections.sort(values);
        playersList.setListData(values.toArray(new PlayerDetails[0]));
        setSize(getPreferredSize());
    }

} // @jve:decl-index=0:visual-constraint="67,32"
