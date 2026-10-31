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

package freerails.client.view;

import freerails.client.model.TrainOrdersListModel;
import freerails.client.renderer.RendererRoot;
import freerails.client.ModelRoot;
import freerails.model.cargo.Cargo;
import freerails.model.station.Station;
import freerails.model.train.schedule.TrainOrder;
import freerails.model.world.*;
import freerails.move.Move;

import freerails.model.player.Player;
import freerails.model.train.*;
import freerails.model.train.schedule.ScheduleState;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.move.UpdateTrainMove;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.Serial;
import java.util.*;
import java.util.List;

/**
 * Displays a train's schedule and provides controls that let you
 * edit it.
 */
public class TrainSchedulePanel extends JPanel implements View {

    @Serial
    private static final long serialVersionUID = 3762248626113884214L;
    private static final Logger logger = Logger.getLogger(TrainSchedulePanel.class.getName());
    private JButton addStationJButton;
    private JMenu addWagonJMenu;
    private JPopupMenu editOrderJPopupMenu;
    private JMenuItem gotoStationJMenuItem;
    private JList<TrainOrdersListModel.TrainOrdersListElement> orders;
    private JButton urgentStopJButton;
    private JMenuItem pullUpJMenuItem;
    private JMenuItem pushDownJMenuItem;
    private JMenu removeWagonsJMenu;
    private SelectStationPanel selectStationPanel1;
    private JPopupMenu selectStationJPopupMenu;
    private TrainOrderPanel trainOrderPanel1;
    private JMenu waitJMenu;
    private int trainId = -1;
    private TrainOrdersListModel listModel;
    private ModelRoot modelRoot;
    private RendererRoot vl;

