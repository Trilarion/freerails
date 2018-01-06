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
 * TrainOrderJPanel.java
 *
 */

package freerails.client.view;

import freerails.client.ClientConfig;
import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot;
import freerails.world.KEY;
import freerails.world.ReadOnlyWorld;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.Station;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ListCellRenderer that displays a train order.
 */
public class TrainOrderJPanel implements View, ListCellRenderer {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 4051047466990319413L;
    private final ImageIcon gotoNow = new ImageIcon(TrainOrderJPanel.class
            .getResource(ClientConfig.GRAPHIC_ARROW_SELECTED));
    private final ImageIcon gotoAfterPriorityOrders = new ImageIcon(
            TrainOrderJPanel.class
                    .getResource(ClientConfig.GRAPHIC_ARROW_DESELECTED));
    private final ImageIcon dontGoto = null;
    private final Color backgoundColor = (java.awt.Color) javax.swing.UIManager
            .getDefaults().get("List.background");
    private final Color selectedColor = (java.awt.Color) javax.swing.UIManager
            .getDefaults().get("List.selectionBackground");
    private final Color selectedColorNotFocused = Color.LIGHT_GRAY;
    private final Map<TrainOrderModel, TrainOrderJPanelSingle> lines;
    private ReadOnlyWorld w;
    private FreerailsPrincipal principal;
    private Action closeAction;
    private RendererRoot vl;
    private ModelRoot mr;

    /**
     *
     */
    public TrainOrderJPanel() {
        super();
        lines = new HashMap<>();
    }

    /**
     * @param mr
     * @param vl
     * @param closeAction
     */
    public void setup(ModelRoot mr, RendererRoot vl, Action closeAction) {
        this.mr = mr;
        this.vl = vl;
        this.closeAction = closeAction;
        w = mr.getWorld();
        principal = mr.getPrincipal();
    }

    public java.awt.Component getListCellRendererComponent(JList list,
                                                           Object value, int index, boolean isSelected, boolean cellHasFocus) {
        TrainOrdersListModel.TrainOrdersListElement trainOrders = (TrainOrdersListModel.TrainOrdersListElement) value;

        // Set station name
        int stationNumber = trainOrders.order.stationId;
        Station station = (Station) w.get(principal, KEY.STATIONS,
                stationNumber);
        String stationName = station.getStationName();

        // Set wait until full
        String waitUntilFull = trainOrders.order.waitUntilFull ? "Wait until full"
                : "";
        Selection select;
        if (isSelected) {
            if (list.isFocusOwner()) {
                select = Selection.select;
            } else {
                select = Selection.selectNoFocus;
            }
        } else {
            select = Selection.unselect;
        }
        String orderText;
        if (null == trainOrders.order.consist) {
            if (trainOrders.order.autoConsist) {
                orderText = "Select wagons automatically";
            } else {
                orderText = "No Change";
            }
        } else {
            orderText = "";
        }

        TrainOrderModel tm = new TrainOrderModel(stationName, waitUntilFull,
                select, trainOrders.gotoStatus, orderText);
        TrainOrderJPanelSingle panelSingle = lines.get(tm);
        if (panelSingle == null) {
            panelSingle = new TrainOrderJPanelSingle();
            panelSingle.setup(mr, vl, closeAction);

            panelSingle.stationNameJLabel.setText(stationName);
            panelSingle.ordersJLabel.setText(waitUntilFull);

            // Set selected
            if (isSelected) {
                if (list.isFocusOwner()) {
                    panelSingle.setBackground(selectedColor);
                } else {
                    panelSingle.setBackground(selectedColorNotFocused);
                }
            } else {
                panelSingle.setBackground(backgoundColor);
            }

            // Set goto status.
            switch (trainOrders.gotoStatus) {
                case TrainOrdersListModel.DONT_GOTO:
                    panelSingle.gotoIcon.setIcon(this.dontGoto);
                    break;
                case TrainOrdersListModel.GOTO_AFTER_PRIORITY_ORDERS:
                    panelSingle.gotoIcon.setIcon(this.gotoAfterPriorityOrders);
                    break;
                case TrainOrdersListModel.GOTO_NOW:
                    panelSingle.gotoIcon.setIcon(this.gotoNow);
                    break;
                default:
                    throw new IllegalArgumentException(String
                            .valueOf(trainOrders.gotoStatus));
            }
            panelSingle.gotoIcon.setPreferredSize(new Dimension(20, 20));

            // Set consist
            TrainListCellRenderer trainViewJPanel = (TrainListCellRenderer) panelSingle.consistChangeJPanel;
            trainViewJPanel.display(trainOrders.trainNumber, index);

            // Show priority orders.
            if (trainOrders.isPriorityOrder) {
                // Write the station name in upper case
                String s = panelSingle.stationNameJLabel.getText();
                panelSingle.stationNameJLabel.setText(s + " (Priority Orders)");
            }

            // Check for 'No change'
            if (null == trainOrders.order.consist) {
                if (trainOrders.order.autoConsist) {
                    panelSingle.noChangeJLabel
                            .setText("Select wagons automatically");
                } else {
                    panelSingle.noChangeJLabel.setText("No Change");
                }
            } else {
                panelSingle.noChangeJLabel.setText(null);
            }
            lines.put(tm, panelSingle);
        } else {
            TrainListCellRenderer trainViewJPanel = (TrainListCellRenderer) panelSingle.consistChangeJPanel;
            trainViewJPanel.display(trainOrders.trainNumber, index);
        }
        return panelSingle;
    }

