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

package freerails.model.train.schedule;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Immutable train schedule state containing orders and the next scheduled stop.
 */
public final class ScheduleState implements UnmodifiableSchedule {
    @Serial
    private static final long serialVersionUID = 7943055418460440482L;
    public static final UnmodifiableSchedule EMPTY = new ScheduleState();

    private final State state;

    private record State(List<TrainOrder> orders, int nextScheduledOrder, boolean hasPriorityOrders)
            implements Serializable {
        private State {
            orders = List.copyOf(orders);
        }
    }

    /** Creates an empty schedule with no scheduled destination. */
    public ScheduleState() {
        this(List.of(), -1, false);
    }

    /**
     * Creates a copy of a read-only schedule.
     *
     * @param schedule schedule whose order state is copied
     */
    public ScheduleState(UnmodifiableSchedule schedule) {
        this(copyOrders(schedule), schedule.getNextScheduledOrder(), schedule.hasPriorityOrders());
    }

    /**
     * @param orders schedule orders in index order
     * @param gotoStation index of the next scheduled order, or {@code -1} if none
     * @param hasPriorityOrders whether order zero is a priority order
     */
    public ScheduleState(TrainOrder[] orders, int gotoStation, boolean hasPriorityOrders) {
        this(Arrays.asList(orders.clone()), gotoStation, hasPriorityOrders);
    }

    private ScheduleState(List<TrainOrder> orders, int nextScheduledOrder, boolean hasPriorityOrders) {
        this.state = new State(orders, nextScheduledOrder, hasPriorityOrders);
    }

    private List<TrainOrder> orders() {
        return state.orders();
    }

    private int nextScheduledOrder() {
        return state.nextScheduledOrder();
    }

    private static List<TrainOrder> copyOrders(UnmodifiableSchedule schedule) {
        List<TrainOrder> orders = new ArrayList<>(schedule.getNumberOfOrders());
        for (int i = 0; i < schedule.getNumberOfOrders(); i++) {
            orders.add(schedule.getOrder(i));
        }
        return orders;
    }

    /**
     * Returns a schedule with the specified priority order.
     *
     * @param order new priority order
     * @return the updated schedule
     */
    public ScheduleState setPriorityOrders(TrainOrder order) {
        List<TrainOrder> updatedOrders = new ArrayList<>(orders());
        int updatedNextOrder = nextScheduledOrder();
        if (hasPriorityOrders()) {
            updatedOrders.set(PRIORITY_ORDERS, order);
        } else {
            updatedOrders.add(PRIORITY_ORDERS, order);
            if (updatedNextOrder >= 0) {
                updatedNextOrder++;
            }
        }
        return new ScheduleState(updatedOrders, updatedNextOrder, true);
    }

    /**
     * Returns a schedule with the order at the specified position removed.
     * Removing an index beyond the end of the schedule leaves it unchanged.
     *
     * @param orderNumber index of the order to remove
     * @return the updated schedule
     */
    public ScheduleState removeOrder(int orderNumber) {
        if (orderNumber >= orders().size()) {
            return this;
        }

        List<TrainOrder> updatedOrders = new ArrayList<>(orders());
        boolean updatedHasPriority = hasPriorityOrders();
        if (PRIORITY_ORDERS == orderNumber && updatedHasPriority) {
            updatedHasPriority = false;
        }
        updatedOrders.remove(orderNumber);

        int updatedNextOrder = nextScheduledOrder();
        if (updatedNextOrder > orderNumber) {
            updatedNextOrder--;
        }

        int firstScheduleStop = updatedHasPriority ? 1 : 0;
        if (updatedOrders.size() <= updatedNextOrder) {
            updatedNextOrder = firstScheduleStop;
        }
        if (updatedOrders.size() - firstScheduleStop == 0) {
            updatedNextOrder = -1;
        }

        return new ScheduleState(updatedOrders, updatedNextOrder, updatedHasPriority);
    }

    /** Returns the first index reserved for ordinary scheduled stops. */
    private int firstScheduleStop() {
        return hasPriorityOrders() ? 1 : 0;
    }

    /** Returns the number of ordinary scheduled stops, excluding a priority order. */
    private int numberOfScheduledStops() {
        return orders().size() - firstScheduleStop();
    }

