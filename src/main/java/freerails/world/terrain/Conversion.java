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

package freerails.world.terrain;

import java.io.Serializable;

/**
 * This class represents the conversion of one cargo type to another one a tile.
 */
public class Conversion implements Serializable {

    private static final long serialVersionUID = 3546356219414853689L;
    private final int input;
    private final int output;

    /**
     * @param in
     * @param out
     */
    public Conversion(int in, int out) {
        input = in;
        output = out;
    }

    /**
     * @return
     */
    public int getInput() {
        return input;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Conversion))
            return false;

        final Conversion conversion = (Conversion) o;

        if (input != conversion.input)
            return false;
        return output == conversion.output;
    }

    @Override
    public int hashCode() {
        int result;
        result = input;
        result = 29 * result + output;
        return result;
    }

    /**
     * @return
     */
    public int getOutput() {
        return output;
    }
}