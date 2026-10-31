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
 * TrainOrder.java
 *
 */
package freerails.model.train.schedule;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates the orders for a train.
 */
public class TrainOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 3616453397155559472L;
    private static final int MAXIMUM_NUMBER_OF_WAGONS = 6;
    private final ConsistPolicy consistPolicy;
    private final boolean waitUntilFull;
    private final boolean autoConsist;
    private final List<Integer> consist;
    private final int stationId;

    /** Whether an order leaves the consist unchanged or specifies its wagons. */
    public enum ConsistPolicy {
        UNCHANGED,
        EXPLICIT
    }

    /**
     * @param station destination station identifier
     * @param consistPolicy whether the consist is unchanged or explicitly specified
     * @param consist wagon type identifiers to use; may be empty for an explicitly empty consist
     * @param wait whether to wait at the station until the train is full
     * @param auto whether to automatically choose the consist
     */
    public TrainOrder(int station, ConsistPolicy consistPolicy, List<Integer> consist, boolean wait, boolean auto) {
        this.consistPolicy = Objects.requireNonNull(consistPolicy);
        this.consist = List.copyOf(consist);
        if (consistPolicy == ConsistPolicy.UNCHANGED && !this.consist.isEmpty()) {
            throw new IllegalArgumentException("An unchanged consist cannot contain wagon types.");
        }
        waitUntilFull = consistPolicy == ConsistPolicy.EXPLICIT && !this.consist.isEmpty() && wait;
        stationId = station;
        autoConsist = auto;
    }

    @Serial
    private Object readResolve() {
        if (consistPolicy != null && consist != null) {
            return this;
        }
        ConsistPolicy policy = consist == null ? ConsistPolicy.UNCHANGED : ConsistPolicy.EXPLICIT;
        List<Integer> restoredConsist = consist == null ? List.of() : consist;
        return new TrainOrder(stationId, policy, restoredConsist, waitUntilFull, autoConsist);
    }

    /** Compares all order settings. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TrainOrder trainOrder)) return false;

        if (isAutoConsist() != trainOrder.isAutoConsist()) return false;
        if (getStationId() != trainOrder.getStationId()) return false;
        if (isWaitUntilFull() != trainOrder.isWaitUntilFull()) return false;
        return consistPolicy == trainOrder.consistPolicy && consist.equals(trainOrder.consist);
    }

    /** Returns a hash code based on the order settings. */
    @Override
    public int hashCode() {
        return Objects.hash(isWaitUntilFull(), isAutoConsist(), consistPolicy, consist, getStationId());
    }

    /**
     * Returns the wagon types to use at this stop.
     *
     * @return an immutable list of wagon type identifiers, empty when none are specified
     */
    public List<Integer> getConsist() {
        return consist;
    }

    /** Returns whether the consist is unchanged or explicitly specified. */
    public ConsistPolicy getConsistPolicy() {
        return consistPolicy;
    }

    /** Returns whether this order explicitly specifies one or more wagons. */
    public boolean orderHasWagons() {
        return consistPolicy == ConsistPolicy.EXPLICIT && !consist.isEmpty();
    }

    /** Returns whether the consist has fewer than the supported maximum number of wagons. */
    public boolean hasLessThanMaximumNumberOfWagons() {
        return consist.size() < MAXIMUM_NUMBER_OF_WAGONS;
    }

    /** Returns whether the order uses automatic consist selection. */
    public boolean isAutoConsist() {
        return autoConsist;
    }

    /** Returns whether the train should wait until full at this stop. */
    public boolean isWaitUntilFull() {
        return waitUntilFull;
    }

    /** Returns the identifier of the station to visit. */
    public int getStationId() {
        return stationId;
    }
}