/*
 * Created on 27-Jul-2005
 *
 */
package jfreerails.util;

import static jfreerails.util.ListKey.Type.EndPoint;

import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.TestCase;

public class List2DDiffTest extends TestCase {

    List2D<Object> underlying;

    List2DDiff<Object> diffs;

    SortedMap<ListKey, Object> map;

    enum listid {
        test
    }

    @Override
    protected void setUp() throws Exception {
        underlying = new List2DImpl<Object>(0);
        map = new TreeMap<ListKey, Object>();
        diffs = new List2DDiff<Object>(map, underlying, listid.test);
    }

    /*
     * Test method for 'jfreerails.util.List2DDiff.sizeD1()'
     */
    public void testSizeD1() {
        assertEquals(0, diffs.sizeD1());
        underlying.addD1();
        assertEquals(1, diffs.sizeD1());
    }

    /*
     * Test method for 'jfreerails.util.List2DDiff.sizeD2(int)'
     */
    public void testSizeD2() {
        underlying.addD1();
        assertEquals(1, diffs.sizeD1());
        assertEquals(0, diffs.sizeD2(0));
        underlying.addD2(0, String.valueOf(1));
        assertEquals(1, diffs.sizeD2(0));
    }

    /*
     * Test method for 'jfreerails.util.List2DDiff.get(int, int)'
     */
    public void testGetIntInt() {
        underlying.addD1();
        underlying.addD2(0, String.valueOf(1));
        assertEquals(String.valueOf(1), underlying.get(0, 0));
        assertEquals(String.valueOf(1), diffs.get(0, 0));

    }

    /*
     * Test method for 'jfreerails.util.List2DDiff.removeLastD2(int)'
     */
    public void testRemoveLastD2() {
        underlying.addD1();
        underlying.addD2(0, String.valueOf(1));
        underlying.addD2(0, String.valueOf(2));
        Object removed = diffs.removeLastD2(0);
        assertEquals(String.valueOf(2), removed);
        assertEquals(2, underlying.sizeD2(0));
        assertEquals(2, diffs.getUnderlyingSize(0));
        assertEquals(1, map.size());
        assertEquals(1, diffs.sizeD2(0));
    }

    /*
     * Test method for 'jfreerails.util.List2DDiff.removeLastD1()'
     */
    public void testRemoveLastD1() {
        underlying.addD1();
        underlying.addD1();
        assertEquals(2, diffs.sizeD1());
        int i = diffs.removeLastD1();
        assertEquals(1, i);
        assertEquals(1, diffs.sizeD1());
    }

    /*
     * Test method for 'jfreerails.util.List2DDiff.addD1()'
     */
    public void testAddD1() {
        underlying.addD1();
        assertEquals(1, diffs.sizeD1());
        assertEquals(1, diffs.getUnderlyingSize());
        assertEquals(1, diffs.size());

        diffs.addD1();
        ListKey sizeKey = new ListKey(EndPoint, listid.test);
        assertEquals(2, map.size());
        assertTrue(map.containsKey(sizeKey));
        assertEquals(new Integer(2), map.get(sizeKey));
        assertEquals(2, diffs.sizeD1());
        diffs.addD1();
        assertEquals(3, diffs.sizeD1());
    }

    /*
     * Test method for 'jfreerails.util.List2DDiff.addD2(int, T)'
     */
    public void testAddD2() {
        underlying.addD1();
        underlying.addD2(0, String.valueOf(1));
        underlying.addD2(0, String.valueOf(2));
        assertEquals(2, diffs.sizeD2(0));
        int i = diffs.addD2(0, String.valueOf(3));
        assertEquals(2, i);
        assertEquals(3, diffs.sizeD2(0));
        i = diffs.addD2(0, String.valueOf(4));
        assertEquals(3, i);
        assertEquals(4, diffs.sizeD2(0));
        assertEquals(String.valueOf(3), diffs.get(0, 2));
        assertEquals(String.valueOf(4), diffs.get(0, 3));

    }

    /*
     * Test method for 'jfreerails.util.List2DDiff.set(int, int, T)'
     */
    public void testSetIntIntT() {
        underlying.addD1();
        underlying.addD2(0, String.valueOf(1));
        underlying.addD2(0, String.valueOf(2));
        assertEquals(String.valueOf(2), diffs.get(0, 1));
        diffs.set(0, 1, String.valueOf(22));
        assertEquals(String.valueOf(22), diffs.get(0, 1));
        diffs.addD2(0, String.valueOf(3));
        assertEquals(String.valueOf(3), diffs.get(0, 2));
        diffs.set(0, 2, String.valueOf(33));
        assertEquals(String.valueOf(33), diffs.get(0, 2));
    }

