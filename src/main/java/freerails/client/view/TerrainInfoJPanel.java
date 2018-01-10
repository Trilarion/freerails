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
 * TerrainInfoJPanel.java
 *
 */

package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.cargo.CargoType;
import freerails.world.terrain.TileConsumption;
import freerails.world.terrain.TileConversion;
import freerails.world.terrain.TileProduction;
import freerails.world.terrain.TerrainType;
import freerails.world.train.WagonType;

import javax.swing.*;
import java.awt.*;

/**
 * This JPanel shows information on a terrain type.
 */
public class TerrainInfoJPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 3258131375164045363L;

    private RendererRoot rr;

    private ReadOnlyWorld w;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel terrainDescription;
    private javax.swing.JLabel terrainImage;
    private javax.swing.JLabel terrainName;

    public TerrainInfoJPanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        terrainImage = new javax.swing.JLabel();
        terrainName = new javax.swing.JLabel();
        terrainDescription = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        terrainImage.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/freerails/client/graphics/terrain/City_0.png")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 4, 4);
        add(terrainImage, gridBagConstraints);

        terrainName.setFont(new java.awt.Font("Dialog", 1, 14));
        terrainName.setText("City");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 8);
        add(terrainName, gridBagConstraints);

        terrainDescription.setFont(new java.awt.Font("Dialog", 0, 12));
        terrainDescription
                .setText("<html>\n<p>Right-of-Way costs X per mile. </p>\n<table width=\"75%\" >\n  <tr> \n    <td><strong>Supplies:</strong></td>\n    <td>&nbsp;</td>\n  </tr>\n  <tr> \n    <td>Mail </td>\n    <td>2</td>\n  </tr>\n  <tr> \n    <td>Passengers</td>\n    <td>2</td>\n  </tr>\n  <tr> \n    <td> <strong>Demands</strong></td>\n    <td>&nbsp;</td>\n  </tr>\n  <tr> \n    <td>Mail</td>\n    <td>&nbsp;</td>\n  </tr>\n  <tr> \n    <td>Passengers</td>\n    <td>&nbsp;</td>\n  </tr>\n  <tr> \n    <td><strong>Converts</strong></td>\n    <td>&nbsp;</td>\n  </tr>\n  <tr> \n    <td>Livestock to Food</td>\n    <td>&nbsp;</td>\n  </tr>\n  <tr>\n    <td>Steel to Goods</td>\n    <td>&nbsp;</td>\n  </tr>\n</table>\n</html>");
        terrainDescription.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 8);
        add(terrainDescription, gridBagConstraints);

    }

    public void setup(ReadOnlyWorld w, RendererRoot vl) {
        this.w = w;
        rr = vl;
    }

    public void setTerrainType(int typeNumber) {

        TerrainType type = (TerrainType) w.get(SKEY.TERRAIN_TYPES, typeNumber);

        String row = "<p>Right-of-Way costs $" + type.getRightOfWay()
                + " per mile. </p>";
        StringBuilder tableString = new StringBuilder();
        int cargosProduced = type.getProduction().size();
        int cargosConsumed = type.getConsumption().size();
        int cargosConverted = type.getConversion().size();
        if ((cargosProduced + cargosConsumed + cargosConverted) > 0) {
            // if the terrain type produces, consumes, or converts anything.
            tableString = new StringBuilder("<table width=\"75%\" >");
            if (cargosProduced != 0) {
                tableString.append("<tr> <td><strong>Supplies</strong></td> <td>&nbsp;</td> </tr>");
                for (int i = 0; i < cargosProduced; i++) {
                    TileProduction p = type.getProduction().get(i);
                    CargoType c = (CargoType) w.get(SKEY.CARGO_TYPES, p
                            .getCargoType());
                    String supply = String.valueOf(p.getRate()
                            / WagonType.UNITS_OF_CARGO_PER_WAGON);
                    tableString.append("<tr> <td>").append(c.getDisplayName()).append(" </td><td>").append(supply).append("</td></tr>");
                }
            }
            if (cargosConsumed != 0) {
                tableString.append("<tr> <td><strong>Demands</strong></td> <td>&nbsp;</td> </tr>");
                for (int i = 0; i < cargosConsumed; i++) {
                    TileConsumption p = type.getConsumption().get(i);
                    CargoType c = (CargoType) w.get(SKEY.CARGO_TYPES, p
                            .getCargoType());
                    tableString.append("<tr> <td>").append(c.getDisplayName()).append(" </td><td>&nbsp;</td></tr>");
                }
            }
            if (cargosConverted != 0) {
                tableString.append("<tr> <td><strong>Converts</strong></td> <td>&nbsp;</td> </tr>");
                for (int i = 0; i < cargosConverted; i++) {
                    TileConversion p = type.getConversion().get(i);
                    CargoType input = (CargoType) w.get(SKEY.CARGO_TYPES, p
                            .getInput());
                    CargoType output = (CargoType) w.get(SKEY.CARGO_TYPES, p
                            .getOutput());
                    tableString.append("<tr> <td colspan=\"2\">").append(input.getDisplayName()).append(" to ").append(output.getDisplayName()).append("</td></tr>");
                }
            }
            tableString.append("</table> ");
        }
        String labelString = "<html>" + row + tableString + "</html>";
        terrainDescription.setText(labelString);
        terrainName.setText(type.getDisplayName());

        Image tileIcon = rr.getTileViewWithNumber(typeNumber).getDefaultIcon();
        terrainImage.setIcon(new ImageIcon(tileIcon));

        repaint();
    }
    // End of variables declaration//GEN-END:variables

}
