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

import java.io.Serializable;

/**
 * Records which cargo is converted to other cargo at a station.
 */
public class StationCargoConversion implements Serializable {

    private static final long serialVersionUID = 3690754012076978231L;
    // TODO what about Null as standard value
    private static final int NOT_CONVERTED = Integer.MIN_VALUE;
    // this should probably be a map
    private final ImmutableList<Integer> convertedTo;

    // TODO provide as map
    /**
     * @param convertedTo
     */
    public StationCargoConversion(Integer[] convertedTo) {
        this.convertedTo = new ImmutableList<>(convertedTo);
    }

    // TODO is this really needed
    /**
     * @param numberOfCargoTypes
     * @return
     */
    public static StationCargoConversion emptyInstance(int numberOfCargoTypes) {
        return new StationCargoConversion(emptyConversionArray(numberOfCargoTypes));
    }

    // TODO eliminate this
    /**
     * @param numberOfCargoTypes
     * @return
     */
    public static Integer[] emptyConversionArray(int numberOfCargoTypes) {
        Integer[] convertedTo = new Integer[numberOfCargoTypes];

        for (int i = 0; i < numberOfCargoTypes; i++) {
            convertedTo[i] = NOT_CONVERTED;
        }
        return convertedTo;
    }

    // TODO what is the cargoNumber and do we need it? Maybe a cargo type?
    /**
     * @param cargoNumber
     * @return
     */
    public boolean convertsCargo(int cargoNumber) {
        return NOT_CONVERTED != convertedTo.get(cargoNumber);
    }

    // TODO what is the meaning of the return value?
    /**
     * @param cargoNumber
     * @return
     */
    public int getConversion(int cargoNumber) {
        return convertedTo.get(cargoNumber);
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int i = 0; i < convertedTo.size(); i++) {
            result = 29 * result + convertedTo.get(i);
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StationCargoConversion) {
            StationCargoConversion test = (StationCargoConversion) obj;
            return convertedTo.equals(test.convertedTo);
        }
        return false;
    }
}