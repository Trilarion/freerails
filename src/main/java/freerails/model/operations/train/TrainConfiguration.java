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

package freerails.model.operations.train;

import freerails.model.ModelConstants;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Immutable engine and wagon configuration for a train.
 */
public record TrainConfiguration(int engineId, List<Integer> wagonTypes) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a configuration with the specified engine and ordered wagon types.
     *
     * @param engineId   engine type identifier
     * @param wagonTypes wagon type identifiers in consist order
     */
    public TrainConfiguration(int engineId, @NotNull List<Integer> wagonTypes) {
        this.engineId = engineId;
        this.wagonTypes = List.copyOf(wagonTypes);
    }

    /**
     * Returns the engine type identifier.
     */
    @Override
    public int engineId() {
        return engineId;
    }

    /**
     * Returns an immutable snapshot of the wagon type identifiers.
     */
    @Override
    public List<Integer> wagonTypes() {
        return wagonTypes;
    }

    /**
     * Returns the number of wagons in the consist.
     */
    public int getNumberOfWagons() {
        return wagonTypes.size();
    }

    /**
     * Returns the wagon type at the specified consist position.
     *
     * @param position zero-based wagon position
     * @return wagon type identifier
     * @throws IndexOutOfBoundsException if the position is outside the consist
     */
    public int getWagonType(int position) {
        return wagonTypes.get(position);
    }

    /**
     * Returns the train length in map-distance units, including its engine.
     */
    public int getLength() {
        return (1 + wagonTypes.size()) * ModelConstants.WAGON_LENGTH;
    }

    /**
     * Returns a configuration with a different engine and the same wagons.
     */
    public TrainConfiguration withEngineId(int newEngineId) {
        return new TrainConfiguration(newEngineId, wagonTypes);
    }

    /**
     * Returns a configuration with different wagons and the same engine.
     */
    public TrainConfiguration withWagonTypes(@NotNull List<Integer> newWagonTypes) {
        return new TrainConfiguration(engineId, newWagonTypes);
    }

    /**
     * Returns whether this object is equal to the specified object.
     *
     * @param obj the object to compare with this object
     * @return true if this object is equal to the specified object, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TrainConfiguration(int id, List<Integer> types))) {
            return false;
        }
        return engineId == id && wagonTypes.equals(types);
    }

}
