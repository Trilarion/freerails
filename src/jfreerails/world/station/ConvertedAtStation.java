/*
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

package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;


public class ConvertedAtStation implements FreerailsSerializable {
    public static final int NOT_CONVERTED = Integer.MIN_VALUE;
    private final int[] convertedTo;

    public ConvertedAtStation(int[] convertedTo) {
        this.convertedTo = (int[])convertedTo.clone(); //defensive copy.
    }

    public static ConvertedAtStation emptyInstance(int numberOfCargoTypes) {
        int[] convertedTo = emptyConversionArray(numberOfCargoTypes);

        return new ConvertedAtStation(convertedTo);
    }

    public static int[] emptyConversionArray(int numberOfCargoTypes) {
        int[] convertedTo = new int[numberOfCargoTypes];

        for (int i = 0; i < numberOfCargoTypes; i++) {
            convertedTo[i] = NOT_CONVERTED;
        }

        return convertedTo;
    }

    public boolean isCargoConverted(int cargoNumber) {
        if (NOT_CONVERTED == convertedTo[cargoNumber]) {
            return false;
        } else {
            return true;
        }
    }

    public int getConversion(int cargoNumber) {
        return convertedTo[cargoNumber];
    }

    public boolean equals(Object o) {
        if (o instanceof ConvertedAtStation) {
            ConvertedAtStation test = (ConvertedAtStation)o;

            if (this.convertedTo.length != test.convertedTo.length) {
                return false;
            }

            for (int i = 0; i < convertedTo.length; i++) {
                if (convertedTo[i] != test.convertedTo[i]) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }
}