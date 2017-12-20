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
 *
 */
package freerails.util;

import java.io.Serializable;

/**
 * @param <T>
 */
// TODO difference List2D and List2Dmpl?
public interface List2D<T> extends Serializable {

    /**
     * @return
     */
    int sizeD1();

    /**
     * @param d1
     * @return
     */
    int sizeD2(int d1);

    /**
     * @param d1
     * @param d2
     * @return
     */
    T get(int d1, int d2);

    /**
     * @param d1
     * @return
     */
    T removeLastD2(int d1);

    /**
     * @return
     */
    int removeLastD1();

    /**
     * @return
     */
    int addD1();

    /**
     * @param d1
     * @param element
     * @return
     */
    int addD2(int d1, T element);

    /**
     * @param d1
     * @param d2
     * @param element
     */
    void set(int d1, int d2, T element);

}
