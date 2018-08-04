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
package freerails.model.station;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the blue print for what an engine shop is producing.
 */
public class TrainBlueprint implements Serializable {

    private static final long serialVersionUID = 3545515106038592057L;
    private final int engineId;
    // TODO wagon type?? (make if ordinary immutable arraylist)
    private final List<Integer> wagonTypes;

    /**
     * @param engineId
     * @param wagonTypes
     */
    public TrainBlueprint(int engineId, Integer[] wagonTypes) {
        this.engineId = engineId;
        this.wagonTypes = new ArrayList<>(Arrays.asList(wagonTypes));
    }

    @Override
    public int hashCode() {
        return engineId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TrainBlueprint)) return false;

        final TrainBlueprint productionAtEngineShop = (TrainBlueprint) obj;

        if (engineId != productionAtEngineShop.engineId) return false;
        return wagonTypes.equals(productionAtEngineShop.wagonTypes);
    }

    /**
     * @return
     */
    public int getEngineId() {
        return engineId;
    }

    /**
     * @return
     */
    public List<Integer> getWagonTypes() {
        return wagonTypes;
    }

    @Override
    public String toString() {
        return "engine type: " + engineId + ", with " + wagonTypes.size() + " wagons";
    }
}