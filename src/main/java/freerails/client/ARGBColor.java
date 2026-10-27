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

package freerails.client;

/**
 * ARGB value in an int. Needs its own class, so gson can convert to/from hex value in json de/serialization.
 */
public class ARGBColor {

    private int argb;

    public ARGBColor(int argb) {
        this.argb = argb;
    }

    public int getARGB() {
        return argb;
    }

    public static String toHexString(ARGBColor color) {
        return String.format("#%s", Integer.toHexString(color.getARGB()));
    }

    public static ARGBColor fromHexString(String hexString) {
        if (!(hexString.charAt(0) == '#')) {
            throw new IllegalArgumentException();
        }
        int argb = Integer.parseUnsignedInt(hexString.substring(1), 16);
        return new ARGBColor(argb);
    }
}
