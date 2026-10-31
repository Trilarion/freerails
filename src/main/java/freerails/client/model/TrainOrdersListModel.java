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
 * TrainOrdersListModel.java
 *
 */
package freerails.client.model;

import freerails.client.view.TrainSchedulePanel;
import freerails.model.train.Train;
import freerails.model.train.schedule.TrainOrder;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.train.schedule.UnmodifiableSchedule;

import javax.swing.*;
import java.io.Serial;

/**
 * AbstractListModel used by {@link TrainSchedulePanel} to display the orders
 * making up a train schedule.
 */
public class TrainOrdersListModel extends AbstractListModel<TrainOrdersListModel.TrainOrdersListElement> {

    public static final int DONT_GOTO = 0;
    public static final int GOTO_NOW = 1;

    @Serial
    private static final long serialVersionUID = 3762537827703009847L;
    private final int trainNumber;
    private final UnmodifiableWorld world;
    private final Player player;

    /**
     * @param world
     * @param trainNumber
     * @param player
     */
    public TrainOrdersListModel(UnmodifiableWorld world, int trainNumber, Player player) {
        this.trainNumber = trainNumber;
        this.world = world;
        this.player = player;
        assert null != getSchedule();
    }

    @Override
    public TrainOrdersListElement getElementAt(int index) {
        UnmodifiableSchedule schedule = getSchedule();
        int gotoStatus;

        gotoStatus = schedule.getCurrentOrderIndex() == index ? GOTO_NOW : DONT_GOTO;
        TrainOrder order = schedule.getOrder(index);

        return new TrainOrdersListElement(order.isOneShot(), gotoStatus, order, trainNumber);
    }

    @Override
    public int getSize() {
        UnmodifiableSchedule schedule = getSchedule();
        int size = 0;
        if (schedule != null) {
            size = schedule.getNumberOfOrders();
        }
        return size;
    }

    /**
     *
     */
    public void fireRefresh() {
        super.fireContentsChanged(this, 0, getSize());
    }

    private UnmodifiableSchedule getSchedule() {
        Train train = world.getTrain(player, trainNumber);
        return train.getSchedule();
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
        public final boolean isUrgentStop;

        /**
         *
         */
        public final int gotoStatus;

        /**
         *
         */
        public final TrainOrder order;

        /**
         *
         */
        public final int trainNumber;

        /**
         * @param isUrgentStop
         * @param gotoStatus
         * @param order
         * @param trainNumber
         */
        private TrainOrdersListElement(boolean isUrgentStop, int gotoStatus, TrainOrder order, int trainNumber) {
            this.isUrgentStop = isUrgentStop;
            this.gotoStatus = gotoStatus;
            this.order = order;
            this.trainNumber = trainNumber;
        }
    }
}