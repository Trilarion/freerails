/*
 * Created on 22-May-2005
 *
 */
package jfreerails.controller;

import junit.framework.TestCase;

public class OpenListTest extends TestCase {

    public void testGetF() {
    }

    public void testContains() {
        OpenList openList = new OpenList();
        assertFalse(openList.contains(0));
        openList.add(0, 4);
        assertTrue(openList.contains(0));
        assertFalse(openList.contains(4));
        openList.popNodeWithSmallestF();
        assertFalse(openList.contains(0));
    }

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

    public void testSize() {
        OpenList openList = new OpenList();
        assertEquals(0, openList.size());
        openList.add(0, 4);
        assertEquals(1, openList.size());
        openList.popNodeWithSmallestF();
        assertEquals(0, openList.size());
    }

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
