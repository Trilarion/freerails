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
 * StationInfoPanel.java
 *
 */

package freerails.client.view;

import freerails.client.ModelRootProperty;
import freerails.client.renderer.RendererRoot;
import freerails.client.ModelRoot;
import freerails.model.world.*;
import freerails.model.operations.cargo.Cargo;
import freerails.model.operations.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.terrain.TerrainTile;
import freerails.model.player.Player;
import freerails.model.operations.station.Station;
import freerails.model.ModelConstants;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

// TODO with the demise of the NonNullWorldIterator this is broken completely, need a bidirectional iterator over stations
/**
 * Displays the supply and demand at a station.
 */
public class StationInfoPanel extends JPanel implements View {

    private static final TaggedLogger logger = Logger.tag("StationInfoPanel");
    @Serial
    private static final long serialVersionUID = 4050759377680150585L;
    private UnmodifiableWorld world;
    private ModelRoot modelRoot;
    private final List<Station> stations = new ArrayList<>();
    private int stationIndex = -1;
    private JButton close;
    private JLabel label1;
    private JButton nextStation;
    private JButton previousStation;
    private final ComponentListener componentListener = new ComponentAdapter() {
        /**
         * Component hidden.
         * @param e the e
         */
        @Override
        public void componentHidden(ComponentEvent e) {
        }

        /**
         * Component shown.
         * @param evt the evt
         */
        @Override
        public void componentShown(ComponentEvent evt) {
            display();
        }
    };

    /**
     * Creates a station info panel instance.
     */
    public StationInfoPanel() {
        GridBagConstraints gridBagConstraints;

        label1 = new JLabel();
        nextStation = new JButton();
        previousStation = new JButton();
        close = new JButton();

        setLayout(new GridBagLayout());

        setMinimumSize(new Dimension(250, 177));
        label1.setFont(new Font("Dialog", Font.PLAIN, 10));
        label1.setText("<html>\n<h4 align=\"center\">Supply and Demand at stationName</h4>\n<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\">\n  <tr>\n    <td>&nbsp;</td>\n    <td>Will pay<br>for</td>\n    <td>Supplies<br>(cars per year)</td>\n    <td>Waiting for pickup<br>(car loads)</td>\n  </tr>\n   <tr>\n    <td>Mail</td>\n    <td>Yes</td>\n    <td>&nbsp;</td>\n    <td>&nbsp;</td>\n  </tr>\n  <tr>\n    <td>Passengers</td>\n    <td>No</td>\n    <td>3</td>\n    <td>2.5</td>\n  </tr>\n \n</table>\n\n</html>");
        label1.setVerticalAlignment(SwingConstants.TOP);
        label1.setAlignmentY(0.0F);
        label1.setVerticalTextPosition(SwingConstants.TOP);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(8, 8, 4, 8);
        add(label1, gridBagConstraints);

        nextStation.setText("next ->");
        nextStation.setMargin(new Insets(0, 0, 0, 0));
        nextStation.addActionListener(this::nextStationActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        add(nextStation, gridBagConstraints);

        previousStation.setText("<- previous");
        previousStation.setMargin(new Insets(0, 0, 0, 0));
        previousStation.addActionListener(this::previousStationActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        add(previousStation, gridBagConstraints);

        close.setText("close");
        close.setMargin(new Insets(0, 0, 0, 0));
        close.setMaximumSize(new Dimension(65, 22));
        close.setMinimumSize(new Dimension(65, 22));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        add(close, gridBagConstraints);
    }


    /**
     * Previous station action performed.
     * @param evt the evt
     */
    private void previousStationActionPerformed(ActionEvent evt) {
        if (stationIndex > 0) {
            stationIndex--;
            modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, stations.get(stationIndex).getLocation());
            display();
        }
    }

    /**
     * Next station action performed.
     * @param evt the evt
     */
    private void nextStationActionPerformed(ActionEvent evt) {
        if (stationIndex >= 0 && stationIndex < stations.size() - 1) {
            stationIndex++;
            modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, stations.get(stationIndex).getLocation());
            display();
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
        this.modelRoot = modelRoot;
        this.world = modelRoot.getWorld();
        this.stations.clear();
        this.stations.addAll(this.world.getStations(modelRoot.getPlayer()));
        this.stationIndex = this.stations.isEmpty() ? -1 : 0;
        addComponentListener(componentListener);
        close.addActionListener(closeAction);
    }

    /**
     * Sets the station.
     * @param stationNumber the station number
     */
    public void setStation(int stationNumber) {
        if (stations.isEmpty()) {
            stationIndex = -1;
            return;
        }
        stationIndex = Math.clamp(stationNumber, 0, stations.size() - 1);
        display();
    }

    /**
     * Display.
     */
    private void display() {
        previousStation.setEnabled(stationIndex > 0);
        nextStation.setEnabled(stationIndex >= 0 && stationIndex < stations.size() - 1);

        String label;
        if (stationIndex >= 0 && stationIndex < stations.size()) {
            Station station = stations.get(stationIndex);
            TerrainTile tile = world.getTile(station.getLocation());
            String stationTypeName = tile.trackPiece().trackType().getName();
            UnmodifiableCargoBatchBundle cargoWaiting = station.getCargoBatchBundle();

            StringBuilder table1 = new StringBuilder();
            table1.append("<html>");
            table1.append("<h2 align=\"center\">");
            table1.append(station.getStationName());
            table1.append(" (");
            table1.append(stationTypeName);
            table1.append(")</h2>");
            table1.append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"3\"><tr><td>&nbsp;</td>\n    <td>Demand</td>\n    <td>Supplies<br/>(cars/year)</td><td>Ready<br />(loads)</td>  </tr>");

            for (Cargo cargo : world.getCargos()) {
                int id = cargo.getId();
                String demanded = station.getDemandForCargo().isCargoDemanded(id) ? "Yes" : "No";
                int amountSupplied = station.getSupply().getSupply(id);
                boolean isSupplied = amountSupplied > 0;
                String supply = isSupplied ? String.valueOf(amountSupplied / ModelConstants.UNITS_OF_CARGO_PER_WAGON) : "&nbsp;";
                int amountWaiting = cargoWaiting.getAmountOfType(id);
                String waiting = amountWaiting > 0 ? String.valueOf(amountWaiting / ModelConstants.UNITS_OF_CARGO_PER_WAGON) : "&nbsp;";

                if (station.getDemandForCargo().isCargoDemanded(id) || isSupplied) {
                    table1.append("<tr>");
                    table1.append("<td>").append(cargo.getName()).append("</td>");
                    table1.append("<td align=center>").append(demanded).append("</td>");
                    table1.append("<td align=center>").append(supply).append("</td>");
                    table1.append("<td align=center>").append(waiting).append("</td>");
                    table1.append("</tr>");
                }
            }
            table1.append("</table>");
            table1.append("</html>");
            label = table1.toString();
        } else {
            label = "<html><h2 align=\"center\">No Station " + "Selected</h2></html>";
        }
        label1.setText(label);
        repaint();
    }

    /**
     * Paints this component.
     * @param g the g
     */
    @Override
    protected void paintComponent(Graphics g) {
        // We need to update if the cargo bundle has changed.
        Player playerPlayer = modelRoot.getPlayer();

        super.paintComponent(g);
    }

    /**
     * Removes the close button.
     */
    void removeCloseButton() {
        remove(close);
    }

}
