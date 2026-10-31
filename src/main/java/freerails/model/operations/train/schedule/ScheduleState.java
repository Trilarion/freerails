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

package freerails.model.operations.train.schedule;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Immutable train schedule containing an ordered route and its current stop. */
public final class ScheduleState implements UnmodifiableSchedule {
    @Serial
    private static final long serialVersionUID = 1L;
    public static final UnmodifiableSchedule EMPTY = new ScheduleState();
    private static final int MAXIMUM_ONE_SHOT_STOPS = 1;

    private record State(List<TrainOrder> orders, int currentOrderIndex) implements java.io.Serializable {
        private State {
            orders = List.copyOf(orders);
        }
    }

    private final State state;

    public ScheduleState() {
        this(List.of(), -1);
    }

    public ScheduleState(UnmodifiableSchedule schedule) {
        this(copyOrders(schedule), schedule.getCurrentOrderIndex());
    }

    public ScheduleState(TrainOrder[] orders, int currentOrderIndex) {
        this(Arrays.asList(orders.clone()), currentOrderIndex);
    }

    private ScheduleState(List<TrainOrder> orders, int currentOrderIndex) {
        validate(orders, currentOrderIndex);
        state = new State(orders, currentOrderIndex);
    }

    private List<TrainOrder> orders() {
        return state.orders();
    }

    private static List<TrainOrder> copyOrders(UnmodifiableSchedule schedule) {
        List<TrainOrder> orders = new ArrayList<>(schedule.getNumberOfOrders());
        for (int i = 0; i < schedule.getNumberOfOrders(); i++) {
            orders.add(schedule.getOrder(i));
        }
        return orders;
    }

    private static void validate(List<TrainOrder> orders, int currentOrderIndex) {
        int oneShotCount = 0;
        int regularCount = 0;
        for (TrainOrder order : orders) {
            Objects.requireNonNull(order);
            if (order.isOneShot()) {
                oneShotCount++;
            } else {
                regularCount++;
            }
        }
        if (oneShotCount > MAXIMUM_ONE_SHOT_STOPS || regularCount > MAXIMUM_NUMBER_OF_STOPS
                || orders.size() > MAXIMUM_NUMBER_OF_STOPS + MAXIMUM_ONE_SHOT_STOPS) {
            throw new IllegalArgumentException("Schedule exceeds its stop limit.");
        }
        if (currentOrderIndex < -1 || currentOrderIndex >= orders.size()
                || (orders.isEmpty() != (currentOrderIndex == -1))) {
            throw new IllegalArgumentException("Invalid current stop index: " + currentOrderIndex);
        }
    }

    /** Returns a schedule with an urgent one-shot stop inserted before the current stop. */
    public ScheduleState addUrgentOrder(TrainOrder order) {
        if (!canAddUrgentOrder()) {
            throw new IllegalStateException("An urgent stop already exists or the schedule is full.");
        }
        List<TrainOrder> updatedOrders = new ArrayList<>(orders());
        int insertionIndex = state.currentOrderIndex() == -1 ? updatedOrders.size() : state.currentOrderIndex();
        updatedOrders.add(insertionIndex, order.asOneShot());
        return new ScheduleState(updatedOrders, insertionIndex);
    }

    public boolean canAddUrgentOrder() {
        if (orders().stream().anyMatch(TrainOrder::isOneShot)) {
            return false;
        }
        long regularCount = orders().stream().filter(order -> !order.isOneShot()).count();
        return regularCount <= MAXIMUM_NUMBER_OF_STOPS;
    }

    /**
     * Removes the stop at the specified position.
     * Removing an index beyond the end of the schedule leaves it unchanged.
     */
    public ScheduleState removeOrder(int orderNumber) {
        if (orderNumber >= orders().size()) {
            return this;
        }
        List<TrainOrder> updatedOrders = new ArrayList<>(orders());
        updatedOrders.remove(orderNumber);
        int currentIndex = state.currentOrderIndex();
        if (updatedOrders.isEmpty()) {
            currentIndex = -1;
        } else if (currentIndex > orderNumber) {
            currentIndex--;
        } else if (currentIndex == orderNumber && currentIndex >= updatedOrders.size()) {
            currentIndex = 0;
        }
        return new ScheduleState(updatedOrders, currentIndex);
    }

