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
 * Created on 07-Jul-2005
 *
 */
package freerails.world.common;

import freerails.util.Immutable;
import freerails.world.FreerailsSerializable;

import java.util.Arrays;

/**
 * An immutable list of Strings.
 *
 */
@Immutable
public class ImStringList implements FreerailsSerializable {

    private static final long serialVersionUID = 5211786598838212188L;

    private final String[] strings;

    /**
     *
     * @param strings
     */
    public ImStringList(String... strings) {
        this.strings = strings.clone();
    }

    /**
     *
     * @param i
     * @return
     */
    public String get(int i) {
        return strings[i];
    }

    /**
     *
     * @return
     */
    public int size() {
        return strings.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImStringList))
            return false;

        final ImStringList imStringList = (ImStringList) o;

        return Arrays.equals(strings, imStringList.strings);
    }

    @Override
    public int hashCode() {
        return strings.length;
    }

}
