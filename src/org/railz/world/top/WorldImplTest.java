/*
 * Copyright (C) 2003 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on 20-Mar-2003
 *
 */
package org.railz.world.top;

import org.railz.world.common.FreerailsSerializable;
import junit.framework.TestCase;


/**
 * @author Luke
 *
 */
public class WorldImplTest extends TestCase {
    FreerailsSerializable fs = new FreerailsSerializable() {
        };

    public void testGet() {
        World w = new WorldImpl();
        w.add(KEY.TERRAIN_TYPES, fs);
        assertEquals(w.get(KEY.TERRAIN_TYPES, 0), fs);
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

        copy.add(KEY.TERRAIN_TYPES, fs);

        assertFalse(original.equals(copy));
        assertFalse(copy.equals(original));
        assertEquals(1, copy.size(KEY.TERRAIN_TYPES));
        assertEquals(0, original.size(KEY.TERRAIN_TYPES));
    }
}
