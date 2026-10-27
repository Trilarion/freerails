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

/*
 * BinaryNumberFormatter.java
 *
 */
package freerails.util;

/**
 * Formats non-negative integers as fixed-width binary strings, including for
 * use in track and terrain image file names.
 */
public class BinaryNumberFormatter {

    /**
     * Prevents instantiation of this utility class.
     */
    private BinaryNumberFormatter() {
    }

    /**
     * Formats an integer as a binary string padded with leading zeroes to the
     * requested width.
     *
     * @param i non-negative integer to format
     * @param bits number of binary digits
     * @return the zero-padded binary representation of {@code i}
     * @throws IllegalArgumentException if {@code bits} is outside 0 through
     *                                  31, or {@code i} is negative or does
     *                                  not fit in the requested number of bits
     */
    public static String format(int i, int bits) {
        if (bits < 0 || bits > Integer.SIZE - 1) {
            throw new IllegalArgumentException("bits must be between 0 and 31: " + bits);
        }
        if (i < 0 || (bits == 0 ? i != 0 : (i >>> bits) != 0)) {
            throw new IllegalArgumentException("Value " + i + " does not fit in " + bits + " bits.");
        }
        if (bits == 0) {
            return "";
        }
        String binary = Integer.toBinaryString(i);
        return "0".repeat(bits - binary.length()) + binary;
    }

    /**
     * Formats an integer as a fixed-width binary string with the least
     * significant bit at the left.
     *
     * @param i non-negative integer to format
     * @param bits number of binary digits
     * @return the zero-padded binary representation of {@code i}, reversed
     * @throws IllegalArgumentException if {@code bits} is outside 0 through
     *                                  31, or {@code i} is negative or does
     *                                  not fit in the requested number of bits
     */
    public static String formatWithLowBitOnLeft(int i, int bits) {
        StringBuilder buff = new StringBuilder(format(i, bits));
        buff.reverse();

        return buff.toString();
    }
}