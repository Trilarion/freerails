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
public class TrainOrderPanel implements View, ListCellRenderer {

    private final Icon gotoNow = new ImageIcon(TrainOrderPanel.class.getResource(ClientConfig.GRAPHIC_ARROW_SELECTED));
    private final Icon gotoAfterPriorityOrders = new ImageIcon(TrainOrderPanel.class.getResource(ClientConfig.GRAPHIC_ARROW_DESELECTED));
    private final ImageIcon dontGoto = null;
    private final Color backgoundColor = (Color) UIManager.getDefaults().get("List.background");
    private final Color selectedColor = (Color) UIManager.getDefaults().get("List.selectionBackground");
    private final Color selectedColorNotFocused = Color.LIGHT_GRAY;
    private final Map<TrainOrderModel, TrainOrderJPanelSingle> lines;
    private ReadOnlyWorld world;
    private FreerailsPrincipal principal;
    private Action closeAction;
    private RendererRoot vl;
    private ModelRoot mr;

    /**
     *
     */
    public TrainOrderPanel() {
        super();
        lines = new HashMap<>();
    }

    /**
     * @param modelRoot
     * @param vl
     * @param closeAction
     */
    public void setup(ModelRoot modelRoot, RendererRoot vl, Action closeAction) {
        this.mr = modelRoot;
        this.vl = vl;
        this.closeAction = closeAction;
        world = modelRoot.getWorld();
        principal = modelRoot.getPrincipal();
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        TrainOrdersListModel.TrainOrdersListElement trainOrders = (TrainOrdersListModel.TrainOrdersListElement) value;

        // Set station name
        int stationNumber = trainOrders.order.stationId;
        Station station = (Station) world.get(principal, KEY.STATIONS, stationNumber);
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

    // 666 model still not correct ...

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

    private final class TrainOrderJPanelSingle extends JPanel implements View {

        private static final long serialVersionUID = 3516604388665786813L;
            private JPanel consistChangeJPanel;
        private JLabel gotoIcon;
        private JLabel noChangeJLabel;
        private JLabel ordersJLabel;
        private JLabel stationNameJLabel;

        private TrainOrderJPanelSingle() {
            initComponents();
            setBackground(backgoundColor);
        }

        /**
         * This method is called from within the constructor to initialize the
         * form. WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        private void initComponents() {
            GridBagConstraints gridBagConstraints;

            gotoIcon = new JLabel();
            consistChangeJPanel = new TrainListCellRenderer();
            noChangeJLabel = new JLabel();
            stationNameJLabel = new JLabel();
            ordersJLabel = new JLabel();

            setLayout(new GridBagLayout());

            gotoIcon.setIcon(new ImageIcon(getClass().getResource(ClientConfig.GRAPHIC_ARROW_SELECTED)));
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridheight = 2;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            add(gotoIcon, gridBagConstraints);

            consistChangeJPanel.setLayout(new GridBagLayout());

            noChangeJLabel.setText("No Change");
            consistChangeJPanel.add(noChangeJLabel, new GridBagConstraints());

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            add(consistChangeJPanel, gridBagConstraints);

            stationNameJLabel.setText("Some Station");
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(0, 5, 0, 5);
            add(stationNameJLabel, gridBagConstraints);

            ordersJLabel.setText("wait until full / don't wait");
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.ipadx = 6;
            gridBagConstraints.anchor = GridBagConstraints.EAST;
            gridBagConstraints.insets = new Insets(0, 6, 0, 5);
            add(ordersJLabel, gridBagConstraints);

        }


        public void setup(ModelRoot modelRoot, RendererRoot vl, Action closeAction) {
            world = modelRoot.getWorld();
            TrainListCellRenderer trainViewJPanel = (TrainListCellRenderer) consistChangeJPanel;
            trainViewJPanel.setHeight(15);
            trainViewJPanel.setup(modelRoot, vl, null);
            principal = modelRoot.getPrincipal();

        }

    }

}
