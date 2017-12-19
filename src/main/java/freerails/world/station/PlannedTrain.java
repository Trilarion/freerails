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
 * Created on 28-Mar-2003
 *
 */
package freerails.world.station;

import freerails.util.ImInts;

import java.io.Serializable;

/**
 * This class represents the blue print for what an engine shop is producing.
 */
public class PlannedTrain implements Serializable {
    private static final long serialVersionUID = 3545515106038592057L;

    private final int engineType;

    private final ImInts wagonTypes;

    /**
     * @param e
     * @param wagons
     */
    public PlannedTrain(int e, int[] wagons) {
        engineType = e;
        wagonTypes = new ImInts(wagons);
    }

    @Override
    public int hashCode() {
        return engineType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PlannedTrain))
            return false;

        final PlannedTrain productionAtEngineShop = (PlannedTrain) o;

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
        return "engine type: " + this.engineType + ", with "
                + wagonTypes.size() + "wagons";
    }
}