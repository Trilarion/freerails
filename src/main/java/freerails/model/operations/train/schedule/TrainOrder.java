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
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/** Immutable stop instruction for a train. */
public final class TrainOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final int MAXIMUM_NUMBER_OF_WAGONS = 6;

    /** Loading and consist behavior to apply at a stop. */
    public enum LoadingPolicy {
        KEEP_CONSIST,
        SPECIFIED_CONSIST,
        AUTO_CONSIST
    }

    private final LoadingPolicy loadingPolicy;
    private final List<Integer> consist;
    private final boolean waitUntilFull;
    private final boolean oneShot;
    private final int stationId;

    private TrainOrder(int stationId, LoadingPolicy loadingPolicy, List<Integer> consist,
                       boolean waitUntilFull, boolean oneShot) {
        this.loadingPolicy = Objects.requireNonNull(loadingPolicy);
        this.consist = List.copyOf(consist);
        if (loadingPolicy != LoadingPolicy.SPECIFIED_CONSIST && !this.consist.isEmpty()) {
            throw new IllegalArgumentException("Only a specified-consist stop can contain wagon types.");
        }
        if (loadingPolicy == LoadingPolicy.AUTO_CONSIST && waitUntilFull) {
            throw new IllegalArgumentException("An auto-consist stop cannot wait until full.");
        }
        this.waitUntilFull = loadingPolicy == LoadingPolicy.SPECIFIED_CONSIST
                && !this.consist.isEmpty() && waitUntilFull;
        this.oneShot = oneShot;
        this.stationId = stationId;
    }

    public static TrainOrder keepConsist(int stationId) {
        return new TrainOrder(stationId, LoadingPolicy.KEEP_CONSIST, List.of(), false, false);
    }

    public static TrainOrder specifiedConsist(int stationId, List<Integer> consist, boolean waitUntilFull) {
        return new TrainOrder(stationId, LoadingPolicy.SPECIFIED_CONSIST, consist, waitUntilFull, false);
    }

    public static TrainOrder autoConsist(int stationId) {
        return new TrainOrder(stationId, LoadingPolicy.AUTO_CONSIST, List.of(), false, false);
    }

    /** Returns an otherwise identical stop that is removed after it is visited. */
    public TrainOrder asOneShot() {
        return oneShot ? this : new TrainOrder(stationId, loadingPolicy, consist, waitUntilFull, true);
    }

    /** Returns this stop with a new station and the same policy and one-shot status. */
    public TrainOrder withStation(int newStationId) {
        return new TrainOrder(newStationId, loadingPolicy, consist, waitUntilFull, oneShot);
    }

    /** Returns this stop with a new policy and the same station and one-shot status. */
    public TrainOrder withPolicy(LoadingPolicy newPolicy, List<Integer> newConsist, boolean newWaitUntilFull) {
        return new TrainOrder(stationId, newPolicy, newConsist, newWaitUntilFull, oneShot);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TrainOrder other)) return false;
        return stationId == other.stationId
                && waitUntilFull == other.waitUntilFull
                && oneShot == other.oneShot
                && loadingPolicy == other.loadingPolicy
                && consist.equals(other.consist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stationId, loadingPolicy, consist, waitUntilFull, oneShot);
    }

    public List<Integer> getConsist() {
        return consist;
    }

    public LoadingPolicy getLoadingPolicy() {
        return loadingPolicy;
    }

    public boolean hasSpecifiedConsist() {
        return loadingPolicy == LoadingPolicy.SPECIFIED_CONSIST;
    }

    public boolean orderHasWagons() {
        return hasSpecifiedConsist() && !consist.isEmpty();
    }

    public boolean hasLessThanMaximumNumberOfWagons() {
        return consist.size() < MAXIMUM_NUMBER_OF_WAGONS;
    }

    public boolean isAutoConsist() {
        return loadingPolicy == LoadingPolicy.AUTO_CONSIST;
    }

    public boolean isWaitUntilFull() {
        return waitUntilFull;
    }

    public boolean isOneShot() {
        return oneShot;
    }

    public int getStationId() {
        return stationId;
    }
}
