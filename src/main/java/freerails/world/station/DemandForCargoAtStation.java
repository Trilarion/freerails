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

package freerails.world.station;

import freerails.util.ImInts;

import java.io.Serializable;

/**
 * This class represents the demand for cargo at a station.
 */
public class DemandForCargoAtStation implements Serializable {
    private static final long serialVersionUID = 3257565088071038009L;

    private final ImInts demand;

    /**
     * @param demandArray
     */
    public DemandForCargoAtStation(boolean[] demandArray) {
        demand = ImInts.fromBoolean(demandArray);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DemandForCargoAtStation))
            return false;

        final DemandForCargoAtStation demandAtStation = (DemandForCargoAtStation) o;

        return demand.equals(demandAtStation.demand);
    }

    @Override
    public int hashCode() {
        int result = 0;

        for (int i = 0; i < demand.size(); i++) {
            result = 29 * result + demand.get(i);
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