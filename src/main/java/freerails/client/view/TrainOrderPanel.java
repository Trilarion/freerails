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
 * TrainOrderPanel.java
 *
 */

package freerails.client.view;

import freerails.client.ClientConfig;
import freerails.client.model.TrainOrdersListModel;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.TrainListCellRenderer;
import freerails.controller.ModelRoot;
import freerails.model.world.PlayerKey;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.Station;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ListCellRenderer that displays a train order.
 */
public class TrainOrderPanel implements View, ListCellRenderer {

    private final Icon gotoNow = new ImageIcon(TrainOrderPanel.class.getResource(ClientConfig.GRAPHIC_ARROW_SELECTED));
    private final Icon gotoAfterPriorityOrders = new ImageIcon(TrainOrderPanel.class.getResource(ClientConfig.GRAPHIC_ARROW_DESELECTED));
    private final ImageIcon dontGoto = null;
    public final Color backgoundColor = (Color) UIManager.getDefaults().get("List.background");
    private final Color selectedColor = (Color) UIManager.getDefaults().get("List.selectionBackground");
    private final Color selectedColorNotFocused = Color.LIGHT_GRAY;
    private final Map<TrainOrderModel, TrainOrderPanelSingle> lines;
    public ReadOnlyWorld world;
    public FreerailsPrincipal principal;
    private Action closeAction;
    private RendererRoot vl;
    private ModelRoot modelRoot;

    /**
     *
     */
    public TrainOrderPanel() {
        super();
        lines = new HashMap<>();
    }

    /**
     * @param modelRoot
     * @param rendererRoot
     * @param closeAction
     */
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        this.modelRoot = modelRoot;
        this.vl = rendererRoot;
        this.closeAction = closeAction;
        world = modelRoot.getWorld();
        principal = modelRoot.getPrincipal();
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        TrainOrdersListModel.TrainOrdersListElement trainOrders = (TrainOrdersListModel.TrainOrdersListElement) value;

        // Set station name
        int stationNumber = trainOrders.order.stationId;
        Station station = (Station) world.get(principal, PlayerKey.Stations, stationNumber);
        String stationName = station.getStationName();

        // Set wait until full
        String waitUntilFull = trainOrders.order.waitUntilFull ? "Wait until full" : "";
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

        TrainOrderModel tm = new TrainOrderModel(stationName, waitUntilFull, select, trainOrders.gotoStatus, orderText);
        TrainOrderPanelSingle panelSingle = lines.get(tm);
        if (panelSingle == null) {
            panelSingle = new TrainOrderPanelSingle(this);
            panelSingle.setup(modelRoot, vl, closeAction);

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

            // Set goto success.
            switch (trainOrders.gotoStatus) {
                case TrainOrdersListModel.DONT_GOTO:
                    panelSingle.gotoIcon.setIcon(dontGoto);
                    break;
                case TrainOrdersListModel.GOTO_AFTER_PRIORITY_ORDERS:
                    panelSingle.gotoIcon.setIcon(gotoAfterPriorityOrders);
                    break;
                case TrainOrdersListModel.GOTO_NOW:
                    panelSingle.gotoIcon.setIcon(gotoNow);
                    break;
                default:
                    throw new IllegalArgumentException(String.valueOf(trainOrders.gotoStatus));
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
                    panelSingle.noChangeJLabel.setText("Select wagons automatically");
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

    // TODO model still not correct ...

    /**
     * contains all data which is displayed for one station (order). This is
     * used to find prebuilt Panels
     */
    private static class TrainOrderModel {
        private final String stationName;
        private final String waitUntilFull;
        private final Selection selected;
        private final int gotoStatus;
        private final String orderText;

        /**
         * @param stationName
         * @param waitUntilFull
         * @param selected
         * @param gotoStatus
         */
        private TrainOrderModel(String stationName, String waitUntilFull, Selection selected, int gotoStatus, String orderText) {
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
            return waitUntilFull.hashCode() + stationName.hashCode() + selected.hashCode() + gotoStatus + orderText.hashCode();
        }
    }

}
