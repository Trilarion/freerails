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

/*
 *
 */
package freerails.world.station;

import freerails.util.ImInts;

import java.io.Serializable;

/**
 * Represents the blue print for what an engine shop is producing.
 */
public class TrainBlueprint implements Serializable {

    private static final long serialVersionUID = 3545515106038592057L;
    // TODO engine type?? no enum or an ID
    private final int engineType;
    // TODO wagon type?? (make if ordinary immutable arraylist)
    private final ImInts wagonTypes;

    /**
     * @param engineType
     * @param wagonTypes
     */
    public TrainBlueprint(int engineType, int[] wagonTypes) {
        this.engineType = engineType;
        this.wagonTypes = new ImInts(wagonTypes);
    }

    @Override
    public int hashCode() {
        return engineType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TrainBlueprint))
            return false;

        final TrainBlueprint productionAtEngineShop = (TrainBlueprint) o;

        if (engineType != productionAtEngineShop.engineType)
            return false;
        return wagonTypes.equals(productionAtEngineShop.wagonTypes);
    }

    /**
     * @return
     */
    public int getEngineType() {
        return engineType;
    }

    /**
     * @return
     */
    public ImInts getWagonTypes() {
        return wagonTypes;
    }

    @Override
    public String toString() {
        return "engine type: " + this.engineType + ", with " + wagonTypes.size() + " wagons";
    }
}