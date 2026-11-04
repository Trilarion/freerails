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
 * CargoWaitingAndDemandedPanel.java
 *
 */

package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.client.ModelRoot;
import freerails.model.ModelConstants;
import freerails.model.operations.cargo.Cargo;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.operations.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.player.Player;
import freerails.model.operations.station.Station;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Displays the cargo waiting and demanded at a station - used on
 * the select station popup window.
 */
public class CargoWaitingAndDemandedPanel extends JPanel implements View {

    @Serial
    private static final long serialVersionUID = 3760559784860071476L;
    private UnmodifiableWorld world;
    private Player player;
    private JList<String> demandsJList;
    private JLabel stationName;
    private JTable waitingJTable;

    /**
     * Creates a cargo waiting and demanded panel instance.
     */
    public CargoWaitingAndDemandedPanel() {
        GridBagConstraints gridBagConstraints;

        JScrollPane jScrollPane1 = new JScrollPane();
        JPanel jPanel1 = new JPanel();
        stationName = new JLabel();
        JLabel waiting = new JLabel();
        waitingJTable = new JTable();
        JLabel demands = new JLabel();
        demandsJList = new JList<>();
        JPanel spacer = new JPanel();

        setLayout(new GridBagLayout());

        setPreferredSize(new Dimension(100, 200));
        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jPanel1.setLayout(new GridBagLayout());

        stationName.setText("Station Name");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(6, 6, 6, 6);
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        jPanel1.add(stationName, gridBagConstraints);

        waiting.setText("Waiting");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        jPanel1.add(waiting, gridBagConstraints);

        waitingJTable.setBackground(UIManager.getDefaults().getColor("Button.background"));
        waitingJTable.setFont(new Font("Dialog", Font.PLAIN, 10));
        waitingJTable.setModel(new DefaultTableModel(new Object[][]{{"Mail", "4"}, {"Passengers", null}}, new String[]{"Title 1", "Title 2"}));
        waitingJTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        waitingJTable.setFocusable(false);
        waitingJTable.setRequestFocusEnabled(false);
        waitingJTable.setRowSelectionAllowed(false);
        waitingJTable.setShowHorizontalLines(false);
        waitingJTable.setShowVerticalLines(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(waitingJTable, gridBagConstraints);

        demands.setText("Demands");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        jPanel1.add(demands, gridBagConstraints);

        demandsJList.setBackground(UIManager.getDefaults().getColor("Button.background"));
        demandsJList.setFont(new Font("Dialog", Font.PLAIN, 10));
        demandsJList.setFocusable(false);
        demandsJList.setRequestFocusEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(demandsJList, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(spacer, gridBagConstraints);

        jScrollPane1.setViewportView(jPanel1);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);
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
        world = modelRoot.getWorld();
        player = modelRoot.getPlayer();
    }

    /**
     * Performs the display operation.
     *
     * @param newStationID the new station id parameter
     */
    public void display(int newStationID) {
        Station station = world.getStation(player, newStationID);
        stationName.setText(station.getStationName());
        final UnmodifiableCargoBatchBundle cargoWaiting = station.getCargoBatchBundle();

        // count the number of cargo types waiting and demanded.
        final List<String> typeWaiting = new ArrayList<>();
        final List<Integer> quantityWaiting = new ArrayList<>();
        final Collection<String> typeDemanded = new ArrayList<>();
        for (Cargo cargo : world.getCargos()) {
            int cargoId = cargo.getId();
            int amountWaiting = cargoWaiting.getAmountOfType(cargoId);

            if (0 != amountWaiting) {
                typeWaiting.add(cargo.getName());
                int carloads = amountWaiting / ModelConstants.UNITS_OF_CARGO_PER_WAGON;
                quantityWaiting.add(carloads);
            }
            if (station.getDemandForCargo().isCargoDemanded(cargoId)) {
                typeDemanded.add(cargo.getName());
            }
        }

        /*
         * The table shows the cargo waiting at the station. First column is
         * cargo type; second column is quantity in carloads.
         */
        TableModel tableModel = new MyAbstractTableModel(typeWaiting, quantityWaiting);
        waitingJTable.setModel(tableModel);

        // The list shows the cargo demanded by the station.
        demandsJList.setListData(typeDemanded.toArray(new String[0]));

        invalidate();
    }


    /**
     * Provides the my abstract table model functionality.
     */
    private static class MyAbstractTableModel extends AbstractTableModel {

        @Serial
        private static final long serialVersionUID = 3760559784860071476L;
        private final List<String> typeWaiting;
        private final List<Integer> quantityWaiting;

        /**
         * Creates an abstract table model instance.
         * @param typeWaiting the type waiting
         * @param quantityWaiting the quantity waiting
         */
        private MyAbstractTableModel(List<String> typeWaiting, List<Integer> quantityWaiting) {
            this.typeWaiting = typeWaiting;
            this.quantityWaiting = quantityWaiting;
        }

        /**
         * Returns the row count.
         * @return the row count
         */
        @Override
        public int getRowCount() {
            return typeWaiting.size();
        }

        /**
         * Returns the column count.
         * @return the column count
         */
        @Override
        public int getColumnCount() {
            return 2;
        }

        /**
         * Returns the value at.
         * @param rowIndex the row index
         * @param columnIndex the column index
         * @return the value at
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (0 == columnIndex) {
                return typeWaiting.get(rowIndex);
            }
            return quantityWaiting.get(rowIndex);
        }
    }
}
