/*
 * Created on 20-Mar-2003
 *
 */
package jfreerails.world.top;

import jfreerails.world.common.FreerailsSerializable;
import junit.framework.TestCase;


/**
 * @author Luke
 *
 */
public class WorldImplTest extends TestCase {
    FreerailsSerializable fs = new FreerailsSerializable() {
        };

    public void testGet() {
        WorldImpl w = new WorldImpl();
        w.add(SKEY.TERRAIN_TYPES, fs);
        assertEquals(w.get(SKEY.TERRAIN_TYPES, 0), fs);
    }

    public void testConstructor() {
        World w = new WorldImpl();
        assertEquals("The width should be zero", 0, w.getMapWidth());
        assertEquals("The height should be zero", 0, w.getMapHeight());
    }

    /** Tests that changing the object returned by defensiveCopy() does not alter
     * the world object that was copied.
     */
    public void testDefensiveCopy() {
        World original;
        World copy;
        original = new WorldImpl();
        copy = original.defensiveCopy();
        assertNotSame("The copies should be different objects.", original, copy);
        assertEquals("The copies should be logically equal.", original, copy);

        copy.add(SKEY.TERRAIN_TYPES, fs);

        assertFalse(original.equals(copy));
        assertFalse(copy.equals(original));
        assertEquals(1, copy.size(SKEY.TERRAIN_TYPES));
        assertEquals(0, original.size(SKEY.TERRAIN_TYPES));
    }
}