    /** Returns a schedule with an ordinary stop inserted at the specified position. */
    public ScheduleState addOrder(int orderNumber, TrainOrder order) {
        if (order.isOneShot()) {
            throw new IllegalArgumentException("Use addUrgentOrder for a one-shot stop.");
        }
        if (orderNumber < 0 || orderNumber > orders().size()) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }
        if (!canAddOrder()) {
            throw new IllegalStateException("Schedule is full.");
        }
        List<TrainOrder> updatedOrders = new ArrayList<>(orders());
        updatedOrders.add(orderNumber, order);
        int currentIndex = state.currentOrderIndex();
        if (currentIndex == -1) {
            currentIndex = orderNumber;
        } else if (currentIndex >= orderNumber) {
            currentIndex++;
        }
        return new ScheduleState(updatedOrders, currentIndex);
    }

    public ScheduleState addOrder(TrainOrder order) {
        return addOrder(orders().size(), order);
    }

    /** Replaces a stop, preserving its one-shot marker. */
    public ScheduleState setOrder(int orderNumber, TrainOrder order) {
        if (orderNumber < 0 || orderNumber > orders().size()) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }
        if (orderNumber == orders().size()) {
            return addOrder(orderNumber, order);
        }
        List<TrainOrder> updatedOrders = new ArrayList<>(orders());
        TrainOrder previous = updatedOrders.get(orderNumber);
        if (previous.isOneShot() && !order.isOneShot()) {
            order = order.asOneShot();
        }
        updatedOrders.set(orderNumber, order);
        return new ScheduleState(updatedOrders, state.currentOrderIndex());
    }

    @Override
    public TrainOrder getOrder(int index) {
        return orders().get(index);
    }

    @Override
    public int getCurrentOrderIndex() {
        return state.currentOrderIndex();
    }

    @Override
    public int getNextStationId() {
        int index = getCurrentOrderIndex();
        return index == -1 ? -1 : orders().get(index).getStationId();
    }

    /** Selects the specified stop as the next stop to visit. */
    public ScheduleState setOrderToGoto(int orderNumber) {
        if (orderNumber < 0 || orderNumber >= orders().size()) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }
        return new ScheduleState(orders(), orderNumber);
    }

    /**
     * Completes the current stop. A one-shot stop is removed and the ordinary
     * stop that followed it becomes current; a regular stop advances cyclically.
     */
    public ScheduleState gotoNextStation() {
        int currentIndex = getCurrentOrderIndex();
        if (currentIndex == -1) {
            return this;
        }
        if (getOrder(currentIndex).isOneShot()) {
            return removeOrder(currentIndex);
        }
        int nextIndex = currentIndex + 1;
        if (nextIndex == orders().size()) {
            nextIndex = 0;
        }
        return new ScheduleState(orders(), nextIndex);
    }

    @Override
    public int getNumberOfOrders() {
        return orders().size();
    }

    public boolean canPullUp(int orderNumber) {
        return orderNumber > 0 && orderNumber < orders().size();
    }

    public boolean canPushDown(int orderNumber) {
        return orderNumber >= 0 && orderNumber < orders().size() - 1;
    }

    public ScheduleState pullUp(int orderNumber) {
        if (!canPullUp(orderNumber)) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }
        return moveOrder(orderNumber, orderNumber - 1);
    }

    public ScheduleState pushDown(int orderNumber) {
        if (!canPushDown(orderNumber)) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }
        return moveOrder(orderNumber, orderNumber + 1);
    }

    private ScheduleState moveOrder(int from, int to) {
        List<TrainOrder> updatedOrders = new ArrayList<>(orders());
        TrainOrder order = updatedOrders.remove(from);
        updatedOrders.add(to, order);
        int currentIndex = state.currentOrderIndex();
        if (currentIndex == from) {
            currentIndex = to;
        } else if (from < currentIndex && to >= currentIndex) {
            currentIndex--;
        } else if (from > currentIndex && to <= currentIndex) {
            currentIndex++;
        }
        return new ScheduleState(updatedOrders, currentIndex);
    }

    public boolean canAddOrder() {
        return orders().stream().filter(order -> !order.isOneShot()).count() < MAXIMUM_NUMBER_OF_STOPS;
    }

    public boolean canSetGotoStation(int orderNumber) {
        return orderNumber >= 0 && orderNumber < orders().size();
    }

    @Override
    public boolean stopsAtStation(int stationId) {
        return orders().stream().anyMatch(order -> order.getStationId() == stationId);
    }

    public ScheduleState removeAllStopsAtStation(int stationId) {
        ScheduleState updated = this;
        int index = 0;
        while (index < updated.getNumberOfOrders()) {
            if (updated.getOrder(index).getStationId() == stationId) {
                updated = updated.removeOrder(index);
            } else {
                index++;
            }
        }
        return updated;
    }

    @Serial
    private Object readResolve() {
        return new ScheduleState(orders(), state.currentOrderIndex());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ScheduleState schedule)) return false;
        return state.currentOrderIndex() == schedule.state.currentOrderIndex()
                && orders().equals(schedule.orders());
    }

    @Override
    public int hashCode() {
        return Objects.hash(orders(), state.currentOrderIndex());
    }
}
