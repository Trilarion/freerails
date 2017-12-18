/*
 * Created on 21-Jul-2005
 *
 */
package freerails.util;

import junit.framework.TestCase;

/**
 *
 * @author jkeller1
 */
public class ListXDTest extends TestCase {

    List1D<Object> list1d;
    List2D<Object> list2d;
    List3D<Object> list3d;

    /**
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        list1d = new List1DImpl<>();
        list2d = new List2DImpl<>(5);
        list3d = new List3DImpl<>(3, 2);
    }

    /**
     *
     */
    public void testAdd() {
        // Test initial size.
        assertEquals(0, list1d.size());
        assertEquals(5, list2d.sizeD1());
        assertEquals(0, list2d.sizeD2(0));

        // Add an object
        Integer i = 4;
        assertEquals(0, list1d.add(i));
        assertEquals(0, list2d.addD2(2, i));

        assertEquals(1, list1d.size());
        assertEquals(5, list2d.sizeD1());
        assertEquals(1, list2d.sizeD2(2));
        assertEquals(0, list2d.sizeD2(0));

    }

    /**
     *
     */
    public void testRemove() {
        Integer i = 4;

        list2d.addD2(4, i);
        try {
            list2d.removeLastD1();
            fail();
        } catch (Exception e) {
            // An exception should be thrown since the list we are trying to
            // remove is not empty.
        }

        list3d.addD3(2, 1, i);
        // We now should be able to remove the last
        try {
            list3d.removeLastD1();
            fail();
        } catch (Exception e) {
            // An exception should be thrown since the list we are trying to
            // remove is not empty.
        }

        try {
            list3d.removeLastD2(3);
            fail();
        } catch (Exception e) {
            // An exception should be thrown since the list we are trying to
            // remove is not empty.
        }
    }

    /**
     *
     */
    public void testHashCodeAndEquals() {
        Integer i = 5;
        Integer ii = 53;

        // 1d
        list1d.add(i);
        Object copy = Utils.cloneBySerialisation(list1d);
        assertEquals(copy, list1d);
        assertEquals(copy.hashCode(), list1d.hashCode());
        list1d.add(ii);
        assertFalse(copy.equals(list1d));

        // 2d
        list2d.addD2(0, i);
        copy = Utils.cloneBySerialisation(list2d);
        assertEquals(copy, list2d);
        assertEquals(copy.hashCode(), list2d.hashCode());
        list2d.addD2(0, ii);
        assertFalse(copy.equals(list2d));

        // 3d
        list3d.addD3(0, 1, i);
        copy = Utils.cloneBySerialisation(list3d);
        assertEquals(copy, list3d);
        assertEquals(copy.hashCode(), list3d.hashCode());
        list3d.addD3(0, 1, ii);
        assertFalse(copy.equals(list3d));
    }

    /**
     *
     */
    public void test3DList() {
        list3d = new List3DImpl<>(0, 0);

        // Add a player
        int playerId = list3d.addD1();
        list3d.addD2(playerId);
        list3d.addD2(playerId);
        list3d.addD2(playerId);

        // Then remove them
        while (list3d.sizeD2(playerId) > 0) {
            list3d.removeLastD2(playerId);
        }

    }

}
