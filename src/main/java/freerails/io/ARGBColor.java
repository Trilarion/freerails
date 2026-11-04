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

package freerails.io;

/**
 * ARGB value in an int. Needs its own class, so gson can convert to/from hex value in JSON de/serialization.
 */
public class ARGBColor {

    private int argb;

    /**
     * Creates a color from its packed alpha-red-green-blue integer value.
     *
     * @param argb packed ARGB value
     */
    public ARGBColor(int argb) {
        this.argb = argb;
    }

    /**
     * Returns this color's packed ARGB value.
     *
     * @return packed ARGB value
     */
    public int getARGB() {
        return argb;
    }

    /**
     * Formats a color as a hash-prefixed hexadecimal string.
     *
     * @param color color to format
     * @return hexadecimal representation of the packed ARGB value
     */
    public static String toHexString(ARGBColor color) {
        return String.format("#%s", Integer.toHexString(color.getARGB()));
    }

    /**
     * Parses a hash-prefixed hexadecimal ARGB value.
     *
     * @param hexString string to parse
     * @return the represented color
     * @throws IllegalArgumentException if the string lacks the {@code #}
     * prefix or contains an invalid hexadecimal value
     * @throws NullPointerException if {@code hexString} is null
     */
    public static ARGBColor fromHexString(String hexString) {
        if (!(hexString.charAt(0) == '#')) {
            throw new IllegalArgumentException();
        }
        int argb = Integer.parseUnsignedInt(hexString.substring(1), 16);
        return new ARGBColor(argb);
    }
}
