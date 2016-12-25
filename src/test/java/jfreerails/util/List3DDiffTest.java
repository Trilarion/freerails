/*
 * Created on 27-Jul-2005
 *
 */
package jfreerails.util;

import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.TestCase;

public class List3DDiffTest extends TestCase {

    List3DDiff<Object> diffs;

    SortedMap<ListKey, Object> map;

    List3D<Object> underlying;

    enum listid {
        test
    }

    @Override
    protected void setUp() throws Exception {
        underlying = new List3DImpl<Object>(0, 0);
        map = new TreeMap<ListKey, Object>();
        diffs = new List3DDiff<Object>(map, underlying, listid.test);
    }

    /*
     * Test method for 'jfreerails.util.List3DDiff.addD1()'
     */
    public void testAddD1() {
        diffs.addD1();
        assertEquals(1, diffs.sizeD1());
        diffs.addD1();
        assertEquals(2, diffs.sizeD1());
    }

    /*
     * Test method for 'jfreerails.util.List3DDiff.addD2(int)'
     */
    public void testAddD2() {
        underlying.addD1();
        underlying.addD1();
        diffs.addD2(0);
        assertEquals(1, diffs.sizeD2(0));
        diffs.addD2(0);
        assertEquals(2, diffs.sizeD2(0));
        diffs.addD2(1);
        assertEquals(1, diffs.sizeD2(1));
    }

    /*
     * Test method for 'jfreerails.util.List3DDiff.addD3(int, int, T)'
     */
    public void testAddD3() {
        underlying.addD1();
        underlying.addD1();
        underlying.addD2(1);
        underlying.addD2(1);
        diffs.addD3(1, 0, new Integer(5));
        assertEquals(1, diffs.sizeD3(1, 0));
        diffs.addD3(1, 1, new Integer(5));
        assertEquals(1, diffs.sizeD3(1, 1));
    }

    /*
     * Test method for 'jfreerails.util.List3DDiff.get(int, int, int)'
     */
    public void testGetIntIntInt() {
        underlying.addD1();
        underlying.addD1();
        underlying.addD2(1);
        underlying.addD2(1);
        underlying.addD3(1, 1, new Integer(1));
        assertEquals(new Integer(1), diffs.get(1, 1, 0));
        diffs.addD3(1, 1, new Integer(2));
        diffs.addD3(1, 1, new Integer(3));
        assertEquals(new Integer(2), diffs.get(1, 1, 1));
        assertEquals(new Integer(3), diffs.get(1, 1, 2));

    }

    /*
     * Test method for 'jfreerails.util.List3DDiff.getUnderlyingSize(int...)'
     */
    public void testGetUnderlyingSize() {
        assertEquals(-1, diffs.getUnderlyingSize(0, 0));
        assertEquals(-1, diffs.getUnderlyingSize(0));
        assertEquals(0, diffs.getUnderlyingSize());
        assertEquals(-1, diffs.getUnderlyingSize(1, 0));
        assertEquals(-1, diffs.getUnderlyingSize(0, 1));
        underlying.addD1();
        underlying.addD1();
        assertEquals(2, diffs.getUnderlyingSize());
        assertEquals(0, diffs.getUnderlyingSize(1));
        assertEquals(0, diffs.getUnderlyingSize(0));

    }

    /*
     * Test method for 'jfreerails.util.List3DDiff.removeLastD1()'
     */
    public void testRemoveLastD1() {
        underlying.addD1();
        underlying.addD1();
        assertEquals(2, diffs.sizeD1());
        diffs.removeLastD1();
        assertEquals(1, diffs.sizeD1());
        diffs.removeLastD1();
        assertEquals(0, diffs.sizeD1());
        try {
            diffs.removeLastD1();
            fail();
        } catch (Exception e) {

        }

    }

    /*
     * Test method for 'jfreerails.util.List3DDiff.removeLastD2(int)'
     */
    public void testRemoveLastD2() {
        underlying.addD1();
        underlying.addD2(0);
        underlying.addD2(0);
        underlying.addD2(0);
        assertEquals(3, diffs.sizeD2(0));
        diffs.removeLastD2(0);
        assertEquals(2, diffs.sizeD2(0));
        diffs.removeLastD2(0);
        diffs.removeLastD2(0);
        assertEquals(0, diffs.sizeD2(0));
        try {
            diffs.removeLastD2(0);
            fail();
        } catch (Exception e) {

        }

    }

    /*
     * Test method for 'jfreerails.util.List3DDiff.removeLastD3(int, int)'
     */
    public void testRemoveLastD3() {
        underlying.addD1();
        underlying.addD2(0);
        underlying.addD3(0, 0, new Integer(1));
        underlying.addD3(0, 0, new Integer(2));
        underlying.addD3(0, 0, new Integer(3));
        assertEquals(3, diffs.sizeD3(0, 0));
        diffs.removeLastD3(0, 0);
        assertEquals(2, diffs.sizeD3(0, 0));
        diffs.removeLastD3(0, 0);
        assertEquals(1, diffs.sizeD3(0, 0));
        diffs.removeLastD3(0, 0);
        assertEquals(0, diffs.sizeD3(0, 0));
        try {
            diffs.removeLastD3(0, 0);
            fail();
        } catch (Exception e) {

        }

    }

    /*
     * Test method for 'jfreerails.util.List3DDiff.set(int, int, int, T)'
     */
    public void testSetIntIntIntT() {
        underlying.addD1();
        underlying.addD2(0);
        underlying.addD3(0, 0, new Integer(1));
        assertEquals(new Integer(1), diffs.get(0, 0, 0));
        diffs.addD3(0, 0, new Integer(2));
        assertEquals(new Integer(2), diffs.get(0, 0, 1));
        diffs.set(0, 0, 0, new Integer(11));
        assertEquals(new Integer(11), diffs.get(0, 0, 0));
        diffs.set(0, 0, 1, new Integer(22));
        assertEquals(new Integer(22), diffs.get(0, 0, 1));

    }

    /*
     * Test method for 'jfreerails.util.List3DDiff.sizeD1()'
     */
    public void testSizeDx() {
        assertEquals(0, diffs.sizeD1());
        underlying.addD1();
        assertEquals(1, diffs.sizeD1());
        assertEquals(0, diffs.sizeD2(0));
        underlying.addD2(0);
        assertEquals(1, diffs.sizeD2(0));
        assertEquals(0, diffs.sizeD3(0, 0));
        underlying.addD3(0, 0, new Integer(4));
        underlying.addD3(0, 0, new Integer(4));
        assertEquals(2, diffs.sizeD3(0, 0));

    }

    /*
     * Test method for 'jfreerails.util.List3DDiff.uGet(int...)'
     */
    public void testUGet() {
        underlying.addD1();
        underlying.addD2(0);
        underlying.addD3(0, 0, new Integer(1));
        assertEquals(new Integer(1), diffs.uGet(0, 0, 0));
    }

}
