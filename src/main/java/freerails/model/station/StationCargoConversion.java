/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Records which cargo is converted to other cargo at a station.
 */
public class StationCargoConversion implements Serializable {

    private static final long serialVersionUID = 3690754012076978231L;
    // TODO what about Null as standard value
    private static final int NOT_CONVERTED = Integer.MIN_VALUE;
    // this should probably be a map
    private final List<Integer> convertedTo;

    // TODO provide as map
    /**
     * Creates conversion settings indexed by cargo index. The sentinel value
     * {@code Integer.MIN_VALUE} means that a cargo type is not converted.
     *
     * @param convertedTo product cargo identifier for each source cargo index,
     *                    or the not-converted sentinel
     */
    public StationCargoConversion(Integer[] convertedTo) {
        this.convertedTo = new ArrayList<>(Arrays.asList(convertedTo));
    }

    // TODO is this really needed
    /**
     * Creates a conversion configuration in which no cargo type is converted.
     *
     * @param numberOfCargoTypes number of cargo types to configure
     * @return empty conversion settings sized for the cargo collection
     */
    public static StationCargoConversion emptyInstance(int numberOfCargoTypes) {
        return new StationCargoConversion(emptyConversionArray(numberOfCargoTypes));
    }

    // TODO eliminate this
    /**
     * Creates an array initialized to the not-converted sentinel.
     *
     * @param numberOfCargoTypes number of cargo types to include
     * @return conversion array with one entry per cargo type
     */
    public static Integer[] emptyConversionArray(int numberOfCargoTypes) {
        Integer[] convertedTo = new Integer[numberOfCargoTypes];

        for (int i = 0; i < numberOfCargoTypes; i++) {
            convertedTo[i] = NOT_CONVERTED;
        }
        return convertedTo;
    }

    /**
     * Tests whether the cargo at an index has a configured conversion.
     *
     * @param cargoIndex zero-based position in the world's cargo collection
     * @return {@code true} if a product cargo is configured
     * @throws IndexOutOfBoundsException if the index is outside the configured
     *         cargo range
     */
    public boolean convertsCargo(int cargoIndex) {
        return NOT_CONVERTED != convertedTo.get(cargoIndex);
    }

    /**
     * Returns the product cargo identifier configured for a source cargo.
     *
     * @param cargoIndex zero-based position of the source cargo in the world's
     *                   cargo collection
     * @return product cargo identifier, or {@code Integer.MIN_VALUE} when no
     *         conversion is configured
     * @throws IndexOutOfBoundsException if the index is outside the configured
     *         cargo range
     */
    public int getConversion(int cargoIndex) {
        return convertedTo.get(cargoIndex);
    }

    /**
     * Returns a hash code derived from the configured conversion entries.
     *
     * @return hash code for these conversion settings
     */
    @Override
    public int hashCode() {
        int result = 0;
        for (Integer aConvertedTo : convertedTo) {
            result = 29 * result + aConvertedTo;
        }

        return result;
    }

    /**
     * Tests whether another conversion configuration has the same entries.
     *
     * @param obj object to compare
     * @return {@code true} if both configurations are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StationCargoConversion) {
            StationCargoConversion test = (StationCargoConversion) obj;
            return convertedTo.equals(test.convertedTo);
        }
        return false;
    }
}