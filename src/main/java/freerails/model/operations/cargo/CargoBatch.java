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

package freerails.model.operations.cargo;

import freerails.model.simulation.Time;
import freerails.util.IntPoint2D;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a cargo batch. Cargo of the same batch is cargo of
 * the same type that was produced at the same location and at the same time.
 * A batch stores its cargo type's stable ID rather than a {@link Cargo}
 * instance because cargo definitions are owned by the world and resolved
 * there by ID.
 */
public class CargoBatch implements Serializable, Comparable<CargoBatch> {

    @Serial
    private static final long serialVersionUID = 3257006557605540149L;
    private final int cargoTypeId;
    private final IntPoint2D sourceP;
    private final int originalStationId;
    private final Time creationTime;

    /**
     * Creates a batch identity for cargo produced at a particular place and
     * time.
     *
     * @param cargoTypeId identifier of the cargo type
     * @param p source tile coordinates
     * @param time simulation time when this batch was created
     * @param origin identifier of the originating station
     */
    public CargoBatch(int cargoTypeId, IntPoint2D p, Time time, int origin) {
        this.cargoTypeId = cargoTypeId;
        sourceP = p;
        creationTime = time;
        originalStationId = origin;
    }

    /**
     * Returns the simulation time when this batch was created.
     *
     * @return batch creation time
     */
    public Time getCreationTime() {
        return creationTime;
    }

    /**
     * Returns the identifier of the station where this batch originated.
     *
     * @return originating station identifier
     */
    public int getOriginalStationId() {
        return originalStationId;
    }

    /**
     * Returns the identifier of the cargo type in this batch.
     *
     * @return cargo type identifier
     */
    public int getCargoTypeId() {
        return cargoTypeId;
    }

    /**
     * Returns the source tile coordinates for this batch.
     *
     * @return source tile position
     */
    public IntPoint2D getSourceP() {
        return sourceP;
    }

    /**
     * Tests equality across cargo type, source, creation time, and origin.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} describes the same batch
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CargoBatch test) {
            return test.cargoTypeId == cargoTypeId && sourceP.equals(test.sourceP)
                    && test.creationTime.equals(creationTime) && test.originalStationId == originalStationId;
        }
        return false;
    }

    /**
     * Returns a hash derived from the fields used for batch equality.
     *
     * @return hash code for this batch
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + cargoTypeId;
        result = 37 * result + sourceP.hashCode();
        result = 37 * result + originalStationId;
        result = 37 * result + creationTime.hashCode();

        return result;
    }

    /**
     * Orders batches by creation time, cargo type, origin station, then source
     * coordinates.
     *
     * @param o batch to compare with
     * @return ordering result for the batch fields
     */
    @Override
    public int compareTo(@NotNull CargoBatch o) {
        int timeComparison = creationTime.compareTo(o.creationTime);
        if (timeComparison != 0) {
            return timeComparison;
        }
        int cargoTypeComparison = Integer.compare(cargoTypeId, o.cargoTypeId);
        if (cargoTypeComparison != 0) {
            return cargoTypeComparison;
        }
        int originComparison = Integer.compare(originalStationId, o.originalStationId);
        if (originComparison != 0) {
            return originComparison;
        }
        if (!sourceP.equals(o.sourceP)) {
            return sourceP.compareTo(o.sourceP);
        }
        return 0;
    }
}