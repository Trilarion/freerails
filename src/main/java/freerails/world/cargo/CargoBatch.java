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
 * This class represents a cargo batch.
 *
 * Cargo of the same batch is cargo of the same type that was produced at the same location at the same time.
 */
public class CargoBatch implements Serializable, Comparable<CargoBatch> {

    // TODO Why is cargoType an int here and not CargoCategory or CargoType??
    private static final long serialVersionUID = 3257006557605540149L;
    private final int cargoType;
    private final int sourceX;
    private final int sourceY;
    private final int stationOfOrigin;
    private final long timeCreated;

    /**
     * @param type
     * @param x
     * @param y
     * @param time
     * @param origin
     */
    public CargoBatch(int type, int x, int y, long time, int origin) {
        cargoType = type;
        sourceX = x;
        sourceY = y;
        timeCreated = time;
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

    /**
     * @return
     */
    public long getTimeCreated() {
        return timeCreated;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CargoBatch) {
            CargoBatch test = (CargoBatch) o;

            return test.getCargoType() == this.cargoType
                    && test.getSourceX() == this.sourceX
                    && test.sourceY == this.sourceY
                    && test.timeCreated == this.timeCreated
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
                + (int) (this.timeCreated ^ (this.timeCreated >>> 32));

        return result;
    }

    public int compareTo(CargoBatch o) {
        if (timeCreated != o.timeCreated)
            return (int) (timeCreated - o.timeCreated);
        if (cargoType != o.cargoType)
            return (cargoType - o.cargoType);
        if (stationOfOrigin != o.stationOfOrigin)
            return (stationOfOrigin - o.stationOfOrigin);
        if (sourceX != o.sourceX)
            return (sourceX - o.sourceX);
        if (sourceY != o.sourceY)
            return (sourceY - o.sourceY);
        return 0;
    }
}