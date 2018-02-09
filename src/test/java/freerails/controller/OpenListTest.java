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
package freerails.controller;

import junit.framework.TestCase;

/**
 *
 */
public class OpenListTest extends TestCase {

    /**
     *
     */
    public void testContains() {
        OpenList openList = new OpenList();
        assertFalse(openList.contains(0));
        openList.add(0, 4);
        assertTrue(openList.contains(0));
        assertFalse(openList.contains(4));
        openList.popNodeWithSmallestF();
        assertFalse(openList.contains(0));
    }

    /**
     *
     */
    public void testSmallestF() {
        OpenList openList = new OpenList();
        openList.add(0, 4);
        assertEquals(4, openList.smallestF());
        openList.add(1, 5);
        assertEquals(4, openList.smallestF());
        openList.add(5, 1);
        assertEquals(1, openList.smallestF());
        openList.popNodeWithSmallestF();
        assertEquals(4, openList.smallestF());
        openList.popNodeWithSmallestF();
        assertEquals(5, openList.smallestF());

    }

    /**
     *
     */
    public void testSize() {
        OpenList openList = new OpenList();
        assertEquals(0, openList.size());
        openList.add(0, 4);
        assertEquals(1, openList.size());
        openList.popNodeWithSmallestF();
        assertEquals(0, openList.size());
    }

    /**
     *
     */
    public void testAdd() {
        OpenList openList = new OpenList();
        openList.add(1, 4);
        assertEquals(1, openList.size());
        assertEquals(4, openList.smallestF());
        openList.add(1, 6);
        assertEquals(1, openList.size());
        assertEquals(6, openList.smallestF());
    }
}