    enum Selection {
        select, selectNoFocus, unselect
    }

    // 666 model still not correct ...

    private final class TrainOrderJPanelSingle extends javax.swing.JPanel
            implements View {

        private static final long serialVersionUID = 3516604388665786813L;
        // Variables declaration - do not modify//GEN-BEGIN:variables
        javax.swing.JPanel consistChangeJPanel;
        javax.swing.JLabel gotoIcon;
        javax.swing.JLabel noChangeJLabel;
        javax.swing.JLabel ordersJLabel;
        javax.swing.JLabel stationNameJLabel;

        public TrainOrderJPanelSingle() {
            initComponents();
            this.setBackground(backgoundColor);
        }

        /**
         * This method is called from within the constructor to initialize the
         * form. WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        private void initComponents() {// GEN-BEGIN:initComponents
            java.awt.GridBagConstraints gridBagConstraints;

            gotoIcon = new javax.swing.JLabel();
            consistChangeJPanel = new TrainListCellRenderer();
            noChangeJLabel = new javax.swing.JLabel();
            stationNameJLabel = new javax.swing.JLabel();
            ordersJLabel = new javax.swing.JLabel();

            setLayout(new java.awt.GridBagLayout());

            gotoIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                    ClientConfig.GRAPHIC_ARROW_SELECTED)));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridheight = 2;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            add(gotoIcon, gridBagConstraints);

            consistChangeJPanel.setLayout(new java.awt.GridBagLayout());

            noChangeJLabel.setText("No Change");
            consistChangeJPanel.add(noChangeJLabel,
                    new java.awt.GridBagConstraints());

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            add(consistChangeJPanel, gridBagConstraints);

            stationNameJLabel.setText("Some Station");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
            add(stationNameJLabel, gridBagConstraints);

            ordersJLabel.setText("wait until full / don't wait");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.ipadx = 6;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 5);
            add(ordersJLabel, gridBagConstraints);

        }// GEN-END:initComponents

        // End of variables declaration//GEN-END:variables

        public void setup(ModelRoot mr, RendererRoot vl, Action closeAction) {
            w = mr.getWorld();
            TrainListCellRenderer trainViewJPanel = (TrainListCellRenderer) consistChangeJPanel;
            trainViewJPanel.setHeight(15);
            trainViewJPanel.setup(mr, vl, null);
            principal = mr.getPrincipal();

        }

    }

    /**
     * contains all data which is displayed for one station (order). This is
     * used to find prebuilt Panels
     */
    private class TrainOrderModel {
        final String stationName;
        final String waitUntilFull;
        final Selection selected;
        final int gotoStatus;
        final String orderText;

        /**
         * @param stationName
         * @param waitUntilFull
         * @param selected
         * @param gotoStatus
         */
        public TrainOrderModel(String stationName, String waitUntilFull,
                               Selection selected, int gotoStatus, String orderText) {
            super();
            this.stationName = stationName;
            this.waitUntilFull = waitUntilFull;
            this.selected = selected;
            this.gotoStatus = gotoStatus;
            this.orderText = orderText;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TrainOrderModel)) {
                return false;
            }
            TrainOrderModel cmp = (TrainOrderModel) obj;
            if (cmp.gotoStatus != gotoStatus) {
                return false;
            }
            if (cmp.selected != selected) {
                return false;
            }
            if (stationName != null && cmp.stationName == null) {
                return false;
            }
            if (stationName != null && !stationName.equals(cmp.stationName)) {
                return false;
            }
            if (waitUntilFull != null && cmp.waitUntilFull == null) {
                return false;
            }
            if (waitUntilFull != null && !waitUntilFull.equals(cmp.waitUntilFull)) {
                return false;
            }
            return orderText == null || orderText.equals(cmp.orderText);
        }

        @Override
        public int hashCode() {
            return waitUntilFull.hashCode() + stationName.hashCode()
                    + selected.hashCode() + gotoStatus + orderText.hashCode();
        }

    }

}
