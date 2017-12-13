/*
 * Copyright (C) Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.world.cargo;

import jfreerails.world.common.FreerailsSerializable;


/** This class represents a cargo batch (cargo of the same batch is cargo of the same type
 * that was produced at the same location at the same time).
 *
 * @author Luke
 */
public class CargoBatch implements FreerailsSerializable {
    private final int cargoType;
    private final int sourceX;
    private final int sourceY;
    private final int stationOfOrigin;
    private final long timeCreated;

    public CargoBatch(int type, int x, int y, long time, int stationOfOrigin) {
        cargoType = type;
        sourceX = x;
        sourceY = y;
        timeCreated = time;
        this.stationOfOrigin = stationOfOrigin;
    }

    public int getStationOfOrigin() {
        return stationOfOrigin;
    }

    public int getCargoType() {
        return cargoType;
    }

    public int getSourceX() {
        return sourceX;
    }

    public int getSourceY() {
        return sourceY;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public boolean equals(Object o) {
        if (o instanceof CargoBatch) {
            CargoBatch test = (CargoBatch)o;

            if (test.getCargoType() == this.cargoType &&
                    test.getSourceX() == this.sourceX &&
                    test.sourceY == this.sourceY &&
                    test.timeCreated == this.timeCreated &&
                    test.stationOfOrigin == this.stationOfOrigin) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + this.cargoType;
        result = 37 * result + this.sourceX;
        result = 37 * result + this.sourceY;
        result = 37 * result + this.stationOfOrigin;
        result = 37 * result +
            (int)(this.timeCreated ^ (this.timeCreated >>> 32));

        return result;
    }
}