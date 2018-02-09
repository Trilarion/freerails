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
 * TrackConfigurationTest.java
 * JUnit based test
 *
 *Tests that adding and removing track sections from a configuration.
 */
package freerails.world.track;

import freerails.world.terrain.TileTransition;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashSet;

/**
 *
 */
public class TrackConfigurationTest extends TestCase {

    /**
     *
     */
    public void testAdd() {
        TrackConfiguration a = TrackConfiguration.getFlatInstance("000010000");
        TrackConfiguration b = TrackConfiguration.add(a, TileTransition.NORTH_WEST);
        assertEquals(TrackConfiguration.getFlatInstance("100010000"), b);
        assertFalse(a == b);
    }

    /**
     *
     */
    public void testGet8And9bitTemplate() {
        for (int i = 0; i < 512; i++) {
            TrackConfiguration tc = TrackConfiguration.from9bitTemplate(i);
            assertEquals(i, tc.get9bitTemplate());
        }
        for (TileTransition v : TileTransition.getList()) {
            TrackConfiguration tc = TrackConfiguration.getFlatInstance(v);
            assertEquals(v.get9bitTemplate(), tc.get9bitTemplate());
            assertEquals(v.get8bitTemplate(), tc.get8bitTemplate());
            TrackConfiguration tc2 = TrackConfiguration.from9bitTemplate(v.get9bitTemplate());
            assertEquals(tc, tc2);
        }

    }

    /**
     *
     */
    public void testGetLength() {
        TrackConfiguration a = TrackConfiguration.getFlatInstance("010010000");
        TrackConfiguration b = TrackConfiguration.getFlatInstance("010010010");
        assertEquals(30, a.getLength());
        assertEquals(60, b.getLength());
    }

    /**
     *
     */
    public void testSubtract() {
        TrackConfiguration a = TrackConfiguration.getFlatInstance("100010000");
        TrackConfiguration b = TrackConfiguration.subtract(a, TileTransition.NORTH_WEST);
        assertEquals(TrackConfiguration.getFlatInstance("000010000"), b);
    }

    /**
     *
     */
    public void testToString() {
        TrackConfiguration a = TrackConfiguration.getFlatInstance("100010000");
        assertEquals("tile center, north west", a.toString());
        a = TrackConfiguration.getFlatInstance(TileTransition.NORTH_WEST);
        assertEquals("no tile center, north west", a.toString());
        a = TrackConfiguration.getFlatInstance("000010000");
        assertEquals("tile center", a.toString());
        a = TrackConfiguration.getFlatInstance("000000000");
        assertEquals("no tile center", a.toString());

        // Check that no two track configurations have the same String representation.
        HashSet<String> strings = new HashSet<>();

        for (int i = 0; i < 512; i++) {
            TrackConfiguration test = TrackConfiguration.from9bitTemplate(i);
            String toString = test.toString();

            if (strings.contains(toString)) {
                fail(toString + ' ' + i);
            }
            strings.add(toString);
        }
    }
}