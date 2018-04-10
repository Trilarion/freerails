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

package freerails.model.station;

import freerails.util.ImmutableList;
import freerails.util.Utils;

import java.io.Serializable;

/**
 * Represents the demand for cargo at a station.
 */
public class StationDemand implements Serializable {

    private static final long serialVersionUID = 3257565088071038009L;
    private final ImmutableList<Integer> demand;

    /**
     * @param demandArray
     */
    public StationDemand(boolean[] demandArray) {
        demand = Utils.integerImmutableListFromBoolean(demandArray);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StationDemand)) return false;

        final StationDemand demandAtStation = (StationDemand) obj;

        return demand.equals(demandAtStation.demand);
    }

    @Override
    public int hashCode() {
        int result = 0;

        for (Integer aDemand : demand) {
            result = 29 * result + aDemand;
        }

        return result;
    }

    /**
     * @param cargoNumber
     * @return
     */
    public boolean isCargoDemanded(int cargoNumber) {
        return demand.get(cargoNumber) == 1;
    }

}