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

package freerails.model.cargo;

import freerails.util.Vec2D;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Represents a cargo batch. Cargo of the same batch is cargo of
 * the same type that was produced at the same location and at the same time.
 */
public class CargoBatch implements Serializable, Comparable<CargoBatch> {

    // TODO Why is cargoType an int here and not Cargo directly?? Because we have a list in the world for that?
    private static final long serialVersionUID = 3257006557605540149L;
    private final int cargoTypeId;
    private final Vec2D sourceP;
    private final int originalStationId;
    // TODO creation time maybe as game time?
    private final long creationTime;

    /**
     * @param cargoTypeId
     * @param p
     * @param time
     * @param origin
     */
    public CargoBatch(int cargoTypeId, Vec2D p, long time, int origin) {
        this.cargoTypeId = cargoTypeId;
        sourceP = p;
        creationTime = time;
        originalStationId = origin;
    }

    /**
     * @return
     */
    public int getOriginalStationId() {
        return originalStationId;
    }

    /**
     * @return
     */
    public int getCargoTypeId() {
        return cargoTypeId;
    }

    /**
     * @return
     */
    public Vec2D getSourceP() {
        return sourceP;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CargoBatch) {
            CargoBatch test = (CargoBatch) obj;

            return test.cargoTypeId == cargoTypeId && sourceP.equals(test.sourceP) && test.creationTime == creationTime && test.originalStationId == originalStationId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + cargoTypeId;
        result = 37 * result + sourceP.hashCode();
        result = 37 * result + originalStationId;
        result = 37 * result + (int) (creationTime ^ (creationTime >>> 32));

        return result;
    }

    @Override
    public int compareTo(@NotNull CargoBatch o) {
        if (creationTime != o.creationTime) return (int) (creationTime - o.creationTime);
        if (cargoTypeId != o.cargoTypeId) return cargoTypeId - o.cargoTypeId;
        if (originalStationId != o.originalStationId) return originalStationId - o.originalStationId;
        if (!sourceP.equals(o.sourceP)) return sourceP.compareTo(o.sourceP);
        return 0;
    }
}