    /*
     * Test method for 'jfreerails.util.ListXDDiffs.add(int...)'
     */
    public void testAddIntArray() {
        assertEquals(0, diffs.sizeD1());
        diffs.addDimension();

        ListKey sizeKey = new ListKey(EndPoint, listid.test);

        assertEquals(
                "There should be two values: EndPoint = 0 and EndPoint[0] = 0",
                2, map.size());
        assertTrue(map.containsKey(sizeKey));
        assertEquals(new Integer(1), map.get(sizeKey));

        assertEquals(1, diffs.sizeD1());
        assertEquals(0, diffs.sizeD2(0));
        diffs.addDimension(0);
        assertEquals(1, diffs.sizeD2(0));

    }

    public void testBoundsOnSet1() {
        underlying.addD1();
        underlying.addD2(0, String.valueOf(1));
        underlying.addD2(0, String.valueOf(2));
        try {
            assertEquals(2, diffs.size(0));
            diffs.set(0, 2, String.valueOf(3));
            fail();
        } catch (Exception e) {
        }
    }

    public void testBoundsOnSet2() {
        underlying.addD1();
        underlying.addD2(0, String.valueOf(1));
        underlying.addD2(0, String.valueOf(2));
        underlying.addD2(0, String.valueOf(3));

        diffs.removeLastD2(0);
        diffs.removeLastD2(0);
        try {
            assertEquals(1, diffs.size(0));
            diffs.set(0, 2, String.valueOf(3));
            fail();
        } catch (Exception e) {
        }
    }

    public void testBoundsOnGet() {
        underlying.addD1();
        underlying.addD2(0, String.valueOf(1));
        underlying.addD2(0, String.valueOf(2));
        underlying.addD2(0, String.valueOf(3));

        diffs.removeLastD2(0);
        diffs.removeLastD2(0);
        try {
            assertEquals(1, diffs.size(0));
            diffs.get(0, 2);
            fail();
        } catch (Exception e) {
        }

    }

    public void testReverting2OriginalState1() {
        underlying.addD1();
        underlying.addD1();
        underlying.addD2(0, String.valueOf(1));
        underlying.addD2(0, String.valueOf(2));
        diffs.addD1();
        assertEquals(3, diffs.sizeD1());
        assertEquals(0, diffs.sizeD2(2));
        diffs.removeLastD1();
        assertEquals(2, diffs.sizeD1());
        assertEquals(2, underlying.sizeD1());
        assertEquals(0, map.size());

    }

    public void testReverting2OriginalState2() {
        underlying.addD1();
        underlying.addD1();
        underlying.addD2(0, String.valueOf(1));
        underlying.addD2(0, String.valueOf(2));
        diffs.addD1();
        diffs.addD2(2, String.valueOf(3));
        diffs.addD2(2, String.valueOf(33));
        assertEquals(2, diffs.sizeD2(2));

        assertEquals(String.valueOf(3), diffs.get(2, 0));
        assertEquals(String.valueOf(33), diffs.get(2, 1));

        Object removed = diffs.removeLastD2(2);
        assertEquals(String.valueOf(33), removed);

        removed = diffs.removeLastD2(2);
        assertEquals(String.valueOf(3), removed);
        assertEquals(0, diffs.sizeD2(2));

        assertEquals(3, diffs.sizeD1());
        assertEquals(0, diffs.sizeD2(2));
        diffs.removeLastD1();
        assertEquals(2, diffs.sizeD1());
        assertEquals(2, underlying.sizeD1());
        assertEquals(0, map.size());

    }

    public void testAddingElementAlreadyPresent() {
        underlying.addD1();
        underlying.addD2(0, new Integer(1));
        diffs.removeLastD2(0);
        diffs.addD2(0, new Integer(1));
        assertEquals(0, map.size());

    }

    public void testAddingNullElement() {
        underlying.addD1();
        underlying.addD2(0, null);
        diffs.removeLastD2(0);
        diffs.addD2(0, new Integer(1));
        assertEquals(1, map.size());
        diffs.removeLastD2(0);
        diffs.addD2(0, null);
        assertEquals(0, map.size());
        diffs.addD2(0, null);
    }

    public void testSettingNullElement() {
        underlying.addD1();
        underlying.addD2(0, null);
        underlying.addD2(0, new Integer(1));
        assertEquals(null, diffs.get(0, 0));
        diffs.set(0, 0, new Integer(0));
        assertEquals(new Integer(0), diffs.get(0, 0));
        assertEquals(new Integer(1), diffs.get(0, 1));
        diffs.set(0, 1, null);
        assertEquals(null, diffs.get(0, 1));

    }

}