    public TrainSchedulePanel() {
        GridBagConstraints gridBagConstraints;

        trainOrderPanel1 = new TrainOrderPanel();
        editOrderJPopupMenu = new JPopupMenu();
        gotoStationJMenuItem = new JMenuItem();
        JMenuItem changeStation = new JMenuItem();
        JMenuItem removeStationJMenuItem = new JMenuItem();
        JSeparator jSeparator1 = new JSeparator();
        addWagonJMenu = new JMenu();
        removeWagonsJMenu = new JMenu();
        JMenuItem removeLastJMenuItem = new JMenuItem();
        JMenuItem removeAllJMenuItem = new JMenuItem();
        JMenu changeConsistJMenu = new JMenu();
        JMenuItem noChangeJMenuItem = new JMenuItem();
        JMenuItem engineOnlyJMenuItem = new JMenuItem();
        JMenuItem autoConsistJMenuItem = new JMenuItem();
        waitJMenu = new JMenu();
        JMenuItem dontWaitJMenuItem = new JMenuItem();
        JMenuItem waitUntilFullJMenuItem = new JMenuItem();
        JSeparator jSeparator2 = new JSeparator();
        pullUpJMenuItem = new JMenuItem();
        pushDownJMenuItem = new JMenuItem();
        selectStationPanel1 = new SelectStationPanel();
        selectStationJPopupMenu = new JPopupMenu();
        selectStationJPopupMenu.add(selectStationPanel1);
        addStationJButton = new JButton();
        urgentStopJButton = new JButton();
        JScrollPane jScrollPane1 = new JScrollPane();
        orders = new JList<>();

        gotoStationJMenuItem.setText("Goto station");
        gotoStationJMenuItem.addActionListener(this::gotoStationJMenuItemActionPerformed);

        editOrderJPopupMenu.add(gotoStationJMenuItem);

        changeStation.setText("Change Station");
        changeStation.addActionListener(this::changeStationActionPerformed);

        editOrderJPopupMenu.add(changeStation);

        removeStationJMenuItem.setText("Remove station");
        removeStationJMenuItem.addActionListener(this::removeStationJMenuItemActionPerformed);

        editOrderJPopupMenu.add(removeStationJMenuItem);

        editOrderJPopupMenu.add(jSeparator1);

        addWagonJMenu.setText("Add Wagon");
        editOrderJPopupMenu.add(addWagonJMenu);

        removeWagonsJMenu.setText("Remove wagon(s)");
        removeLastJMenuItem.setText("Remove last");
        removeLastJMenuItem.addActionListener(this::removeLastJMenuItemActionPerformed);

        removeWagonsJMenu.add(removeLastJMenuItem);

        removeAllJMenuItem.setText("Remove all wagons");
        removeAllJMenuItem.addActionListener(this::removeAllJMenuItemActionPerformed);

        removeWagonsJMenu.add(removeAllJMenuItem);

        editOrderJPopupMenu.add(removeWagonsJMenu);

        changeConsistJMenu.setText("Change consist to..");
        noChangeJMenuItem.setText("'No change'");
        noChangeJMenuItem.addActionListener(this::noChangeJMenuItemActionPerformed);

        changeConsistJMenu.add(noChangeJMenuItem);

        engineOnlyJMenuItem.setText("Engine only");
        engineOnlyJMenuItem.addActionListener(this::engineOnlyJMenuItemActionPerformed);

        changeConsistJMenu.add(engineOnlyJMenuItem);

        autoConsistJMenuItem.setText("Choose wagons automatically");
        autoConsistJMenuItem.addActionListener(this::autoConsistJMenuItemActionPerformed);

        changeConsistJMenu.add(autoConsistJMenuItem);

        editOrderJPopupMenu.add(changeConsistJMenu);

        waitJMenu.setText("Wait at station");
        dontWaitJMenuItem.setText("Don't wait");
        dontWaitJMenuItem.addActionListener(this::dontWaitJMenuItemActionPerformed);

        waitJMenu.add(dontWaitJMenuItem);

        waitUntilFullJMenuItem.setText("Wait until full");
        waitUntilFullJMenuItem.addActionListener(this::waitUntilFullJMenuItemActionPerformed);

        waitJMenu.add(waitUntilFullJMenuItem);

        editOrderJPopupMenu.add(waitJMenu);

        editOrderJPopupMenu.add(jSeparator2);

        pullUpJMenuItem.setText("Pull up");
        pullUpJMenuItem.addActionListener(this::pullUpJMenuItemActionPerformed);

        editOrderJPopupMenu.add(pullUpJMenuItem);

        pushDownJMenuItem.setText("Push down");
        pushDownJMenuItem.addActionListener(this::pushDownJMenuItemActionPerformed);

        editOrderJPopupMenu.add(pushDownJMenuItem);

        setLayout(new GridBagLayout());

        setBorder(new TitledBorder("Schedule"));
        addStationJButton.setText("Add Station");
        addStationJButton.addActionListener(this::addStationJButtonActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(addStationJButton, gridBagConstraints);

        urgentStopJButton.setText("Insert Urgent Stop");
        urgentStopJButton.addActionListener(this::urgentStopJButtonActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(urgentStopJButton, gridBagConstraints);

        jScrollPane1.setPreferredSize(new Dimension(280, 160));
        orders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orders.setCellRenderer(trainOrderPanel1);
        orders.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                ordersKeyPressed(e);
            }
        });
        orders.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ordersMouseClicked(e);
            }
        });

        jScrollPane1.setViewportView(orders);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(3, 3, 3, 3);
        add(jScrollPane1, gridBagConstraints);
    }


    private void ordersKeyPressed(KeyEvent evt) {
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_O: {
                // Insert urgent one-shot stop
                urgentStopJButtonActionPerformed(null);
                break;
            }
            case KeyEvent.VK_N: {
                // Add station
                addStationJButtonActionPerformed(null);
                break;
            }
            default: {}
        }

        int orderNumber = orders.getSelectedIndex();
        if (orderNumber == -1) {
            // No order is selected.
            return;
        }
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_G: {
                // Goto station.
                gotoStationJMenuItemActionPerformed(null);
                break;
            }
            case KeyEvent.VK_S: {
                // Change station
                showSelectStation(getSchedule(), orderNumber);
                break;
            }
            case KeyEvent.VK_A: {
                // Auto schedule
                setAutoConsist();
                break;
            }
            case KeyEvent.VK_C: {
                // Change add wagon

                break;
            }
            case KeyEvent.VK_DELETE: {
                // Remove station
                removeStationJMenuItemActionPerformed(null);
                break;
            }
            case KeyEvent.VK_BACK_SPACE: {
                // Remove last wagon
                removeLastWagon();
                break;
            }

            case KeyEvent.VK_W: {
                // toggle wait until full
                ScheduleState schedule = getSchedule();
                TrainOrder order = schedule.getOrder(orderNumber);
                setWaitUntilFull(!order.isWaitUntilFull());
                break;
            }
            default: {}
        }
        listModel.fireRefresh();
    }

    private void autoConsistJMenuItemActionPerformed(ActionEvent evt) {
        setAutoConsist();
    }

    private void changeStationActionPerformed(ActionEvent evt) {
        int orderNumber = orders.getSelectedIndex();
        showSelectStation(getSchedule(), orderNumber);
    }

    private void removeAllJMenuItemActionPerformed(ActionEvent evt) {
        removeAllWagons();
    }

    private void removeLastJMenuItemActionPerformed(ActionEvent evt) {
        removeLastWagon();
    }

    private void waitUntilFullJMenuItemActionPerformed(ActionEvent evt) {
        setWaitUntilFull(true);
    }

    private void dontWaitJMenuItemActionPerformed(ActionEvent evt) {
        setWaitUntilFull(false);
    }

    private void engineOnlyJMenuItemActionPerformed(ActionEvent evt) {
        removeAllWagons();
    }

    private void noChangeJMenuItemActionPerformed(ActionEvent evt) {
        noChange();
    }

    private void urgentStopJButtonActionPerformed(ActionEvent evt) {
        ScheduleState schedule = getSchedule();
        if (!schedule.canAddUrgentOrder()) {
            return;
        }
        try {
            schedule = schedule.addUrgentOrder(TrainOrder.keepConsist(getFirstStationID()));
            showSelectStation(schedule, schedule.getCurrentOrderIndex());
        } catch (NoSuchElementException e) {
            logger.warn("No stations exist so can't add station to schedule!");
        }
    }

    private void addStationJButtonActionPerformed(ActionEvent evt) {
        ScheduleState schedule = getSchedule();
        try {
            int newOrderNumber = schedule.getNumberOfOrders();
            schedule = schedule.addOrder(TrainOrder.keepConsist(getFirstStationID()));
            showSelectStation(schedule, newOrderNumber);
        } catch (NoSuchElementException e) {
            logger.warn("No stations exist so can't add station to schedule!");
        }
    }

    private void removeStationJMenuItemActionPerformed(ActionEvent evt) {
        ScheduleState schedule = getSchedule();
        int i = orders.getSelectedIndex();
        schedule = schedule.removeOrder(i);
        sendUpdateMove(schedule);
    }

    private void gotoStationJMenuItemActionPerformed(ActionEvent evt) {
        ScheduleState schedule = getSchedule();
        int i = orders.getSelectedIndex();
        schedule = schedule.setOrderToGoto(i);
        sendUpdateMove(schedule);
    }

    private void pushDownJMenuItemActionPerformed(ActionEvent evt) {
        ScheduleState schedule = getSchedule();
        int i = orders.getSelectedIndex();
        schedule = schedule.pushDown(i);
        sendUpdateMove(schedule);
        orders.setSelectedIndex(i + 1);
    }

    private void ordersMouseClicked(MouseEvent evt) {
        int i = orders.getSelectedIndex();
        ScheduleState schedule = getSchedule();
        if (i >= schedule.getNumberOfOrders()) {
            // The selected index does not exist!
            // For some reason, the JList hasn't updated yet.
            i = -1;
        }
        if (-1 != i && MouseEvent.BUTTON3 == evt.getButton()) {
            // If an element is select and the right button is pressed.
            TrainOrder order = schedule.getOrder(i);
            pullUpJMenuItem.setEnabled(schedule.canPullUp(i));
            pushDownJMenuItem.setEnabled(schedule.canPushDown(i));
            gotoStationJMenuItem.setEnabled(schedule.canSetGotoStation(i));
            removeWagonsJMenu.setEnabled(order.orderHasWagons());
            waitJMenu.setEnabled(order.orderHasWagons());
            addWagonJMenu.setEnabled(order.hasLessThanMaximumNumberOfWagons());
            setupWagonsPopup();
            editOrderJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    private void pullUpJMenuItemActionPerformed(ActionEvent evt) {
        ScheduleState schedule = getSchedule();
        int i = orders.getSelectedIndex();
        schedule = schedule.pullUp(i);
        sendUpdateMove(schedule);
        orders.setSelectedIndex(i - 1);
    }

    @Override
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        trainOrderPanel1.setup(modelRoot, rendererRoot, null);
        this.modelRoot = modelRoot;
        vl = rendererRoot;

        // This actionListener is fired by the select station popup when a
        // station is selected.
        Action action = new AbstractAction() {

            @Serial
            private static final long serialVersionUID = -2096909872676721636L;

            @Override
            public void actionPerformed(ActionEvent e) {
                sendUpdateMove(selectStationPanel1.generateNewSchedule());
                selectStationJPopupMenu.setVisible(false);
                listModel.fireRefresh();
                orders.requestFocus();
            }
        };
        selectStationPanel1.setup(modelRoot, rendererRoot, action);
    }

    public void display(int newTrainNumber) {
        trainId = newTrainNumber;
        Player player = modelRoot.getPlayer();
        UnmodifiableWorld world = modelRoot.getWorld();
        Train train = world.getTrain(player, newTrainNumber);
        listModel = new TrainOrdersListModel(world, newTrainNumber, player);
        orders.setModel(listModel);
        orders.setFixedCellWidth(250);
        listModel.fireRefresh();
        enableButtons();
    }

    private void enableButtons() {
        ScheduleState schedule = getSchedule();
        addStationJButton.setEnabled(schedule.canAddOrder());

        urgentStopJButton.setEnabled(schedule.canAddUrgentOrder());
    }

    private ScheduleState getSchedule() {
        Player player = modelRoot.getPlayer();
        UnmodifiableWorld world = modelRoot.getWorld();
        Train train = world.getTrain(player, trainId);
        UnmodifiableSchedule unmodifiableSchedule = train.getSchedule();
        // TODO really want to copy the ScheduleState here?
        return new ScheduleState(unmodifiableSchedule);
    }

    /**
     * Since stations can be removed, we should not assume that station 0
     * exists: this method returns the id of the first station that exists.
     */
    private int getFirstStationID() {
        Collection<Station> stations = modelRoot.getWorld().getStations(modelRoot.getPlayer());
        if (stations.isEmpty()) {
            throw new NoSuchElementException();
        }
        return stations.iterator().next().getId();
    }

    private void setupWagonsPopup() {
        addWagonJMenu.removeAll(); // Remove existing menu items.

        for (Cargo cargo : modelRoot.getWorld().getCargos()) {
            JMenuItem wagonMenuItem = new JMenuItem();
            final int cargoTypeId = cargo.getId();
            wagonMenuItem.setText(cargo.getName());
            Image image = vl.getWagonImages(cargoTypeId).getSideOnImage();
            int height = image.getHeight(null);
            int width = image.getWidth(null);
            int scale = height / 10;
            Icon icon = new ImageIcon(image.getScaledInstance(width / scale, height / scale, Image.SCALE_FAST));
            wagonMenuItem.setIcon(icon);
            wagonMenuItem.addActionListener(e -> addWagon(cargoTypeId));
            addWagonJMenu.add(wagonMenuItem);
        }
    }

    private void noChange() {
        TrainOrder oldOrders, newOrders;
        ScheduleState schedule = getSchedule();
        int orderNumber = orders.getSelectedIndex();
        oldOrders = schedule.getOrder(orderNumber);
        newOrders = oldOrders.withPolicy(TrainOrder.LoadingPolicy.KEEP_CONSIST, List.of(), false);
        schedule = schedule.setOrder(orderNumber, newOrders);
        sendUpdateMove(schedule);
    }

    private void setWaitUntilFull(boolean b) {
        TrainOrder oldOrders, newOrders;
        ScheduleState schedule = getSchedule();
        int orderNumber = orders.getSelectedIndex();
        oldOrders = schedule.getOrder(orderNumber);
        // If auto-consist is set do nothing
        if (oldOrders.isAutoConsist() || !oldOrders.hasSpecifiedConsist()) return;
        newOrders = oldOrders.withPolicy(TrainOrder.LoadingPolicy.SPECIFIED_CONSIST,
                oldOrders.getConsist(), b);
        schedule = schedule.setOrder(orderNumber, newOrders);
        sendUpdateMove(schedule);
    }

    private void setAutoConsist() {
        TrainOrder oldOrders, newOrders;
        ScheduleState schedule = getSchedule();
        int orderNumber = orders.getSelectedIndex();
        oldOrders = schedule.getOrder(orderNumber);
        newOrders = oldOrders.withPolicy(TrainOrder.LoadingPolicy.AUTO_CONSIST, List.of(), false);
        schedule = schedule.setOrder(orderNumber, newOrders);
        sendUpdateMove(schedule);
    }

    private void addWagon(int wagonTypeNumber) {
        TrainOrder oldOrders, newOrders;
        ScheduleState schedule = getSchedule();
        int orderNumber = orders.getSelectedIndex();
        oldOrders = schedule.getOrder(orderNumber);
        Integer[] newConsist;
        if (oldOrders.hasSpecifiedConsist()) {
            int oldLength = oldOrders.getConsist().size();
            newConsist = new Integer[oldLength + 1];
            // Copy existing wagons
            for (int i = 0; i < oldLength; i++) {
                newConsist[i] = oldOrders.getConsist().get(i);
            }
            // Then add specified wagon.
            newConsist[oldLength] = wagonTypeNumber;
        } else {
            newConsist = new Integer[]{wagonTypeNumber};
        }
        newOrders = oldOrders.withPolicy(TrainOrder.LoadingPolicy.SPECIFIED_CONSIST,
                Arrays.asList(newConsist), oldOrders.isWaitUntilFull());
        schedule = schedule.setOrder(orderNumber, newOrders);
        sendUpdateMove(schedule);
    }

    private void removeAllWagons() {
        TrainOrder oldOrders, newOrders;
        ScheduleState schedule = getSchedule();
        int orderNumber = orders.getSelectedIndex();
        oldOrders = schedule.getOrder(orderNumber);
        newOrders = oldOrders.withPolicy(TrainOrder.LoadingPolicy.SPECIFIED_CONSIST, List.of(), false);
        schedule = schedule.setOrder(orderNumber, newOrders);
        sendUpdateMove(schedule);
    }

    private void removeLastWagon() {
        // TODO should the code be in the model
        TrainOrder oldOrders, newOrders;
        ScheduleState schedule = getSchedule();
        int orderNumber = orders.getSelectedIndex();
        oldOrders = schedule.getOrder(orderNumber);
        if (!oldOrders.hasSpecifiedConsist()) {
            return;
        }
        List<Integer> oldConsist = oldOrders.getConsist();
        int newLength = oldConsist.size() - 1;
        if (newLength < 0) {
            // No wagons to remove!
            return;
        }
        List<Integer> newConsist = new ArrayList<>(oldOrders.getConsist());
        newConsist.removeLast();

        newOrders = oldOrders.withPolicy(TrainOrder.LoadingPolicy.SPECIFIED_CONSIST,
                newConsist, oldOrders.isWaitUntilFull());
        schedule = schedule.setOrder(orderNumber, newOrders);
        sendUpdateMove(schedule);
    }

    private void sendUpdateMove(ScheduleState schedule) {
        Move move = new UpdateTrainMove(modelRoot.getPlayer(), trainId, null, null, schedule);
        modelRoot.applyMove(move);
    }

    // TODO updates are out of order with the removal of ChangeInListMoves, need to listen differently to changes

    /**
     * Show the popup that lets the user select a station, called when a new
     * scheduled stop is added and when an existing scheduled stop is changed.
     */
    private void showSelectStation(ScheduleState schedule, int orderNumber) {
        selectStationPanel1.display(schedule, orderNumber);

        // Show the select station popup in the middle of the window.
        Container topLevelAncestor = getTopLevelAncestor();
        Dimension d = topLevelAncestor.getSize();
        Dimension d2 = selectStationJPopupMenu.getPreferredSize();
        int x = Math.max((d.width - d2.width) / 2, 0);
        int y = Math.max((d.height - d2.height) / 2, 0);
        selectStationJPopupMenu.show(topLevelAncestor, x, y);
        selectStationPanel1.requestFocus();
    }

}
