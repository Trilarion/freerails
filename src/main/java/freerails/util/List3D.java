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
 * Created on 21-Jul-2005
 *
 */
package freerails.util;

import java.io.Serializable;
import java.util.List;

/**
 * @param <T>
 */
public interface List3D<T> extends Serializable {

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
    int sizeD3(int d1, int d2);

    /**
     * @param d1
     * @param d2
     * @param d3
     * @return
     */
    T get(int d1, int d2, int d3);

    /**
     * @param d1
     * @param d2
     * @return
     */
    List<T> get(int d1, int d2);

    /**
     * @param d1
     * @param d2
     * @return
     */
    T removeLastD3(int d1, int d2);

    /**
     *
     */
    void removeLastD1();

    /**
     * @param d1
     */
    void removeLastD2(int d1);

    /**
     * @return
     */
    int addD1();

    /**
     * @param d1
     * @return
     */
    int addD2(int d1);

    /**
     * @param d1
     * @param d2
     * @param element
     * @return
     */
    int addD3(int d1, int d2, T element);

    /**
     * @param d1
     * @param d2
     * @param d3
     * @param element
     */
    void set(int d1, int d2, int d3, T element);

}
