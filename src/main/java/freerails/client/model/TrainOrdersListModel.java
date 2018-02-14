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
 * TrainOrdersListModel.java
 *
 */
package freerails.client.model;

import freerails.client.view.TrainSchedulePanel;
import freerails.world.KEY;
import freerails.world.world.ReadOnlyWorld;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.train.schedule.ImmutableSchedule;
import freerails.world.train.schedule.Schedule;
import freerails.world.train.TrainModel;
import freerails.world.train.TrainOrders;

import javax.swing.*;

/**
 * AbstractListModel used by {@link TrainSchedulePanel} to display the orders
 * making up a train schedule.
 */
public class TrainOrdersListModel extends AbstractListModel {

    public static final int DONT_GOTO = 0;
    public static final int GOTO_NOW = 1;
    public static final int GOTO_AFTER_PRIORITY_ORDERS = 2;

    private static final long serialVersionUID = 3762537827703009847L;
    private final int trainNumber;
    private final ReadOnlyWorld world;
    private final FreerailsPrincipal principal;

    /**
     * @param world
     * @param trainNumber
     * @param p
     */
    public TrainOrdersListModel(ReadOnlyWorld world, int trainNumber, FreerailsPrincipal p) {
        this.trainNumber = trainNumber;
        this.world = world;
        principal = p;
        assert (null != getSchedule());
    }

    public Object getElementAt(int index) {
        Schedule s = getSchedule();
        int gotoStatus;

        if (s.getNextScheduledOrder() == index) {
            if (s.hasPriorityOrders()) {
                gotoStatus = GOTO_AFTER_PRIORITY_ORDERS;
            } else {
                gotoStatus = GOTO_NOW;
            }
        } else {
            if (s.hasPriorityOrders() && 0 == index) {
                // These orders are the priority orders.
                gotoStatus = GOTO_NOW;
            } else {
                gotoStatus = DONT_GOTO;
            }
        }

        boolean isPriorityOrders = 0 == index && s.hasPriorityOrders();
        TrainOrders order = getSchedule().getOrder(index);

        return new TrainOrdersListElement(isPriorityOrders, gotoStatus, order, trainNumber);
    }

    public int getSize() {
        Schedule s = getSchedule();
        int size = 0;
        if (s != null) {
            size = s.getNumOrders();
        }
        return size;
    }

    /**
     *
     */
    public void fireRefresh() {
        super.fireContentsChanged(this, 0, getSize());
    }

    private Schedule getSchedule() {
        TrainModel train = (TrainModel) world.get(principal, KEY.TRAINS, trainNumber);
        ImmutableSchedule sched = null;
        if (train != null) {
            sched = (ImmutableSchedule) world.get(principal, KEY.TRAIN_SCHEDULES, train.getScheduleID());
        }
        return sched;
    }

    /**
     * Holds the values that are needed by the ListCellRender.
     * TrainOrdersListModel.getElementAt(int index) returns an instance of this
     * class.
     */
    public static class TrainOrdersListElement {

        /**
         *
         */
        public final boolean isPriorityOrder;

        /**
         *
         */
        public final int gotoStatus;

        /**
         *
         */
        public final TrainOrders order;

        /**
         *
         */
        public final int trainNumber;

        /**
         * @param isPriorityOrder
         * @param gotoStatus
         * @param order
         * @param trainNumber
         */
        private TrainOrdersListElement(boolean isPriorityOrder, int gotoStatus, TrainOrders order, int trainNumber) {
            this.isPriorityOrder = isPriorityOrder;
            this.gotoStatus = gotoStatus;
            this.order = order;
            this.trainNumber = trainNumber;
        }
    }
}