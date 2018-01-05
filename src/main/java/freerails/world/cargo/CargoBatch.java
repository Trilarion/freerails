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

package freerails.world.cargo;

import java.io.Serializable;

/**
 * Represents a cargo batch. Cargo of the same batch is cargo of
 * the same type that was produced at the same location and at the same time.
 */
public class CargoBatch implements Serializable, Comparable<CargoBatch> {

    // TODO Why is cargoType an int here and not CargoCategory or CargoType??
    private static final long serialVersionUID = 3257006557605540149L;
    private final int cargoType;
    // TODO Use a Point instead of X ands Y
    private final int sourceX;
    private final int sourceY;
    // TODO call it originalStationID
    private final int stationOfOrigin;
    private final long creationTime;

    /**
     * @param cargoType
     * @param x
     * @param y
     * @param time
     * @param origin
     */
    public CargoBatch(int cargoType, int x, int y, long time, int origin) {
        this.cargoType = cargoType;
        sourceX = x;
        sourceY = y;
        creationTime = time;
        stationOfOrigin = origin;
    }

    /**
     * @return
     */
    public int getStationOfOrigin() {
        return stationOfOrigin;
    }

    /**
     * @return
     */
    public int getCargoType() {
        return cargoType;
    }

    /**
     * @return
     */
    public int getSourceX() {
        return sourceX;
    }

    /**
     * @return
     */
    public int getSourceY() {
        return sourceY;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CargoBatch) {
            CargoBatch test = (CargoBatch) o;

            return test.cargoType == this.cargoType
                    && test.sourceX == this.sourceX
                    && test.sourceY == this.sourceY
                    && test.creationTime == this.creationTime
                    && test.stationOfOrigin == this.stationOfOrigin;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + this.cargoType;
        result = 37 * result + this.sourceX;
        result = 37 * result + this.sourceY;
        result = 37 * result + this.stationOfOrigin;
        result = 37 * result
                + (int) (this.creationTime ^ (this.creationTime >>> 32));

        return result;
    }

    public int compareTo(CargoBatch o) {
        if (creationTime != o.creationTime)
            return (int) (creationTime - o.creationTime);
        if (cargoType != o.cargoType)
            return cargoType - o.cargoType;
        if (stationOfOrigin != o.stationOfOrigin)
            return stationOfOrigin - o.stationOfOrigin;
        if (sourceX != o.sourceX)
            return sourceX - o.sourceX;
        if (sourceY != o.sourceY)
            return sourceY - o.sourceY;
        return 0;
    }
}