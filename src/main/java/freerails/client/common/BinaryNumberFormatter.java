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
 * BinaryNumberFormatter.java
 *
 * Created on 30 April 2003, 20:23
 */
package freerails.client.common;

/**
 * Used to generate file names for track and terrain images.
 *
 */
public class BinaryNumberFormatter {

    /**
     *
     * @param i
     * @param bits
     * @return
     */
    public static String format(int i, int bits) {
        int maxValue = 1 << (bits);

        if (i < 0) {
            throw new IllegalArgumentException(
                    "i must be greater than 0.  It was " + i);
        }

        if (i >= maxValue) {
            throw new IllegalArgumentException("i must be less than "
                    + maxValue + ".  It was " + i + " ("
                    + Integer.toString(i, 2) + ")");
        }

        String s = Integer.toString(i + maxValue, 2);

        return s.substring(1);
    }

    /**
     *
     * @param i
     * @param bits
     * @return
     */
    public static String formatWithLowBitOnLeft(int i, int bits) {
        StringBuilder buff = new StringBuilder(format(i, bits));
        buff.reverse();

        return buff.toString();
    }
}