    /**
     * Returns a schedule with an ordinary order inserted at the specified position.
     *
     * @param orderNumber index at which to insert the order
     * @param order order to insert
     * @return the updated schedule
     * @throws IllegalArgumentException if the index is outside the ordinary-order range
     * @throws IllegalStateException if the schedule already contains the maximum number of orders
     */
    public ScheduleState addOrder(int orderNumber, TrainOrder order) {
        if (orderNumber < firstScheduleStop() || orderNumber > orders().size()) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }
        if (!canAddOrder()) {
            throw new IllegalStateException();
        }

        List<TrainOrder> updatedOrders = new ArrayList<>(orders());
        updatedOrders.add(orderNumber, order);

        int updatedNextOrder = nextScheduledOrder();
        if (updatedNextOrder >= orderNumber) {
            updatedNextOrder++;
        }
        if (-1 == updatedNextOrder && 0 < updatedOrders.size() - firstScheduleStop()) {
            updatedNextOrder = firstScheduleStop();
        }
        return new ScheduleState(updatedOrders, updatedNextOrder, hasPriorityOrders());
    }

    /**
     * Returns a schedule with the order appended.
     *
     * @param order order to append
     * @return the updated schedule
     * @throws IllegalStateException if the schedule already contains the maximum number of orders
     */
    public ScheduleState addOrder(TrainOrder order) {
        return addOrder(orders().size(), order);
    }

    /**
     * Returns a schedule with an order replaced, or appended when the index is beyond its end.
     *
     * @param orderNumber index to replace, or the end index to append at
     * @param order order to set
     * @return the updated schedule
     */
    public ScheduleState setOrder(int orderNumber, TrainOrder order) {
        List<TrainOrder> updatedOrders = new ArrayList<>(orders());
        if (orderNumber >= updatedOrders.size()) {
            updatedOrders.add(order);
        } else {
            updatedOrders.set(orderNumber, order);
        }
        return new ScheduleState(updatedOrders, nextScheduledOrder(), hasPriorityOrders());
    }

    /**
     * @param index zero-based order index
     * @return the order at {@code index}
     */
    @Override
    public TrainOrder getOrder(int index) {
        return orders().get(index);
    }

    /** Returns the index of the order currently being carried out. */
    @Override
    public int getCurrentOrderIndex() {
        return hasPriorityOrders() ? 0 : nextScheduledOrder();
    }

    /**
     * Returns a schedule whose next ordinary stop is the specified index.
     *
     * @param orderNumber index of the order to visit next
     * @return the updated schedule
     * @throws IllegalArgumentException if the index is outside the schedule
     */
    public ScheduleState setOrderToGoto(int orderNumber) {
        if (!canSetGotoStation(orderNumber)) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }
        return new ScheduleState(orders(), orderNumber, hasPriorityOrders());
    }

    /** Returns the station identifier for the current order, or {@code -1} if none. */
    @Override
    public int getNextStationId() {
        int orderToGoto = getCurrentOrderIndex();
        return orderToGoto == -1 ? -1 : orders().get(orderToGoto).getStationId();
    }

    /**
     * Returns a schedule advanced to the next ordinary stop, wrapping at the end.
     * A priority order is removed when it has been visited, without advancing the ordinary stop.
     *
     * @return the updated schedule
     */
    public ScheduleState gotoNextStation() {
        if (hasPriorityOrders()) {
            return removeOrder(PRIORITY_ORDERS);
        }
        if (numberOfScheduledStops() == 0) {
            return this;
        }

        int updatedNextOrder = nextScheduledOrder() + 1;
        if (orders().size() <= updatedNextOrder) {
            updatedNextOrder = firstScheduleStop();
        }
        return new ScheduleState(orders(), updatedNextOrder, hasPriorityOrders());
    }

    /** Returns whether the schedule currently has a priority order. */
    @Override
    public boolean hasPriorityOrders() {
        return state.hasPriorityOrders();
    }

    /** Returns the total number of orders, including any priority order. */
    @Override
    public int getNumberOfOrders() {
        return orders().size();
    }

    /**
     * Returns whether the specified order can be moved one position toward the top.
     *
     * @param orderNumber order index
     * @return {@code true} if it can be pulled up without crossing the priority order
     */
    public boolean canPullUp(int orderNumber) {
        return orderNumber > 0
                && orderNumber < orders().size()
                && !(orderNumber == 1 && hasPriorityOrders());
    }

    /**
     * Returns whether the specified order can be moved one position toward the bottom.
     *
     * @param orderNumber order index
     * @return {@code true} if it can be pushed down
     */
    public boolean canPushDown(int orderNumber) {
        return orderNumber >= firstScheduleStop()
                && orderNumber < orders().size() - 1;
    }

    /**
     * Returns a schedule with the order moved one position toward the top.
     *
     * @param orderNumber index of the order to move
     * @return the updated schedule
     * @throws IllegalArgumentException if the order cannot be moved up
     */
    public ScheduleState pullUp(int orderNumber) {
        if (!canPullUp(orderNumber)) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }

        boolean isGoingToThisStation = nextScheduledOrder() == orderNumber;
        TrainOrder order = getOrder(orderNumber);
        ScheduleState updated = removeOrder(orderNumber).addOrder(orderNumber - 1, order);
        return isGoingToThisStation ? updated.setOrderToGoto(orderNumber - 1) : updated;
    }

    /**
     * Returns a schedule with the order moved one position toward the bottom.
     *
     * @param orderNumber index of the order to move
     * @return the updated schedule
     * @throws IllegalArgumentException if the order cannot be moved down
     */
    public ScheduleState pushDown(int orderNumber) {
        if (!canPushDown(orderNumber)) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }

        boolean isGoingToThisStation = nextScheduledOrder() == orderNumber;
        TrainOrder order = getOrder(orderNumber);
        ScheduleState updated = removeOrder(orderNumber).addOrder(orderNumber + 1, order);
        return isGoingToThisStation ? updated.setOrderToGoto(orderNumber + 1) : updated;
    }

    /** Returns whether another order can be added without exceeding the schedule limit. */
    public boolean canAddOrder() {
        int max = hasPriorityOrders() ? MAXIMUM_NUMBER_OF_ORDER + 1 : MAXIMUM_NUMBER_OF_ORDER;
        return max > getNumberOfOrders();
    }

    /**
     * Returns whether the specified order can be selected as the next stop.
     *
     * @param orderNumber index of the candidate order
     * @return {@code false} if the index is outside the ordinary orders
     */
    public boolean canSetGotoStation(int orderNumber) {
        return orderNumber >= firstScheduleStop() && orderNumber < orders().size();
    }

    /** Returns the index of the next ordinary scheduled order. */
    @Override
    public int getNextScheduledOrder() {
        return nextScheduledOrder();
    }

    /**
     * Returns whether any order visits the specified station.
     *
     * @param stationId station identifier
     * @return {@code true} if an order targets that station
     */
    @Override
    public boolean stopsAtStation(int stationId) {
        for (TrainOrder order : orders()) {
            if (order.getStationId() == stationId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a schedule with every order targeting the specified station removed.
     *
     * @param stationNumber station identifier
     * @return the updated schedule
     */
    public ScheduleState removeAllStopsAtStation(int stationNumber) {
        ScheduleState updated = this;
        int index = 0;
        while (index < updated.getNumberOfOrders()) {
            if (updated.getOrder(index).getStationId() == stationNumber) {
                updated = updated.removeOrder(index);
            } else {
                index++;
            }
        }
        return updated;
    }

    /**
     * Returns whether the current order requests automatic consist selection.
     *
     * @throws IllegalStateException if the schedule has no current order
     */
    private TrainOrder getCurrentOrder() {
        int currentOrderIndex = getCurrentOrderIndex();
        if (currentOrderIndex == -1) {
            throw new IllegalStateException("Schedule has no current order");
        }
        return orders().get(currentOrderIndex);
    }

    @Serial
    private Object readResolve() {
        return new ScheduleState(orders(), nextScheduledOrder(), hasPriorityOrders());
    }

    /** Compares the orders, current destination index, and priority-order state. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ScheduleState schedule)) {
            return false;
        }
        return hasPriorityOrders() == schedule.hasPriorityOrders()
                && nextScheduledOrder() == schedule.nextScheduledOrder()
                && orders().equals(schedule.orders());
    }

    /** Returns a hash code based on all schedule state. */
    @Override
    public int hashCode() {
        return Objects.hash(orders(), nextScheduledOrder(), hasPriorityOrders());
    }
}
