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
 * Created on 27-Jul-2005
 *
 */
package freerails.util;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 */
public class ListXDDiffsTest extends ListXDTest {

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        SortedMap<ListKey, Object> map = new TreeMap<>();
        list1d = new List1DDiff<>(map, list1d, listid.list1);
        list2d = new List2DDiff<>(map, list2d, listid.list2);
        list3d = new List3DDiff<>(map, list3d, listid.list3);
    }

    enum listid {
        list1, list2, list3
    }

}
