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

/**
 *
 */
public class Lists {
// TODO move to their respective ListND classes

    /**
     * @param a
     * @param b
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean equals(List1D a, List1D b) {
        if (a.size() != b.size())
            return false;
        for (int i = 0; i < a.size(); i++) {
            if (!Utils.equal(a.get(i), b.get(i)))
                return false;
        }
        return true;
    }

    /**
     * @param a
     * @param b
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean equals(List2D a, List2D b) {
        if (a.sizeD1() != b.sizeD1())
            return false;
        for (int d1 = 0; d1 < a.sizeD1(); d1++) {
            if (a.sizeD2(d1) != b.sizeD2(d1))
                return false;
            for (int d2 = 0; d2 < a.sizeD2(d1); d2++) {
                if (!Utils.equal(a.get(d1, d2), b.get(d1, d2)))
                    return false;
            }
        }
        return true;
    }

    /**
     * @param a
     * @param b
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean equals(List3D a, List3D b) {
        if (a.sizeD1() != b.sizeD1())
            return false;
        for (int d1 = 0; d1 < a.sizeD1(); d1++) {
            if (a.sizeD2(d1) != b.sizeD2(d1))
                return false;
            for (int d2 = 0; d2 < a.sizeD2(d1); d2++) {
                if (a.sizeD3(d1, d2) != b.sizeD3(d1, d2))
                    return false;
                for (int d3 = 0; d3 < a.sizeD3(d1, d2); d3++) {
                    if (!Utils.equal(a.get(d1, d2, d3), b.get(d1, d2, d3)))
                        return false;
                }
            }
        }
        return true;
    }

}
