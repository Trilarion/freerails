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
 * CargoWaitingAndDemandedPanel.java
 *
 */

package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot;
import freerails.world.KEY;
import freerails.world.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.cargo.CargoBatchBundle;
import freerails.world.cargo.CargoType;
import freerails.world.cargo.ImmutableCargoBatchBundle;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.Station;
import freerails.world.train.WagonType;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Displays the cargo waiting and demanded at a station - used on
 * the select station popup window.
 */
public class CargoWaitingAndDemandedPanel extends JPanel implements View {

    private static final long serialVersionUID = 3760559784860071476L;
    private ReadOnlyWorld world;
    private FreerailsPrincipal principal;
    private JList demandsJList;
    private JLabel stationName;
    private JTable waitingJTable;

    /**
     *
     */
    public CargoWaitingAndDemandedPanel() {
        GridBagConstraints gridBagConstraints;

        JScrollPane jScrollPane1 = new JScrollPane();
        JPanel jPanel1 = new JPanel();
        stationName = new JLabel();
        JLabel waiting = new JLabel();
        waitingJTable = new JTable();
        JLabel demands = new JLabel();
        demandsJList = new JList();
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
        waitingJTable.setFont(new Font("Dialog", 0, 10));
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
        demandsJList.setFont(new Font("Dialog", 0, 10));
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
     * @param modelRoot
     * @param rendererRoot
     * @param closeAction
     */
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        world = modelRoot.getWorld();
        principal = modelRoot.getPrincipal();
    }

    /**
     * @param newStationID
     */
    public void display(int newStationID) {
        Station station = (Station) world.get(principal, KEY.STATIONS, newStationID);
        stationName.setText(station.getStationName());
        final CargoBatchBundle cargoWaiting = (ImmutableCargoBatchBundle) world.get(principal, KEY.CARGO_BUNDLES, station.getCargoBundleID());

        // count the number of cargo types waiting and demanded.
        final List<String> typeWaiting = new ArrayList<>();
        final List<Integer> quantityWaiting = new ArrayList<>();
        final Collection<String> typeDemanded = new ArrayList<>();
        for (int i = 0; i < world.size(SKEY.CARGO_TYPES); i++) {
            CargoType cargoType = (CargoType) world.get(SKEY.CARGO_TYPES, i);
            int amountWaiting = cargoWaiting.getAmountOfType(i);

            if (0 != amountWaiting) {
                typeWaiting.add(cargoType.getDisplayName());
                int carloads = amountWaiting / WagonType.UNITS_OF_CARGO_PER_WAGON;
                quantityWaiting.add(carloads);
            }
            if (station.getDemandForCargo().isCargoDemanded(i)) {
                typeDemanded.add(cargoType.getDisplayName());
            }
        }

        /*
         * The table shows the cargo waiting at the station. First column is
         * cargo type; second column is quantity in carloads.
         */
        TableModel tableModel = new MyAbstractTableModel(typeWaiting, quantityWaiting);
        waitingJTable.setModel(tableModel);

        // The list shows the cargo demanded by the station.
        demandsJList.setListData(typeDemanded.toArray());

        invalidate();
    }


    private static class MyAbstractTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 3760559784860071476L;
        private final List<String> typeWaiting;
        private final List<Integer> quantityWaiting;

        private MyAbstractTableModel(List<String> typeWaiting, List<Integer> quantityWaiting) {
            this.typeWaiting = typeWaiting;
            this.quantityWaiting = quantityWaiting;
        }

        public int getRowCount() {
            return typeWaiting.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (0 == columnIndex) {
                return typeWaiting.get(rowIndex);
            }
            return quantityWaiting.get(rowIndex);
        }
    }
}
