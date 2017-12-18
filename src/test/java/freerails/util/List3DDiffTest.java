/*
 * Created on 27-Jul-2005
 *
 */
package freerails.util;

import junit.framework.TestCase;

import java.util.SortedMap;
import java.util.TreeMap;

public class List3DDiffTest extends TestCase {

    List3DDiff<Object> diffs;

    SortedMap<ListKey, Object> map;

    List3D<Object> underlying;

    @Override
    protected void setUp() throws Exception {
        underlying = new List3DImpl<>(0, 0);
        map = new TreeMap<>();
        diffs = new List3DDiff<>(map, underlying, listid.test);
    }

    /*
     * Test method for 'freerails.util.List3DDiff.addD1()'
     */
    public void testAddD1() {
        diffs.addD1();
        assertEquals(1, diffs.sizeD1());
        diffs.addD1();
        assertEquals(2, diffs.sizeD1());
    }

    /*
     * Test method for 'freerails.util.List3DDiff.addD2(int)'
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
     * Test method for 'freerails.util.List3DDiff.addD3(int, int, T)'
     */
    public void testAddD3() {
        underlying.addD1();
        underlying.addD1();
        underlying.addD2(1);
        underlying.addD2(1);
        diffs.addD3(1, 0, 5);
        assertEquals(1, diffs.sizeD3(1, 0));
        diffs.addD3(1, 1, 5);
        assertEquals(1, diffs.sizeD3(1, 1));
    }

    /*
     * Test method for 'freerails.util.List3DDiff.get(int, int, int)'
     */
    public void testGetIntIntInt() {
        underlying.addD1();
        underlying.addD1();
        underlying.addD2(1);
        underlying.addD2(1);
        underlying.addD3(1, 1, 1);
        assertEquals(1, diffs.get(1, 1, 0));
        diffs.addD3(1, 1, 2);
        diffs.addD3(1, 1, 3);
        assertEquals(2, diffs.get(1, 1, 1));
        assertEquals(3, diffs.get(1, 1, 2));

    }

    /*
     * Test method for 'freerails.util.List3DDiff.getUnderlyingSize(int...)'
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
     * Test method for 'freerails.util.List3DDiff.removeLastD1()'
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
     * Test method for 'freerails.util.List3DDiff.removeLastD2(int)'
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
     * Test method for 'freerails.util.List3DDiff.removeLastD3(int, int)'
     */
    public void testRemoveLastD3() {
        underlying.addD1();
        underlying.addD2(0);
        underlying.addD3(0, 0, 1);
        underlying.addD3(0, 0, 2);
        underlying.addD3(0, 0, 3);
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
     * Test method for 'freerails.util.List3DDiff.set(int, int, int, T)'
     */
    public void testSetIntIntIntT() {
        underlying.addD1();
        underlying.addD2(0);
        underlying.addD3(0, 0, 1);
        assertEquals(1, diffs.get(0, 0, 0));
        diffs.addD3(0, 0, 2);
        assertEquals(2, diffs.get(0, 0, 1));
        diffs.set(0, 0, 0, 11);
        assertEquals(11, diffs.get(0, 0, 0));
        diffs.set(0, 0, 1, 22);
        assertEquals(22, diffs.get(0, 0, 1));

    }

    /*
     * Test method for 'freerails.util.List3DDiff.sizeD1()'
     */
    public void testSizeDx() {
        assertEquals(0, diffs.sizeD1());
        underlying.addD1();
        assertEquals(1, diffs.sizeD1());
        assertEquals(0, diffs.sizeD2(0));
        underlying.addD2(0);
        assertEquals(1, diffs.sizeD2(0));
        assertEquals(0, diffs.sizeD3(0, 0));
        underlying.addD3(0, 0, 4);
        underlying.addD3(0, 0, 4);
        assertEquals(2, diffs.sizeD3(0, 0));

    }

    /*
     * Test method for 'freerails.util.List3DDiff.uGet(int...)'
     */
    public void testUGet() {
        underlying.addD1();
        underlying.addD2(0);
        underlying.addD3(0, 0, 1);
        assertEquals(1, diffs.uGet(0, 0, 0));
    }

    enum listid {
        test
    }

}
