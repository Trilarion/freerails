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
import freerails.model.operations.train.Train;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;

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
     * Creates a train orders list model instance.
     *
     * @param world the game world being queried
     * @param trainNumber the train number parameter
     * @param player the player whose game state is used
     */
    public TrainOrdersListModel(UnmodifiableWorld world, int trainNumber, Player player) {
        this.trainNumber = trainNumber;
        this.world = world;
        this.player = player;
        assert null != getSchedule();
    }

    /**
     * Returns the element at.
     * @param index the index
     * @return the element at
     */
    @Override
    public TrainOrdersListElement getElementAt(int index) {
        UnmodifiableSchedule schedule = getSchedule();
        int gotoStatus;

        gotoStatus = schedule.getCurrentOrderIndex() == index ? GOTO_NOW : DONT_GOTO;
        TrainOrder order = schedule.getOrder(index);

        return new TrainOrdersListElement(order.isOneShot(), gotoStatus, order, trainNumber);
    }

    /**
     * Returns the size.
     * @return the size
     */
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
     * Performs the fire refresh operation.
     */
    public void fireRefresh() {
        super.fireContentsChanged(this, 0, getSize());
    }

    /**
     * Returns the schedule.
     * @return the schedule
     */
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
         * Creates a train orders list element instance.
         *
         * @param isUrgentStop the is urgent stop parameter
         * @param gotoStatus the goto status parameter
         * @param order the order parameter
         * @param trainNumber the train number parameter
         */
        private TrainOrdersListElement(boolean isUrgentStop, int gotoStatus, TrainOrder order, int trainNumber) {
            this.isUrgentStop = isUrgentStop;
            this.gotoStatus = gotoStatus;
            this.order = order;
            this.trainNumber = trainNumber;
        }
    }
}