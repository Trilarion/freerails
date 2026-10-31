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
 * TrainOrderPanel.java
 *
 */

package freerails.client.view;

import freerails.client.ClientConstants;
import freerails.client.model.TrainOrdersListModel;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.TrainListCellRenderer;
import freerails.client.ModelRoot;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.operations.station.Station;
import freerails.model.operations.train.schedule.TrainOrder;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ListCellRenderer that displays a train order.
 */
public class TrainOrderPanel implements View, ListCellRenderer<TrainOrdersListModel.TrainOrdersListElement> {

    private final Icon gotoNow = new ImageIcon(TrainOrderPanel.class.getResource(ClientConstants.GRAPHIC_ARROW_SELECTED));
    private final ImageIcon dontGoto = null;
    public final Color backgoundColor = (Color) UIManager.getDefaults().get("List.background");
    private final Color selectedColor = (Color) UIManager.getDefaults().get("List.selectionBackground");
    private final Color selectedColorNotFocused = Color.LIGHT_GRAY;
    private final Map<TrainOrderModel, TrainOrderPanelSingle> lines;
    public UnmodifiableWorld world;
    public Player player;
    private Action closeAction;
    private RendererRoot vl;
    private ModelRoot modelRoot;

    /**
     * Creates a train order panel instance.
     */
    public TrainOrderPanel() {
        super();
        lines = new HashMap<>();
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
        this.modelRoot = modelRoot;
        vl = rendererRoot;
        this.closeAction = closeAction;
        world = modelRoot.getWorld();
        player = modelRoot.getPlayer();
    }

    /**
     * Returns the list cell renderer component.
     * @param list the list
     * @param trainOrders the train orders
     * @param index the index
     * @param isSelected the is selected
     * @param cellHasFocus the cell has focus
     * @return the list cell renderer component
     */
    @Override
    public Component getListCellRendererComponent(JList<? extends TrainOrdersListModel.TrainOrdersListElement> list, TrainOrdersListModel.TrainOrdersListElement trainOrders, int index, boolean isSelected, boolean cellHasFocus) {

        // Set station name
        int stationNumber = trainOrders.order.getStationId();
        Station station = world.getStation(player, stationNumber);
        String stationName = station.getStationName();

        // Set wait until full
        String waitUntilFull = trainOrders.order.isWaitUntilFull() ? "Wait until full" : "";
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
        if (trainOrders.order.getLoadingPolicy() != TrainOrder.LoadingPolicy.SPECIFIED_CONSIST) {
            if (trainOrders.order.isAutoConsist()) {
                orderText = "Select wagons automatically";
            } else {
                orderText = "No Change";
            }
        } else {
            orderText = "";
        }

        TrainOrderModel tm = new TrainOrderModel(stationName, waitUntilFull, select, trainOrders.gotoStatus,
                orderText, trainOrders.isUrgentStop);
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

            if (trainOrders.isUrgentStop) {
                String s = panelSingle.stationNameJLabel.getText();
                panelSingle.stationNameJLabel.setText(s + " (Urgent, one-shot)");
            }

            // Check for 'No change'
            if (trainOrders.order.getLoadingPolicy() != TrainOrder.LoadingPolicy.SPECIFIED_CONSIST) {
                if (trainOrders.order.isAutoConsist()) {
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
     * Contains the data displayed for one station order and identifies its
     * prebuilt panel.
     *
     * @param stationName displayed name of the station in the order
     * @param waitUntilFull wait-until-full setting for the order
     * @param selected selection state of the order
     * @param gotoStatus destination status associated with the order
     * @param orderText text displayed for the order
     * @param urgentStop whether the order is an urgent stop
     */
        private record TrainOrderModel(String stationName, String waitUntilFull, Selection selected, int gotoStatus,
                                       String orderText, boolean urgentStop) {
        /**
         * Creates a model for one station's train order.
         *
         * @param stationName the name of the station
         * @param waitUntilFull the wait-until-full setting
         * @param selected the selection state for the order
         * @param gotoStatus the order's destination status
         */
        private TrainOrderModel {
        }

    }

}
