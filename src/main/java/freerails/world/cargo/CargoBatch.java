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

import freerails.util.Point2D;

import java.io.Serializable;

/**
 * Represents a cargo batch. Cargo of the same batch is cargo of
 * the same type that was produced at the same location and at the same time.
 */
public class CargoBatch implements Serializable, Comparable<CargoBatch> {

    // TODO Why is cargoType an int here and not CargoCategory or CargoType??
    private static final long serialVersionUID = 3257006557605540149L;
    private final int cargoType;
    private final Point2D sourceP;
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
    public CargoBatch(int cargoType, Point2D p, long time, int origin) {
        this.cargoType = cargoType;
        sourceP = p;
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
    public Point2D getSourceP() {
        return sourceP;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CargoBatch) {
            CargoBatch test = (CargoBatch) obj;

            return test.cargoType == cargoType && sourceP.equals(test.sourceP) && test.creationTime == creationTime && test.stationOfOrigin == stationOfOrigin;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + cargoType;
        result = 37 * result + sourceP.hashCode();
        result = 37 * result + stationOfOrigin;
        result = 37 * result + (int) (creationTime ^ (creationTime >>> 32));

        return result;
    }

    public int compareTo(CargoBatch o) {
        if (creationTime != o.creationTime) return (int) (creationTime - o.creationTime);
        if (cargoType != o.cargoType) return cargoType - o.cargoType;
        if (stationOfOrigin != o.stationOfOrigin) return stationOfOrigin - o.stationOfOrigin;
        // TODO compareTo for Point2D?
        if (sourceP.x != o.sourceP.x) return sourceP.x - o.sourceP.x;
        if (sourceP.y != o.sourceP.y) return sourceP.y - o.sourceP.y;
        return 0;
    }
}