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
 * Records which cargo is converted to other cargo at a station.
 */
public class CargoConversionAtStation implements Serializable {
    private static final long serialVersionUID = 3690754012076978231L;

    private static final int NOT_CONVERTED = Integer.MIN_VALUE;

    private final ImInts convertedTo;

    /**
     * @param convertedTo
     */
    public CargoConversionAtStation(int[] convertedTo) {
        this.convertedTo = new ImInts(convertedTo);
    }

    /**
     * @param numberOfCargoTypes
     * @return
     */
    public static CargoConversionAtStation emptyInstance(int numberOfCargoTypes) {
        int[] convertedTo = emptyConversionArray(numberOfCargoTypes);

        return new CargoConversionAtStation(convertedTo);
    }

    /**
     * @param numberOfCargoTypes
     * @return
     */
    public static int[] emptyConversionArray(int numberOfCargoTypes) {
        int[] convertedTo = new int[numberOfCargoTypes];

        for (int i = 0; i < numberOfCargoTypes; i++) {
            convertedTo[i] = NOT_CONVERTED;
        }

        return convertedTo;
    }

    /**
     * @param cargoNumber
     * @return
     */
    public boolean isCargoConverted(int cargoNumber) {
        return NOT_CONVERTED != convertedTo.get(cargoNumber);
    }

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
    public boolean equals(Object o) {
        if (o instanceof CargoConversionAtStation) {
            CargoConversionAtStation test = (CargoConversionAtStation) o;

            if (this.convertedTo.size() != test.convertedTo.size()) {
                return false;
            }

            for (int i = 0; i < convertedTo.size(); i++) {
                if (convertedTo.get(i) != test.convertedTo.get(i)) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }
}