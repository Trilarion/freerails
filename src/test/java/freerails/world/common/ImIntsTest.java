/*
 * Created on 06-Jul-2005
 *
 */
package freerails.world.common;

import freerails.util.Utils;
import junit.framework.TestCase;

public class ImIntsTest extends TestCase {

    /*
     * Test method for 'freerails.world.common.ImInts.append(int...)'
     */
    public void testAppend() {

        int[] a = { 1, 2, 3 };
        int[] b = { 4, 5, 6, 7 };
        int[] c = { 1, 2, 3, 4, 5, 6, 7 };
        ImInts ai = new ImInts(a);
        ImInts ci = new ImInts(c);
        assertFalse(ci.equals(ai));
        assertEquals(ci, ai.append(b));

    }

    public void testRemoveLast() {
        // Test method does not change original
        ImInts original = new ImInts(1, 2, 3, 4);
        ImInts clone = (ImInts) Utils.cloneBySerialisation(original);

        assertEquals(original, clone);
        original.removeLast();
        assertEquals(original, clone);

        ImInts actual, expected;
        actual = (new ImInts(1, 2, 3)).removeLast();
        expected = new ImInts(1, 2);
        assertEquals(expected, actual);

        actual = (new ImInts(1, 2)).removeLast();
        expected = new ImInts(1);
        assertEquals(expected, actual);

        actual = (new ImInts(1, 2, 4, 3)).removeLast();
        expected = new ImInts(1, 2, 4);
        assertEquals(expected, actual);
    }

    public void testEquals() {
        int[] a = { 1, 2, 3 };
        int[] b = { 1, 2, 3 };
        ImInts ai = new ImInts(a);
        ImInts bi = new ImInts(b);
        assertEquals(ai, bi);
        ImInts ci = new ImInts(1, 2, 3);
        assertEquals(ai, ci);

    }

}
