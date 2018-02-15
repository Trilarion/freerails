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
package freerails.model.track;

import freerails.model.terrain.TileTransition;
import junit.framework.TestCase;

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
        assertFalse(a.equals(b));
    }

    /**
     *
     */
    public void testGet8And9bitTemplate() {
        for (int i = 0; i < 512; i++) {
            TrackConfiguration trackConfiguration = TrackConfiguration.from9bitTemplate(i);
            assertEquals(i, trackConfiguration.get9bitTemplate());
        }
        for (TileTransition tileTransition : TileTransition.getTransitions()) {
            TrackConfiguration trackConfiguration1 = TrackConfiguration.getFlatInstance(tileTransition);
            assertEquals(tileTransition.get9bitTemplate(), trackConfiguration1.get9bitTemplate());
            assertEquals(tileTransition.get8bitTemplate(), trackConfiguration1.get8bitTemplate());

            TrackConfiguration trackConfiguration2 = TrackConfiguration.from9bitTemplate(tileTransition.get9bitTemplate());
            assertEquals(trackConfiguration1, trackConfiguration2);
        }
    }

    /**
     *
     */
    public void testGetLength() {
        TrackConfiguration trackConfiguration = TrackConfiguration.getFlatInstance("010010000");
        assertEquals(30, trackConfiguration.getLength());
        trackConfiguration = TrackConfiguration.getFlatInstance("010010010");
        assertEquals(60, trackConfiguration.getLength());
    }

    /**
     *
     */
    public void testSubtract() {
        TrackConfiguration trackConfiguration1 = TrackConfiguration.getFlatInstance("100010000");
        TrackConfiguration trackConfiguration2 = TrackConfiguration.subtract(trackConfiguration1, TileTransition.NORTH_WEST);
        assertEquals(TrackConfiguration.getFlatInstance("000010000"), trackConfiguration2);
    }

    /**
     *
     */
    public void testToString() {
        TrackConfiguration trackConfiguration = TrackConfiguration.getFlatInstance("100010000");
        assertEquals("tile center, north west", trackConfiguration.toString());
        trackConfiguration = TrackConfiguration.getFlatInstance(TileTransition.NORTH_WEST);
        assertEquals("no tile center, north west", trackConfiguration.toString());
        trackConfiguration = TrackConfiguration.getFlatInstance("000010000");
        assertEquals("tile center", trackConfiguration.toString());
        trackConfiguration = TrackConfiguration.getFlatInstance("000000000");
        assertEquals("no tile center", trackConfiguration.toString());

        // Check that no two track configurations have the same String representation.
        HashSet<String> strings = new HashSet<>();

        for (int i = 0; i < 512; i++) {
            trackConfiguration = TrackConfiguration.from9bitTemplate(i);
            String toString = trackConfiguration.toString();

            if (strings.contains(toString)) {
                fail(toString + ' ' + i);
            }
            strings.add(toString);
        }
    }
}