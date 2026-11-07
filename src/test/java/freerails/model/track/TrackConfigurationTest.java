/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
import freerails.util.TestUtils;
import junit.framework.TestCase;

import java.util.HashSet;

/**
 * Tests track configuration behavior.
 */
public class TrackConfigurationTest extends TestCase {

    /**
     * Tests add behavior.
     */
    public void testAdd() {
        TrackConfiguration a = TrackConfiguration.from8bitTemplate(0);
        TrackConfiguration b = TrackConfiguration.add(a, TileTransition.NORTH_WEST);
        assertEquals(TrackConfiguration.getFlatInstance("10000000"), b);
        assertFalse(a.equals(b));
    }

    /**
     * Tests canonical configuration masks and directional transition masks.
     */
    public void testConfigurationAndTransitionMasks() {
        for (int i = 0; i < 256; i++) {
            TrackConfiguration trackConfiguration = TrackConfiguration.from8bitTemplate(i);
            assertEquals(i, trackConfiguration.get8bitTemplate());
        }
        for (TileTransition tileTransition : TileTransition.getTransitions()) {
            TrackConfiguration trackConfiguration1 = TrackConfiguration.getFlatInstance(tileTransition);
            assertEquals(tileTransition.get8bitTemplate(), trackConfiguration1.get8bitTemplate());

            TrackConfiguration trackConfiguration2 = TrackConfiguration.from8bitTemplate(tileTransition.get8bitTemplate());
            assertEquals(trackConfiguration1, trackConfiguration2);
        }
    }

    /**
     * Tests binary strings use ordinary eight-bit order and reject legacy-width inputs.
     */
    public void testBinaryStringUsesStandardBitOrder() {
        assertEquals(1, TrackConfiguration.getFlatInstance("00000001").getConfiguration());
        assertEquals(128, TrackConfiguration.getFlatInstance("10000000").getConfiguration());
        assertEquals(TrackConfiguration.from8bitTemplate(0b10000001),
                TrackConfiguration.getFlatInstance("10000001"));

        TestUtils.assertThrows(NumberFormatException.class,
                () -> TrackConfiguration.getFlatInstance("000010000"));
    }

    /**
     * Tests directional masks follow compass order clockwise from north.
     */
    public void testDirectionalTemplatesMatchCompassOrder() {
        for (TileTransition direction : TileTransition.getTransitions()) {
            assertEquals(1 << direction.getID(), direction.get8bitTemplate());
        }
    }

    /**
     * Tests get length behavior.
     */
    public void testGetLength() {
        TrackConfiguration trackConfiguration = TrackConfiguration.getFlatInstance("00000001");
        assertEquals(100, trackConfiguration.getLength());
        trackConfiguration = TrackConfiguration.getFlatInstance("00010001");
        assertEquals(200, trackConfiguration.getLength());
    }

    /**
     * Tests subtract behavior.
     */
    public void testSubtract() {
        TrackConfiguration trackConfiguration1 = TrackConfiguration.getFlatInstance("10000000");
        TrackConfiguration trackConfiguration2 = TrackConfiguration.subtract(trackConfiguration1, TileTransition.NORTH_WEST);
        assertEquals(TrackConfiguration.from8bitTemplate(0), trackConfiguration2);
    }

    /**
     * Tests rotations preserve directional connections and return after full cycle behavior.
     */
    public void testRotationsPreserveDirectionalConnectionsAndReturnAfterFullCycle() {
        int[] pieces = {
                TileTransition.NORTH.get8bitTemplate(),
                TileTransition.NORTH.get8bitTemplate() | TileTransition.SOUTH.get8bitTemplate(),
                TileTransition.NORTH.get8bitTemplate() | TileTransition.EAST.get8bitTemplate()
        };

        for (int piece : pieces) {
            int[] rotations = EightRotationsOfTrackPieceProducer.getRotations(piece);
            int expected = piece;
            for (int orientation = 0; orientation < rotations.length; orientation++) {
                assertEquals("Unexpected connections at orientation " + orientation,
                        expected, rotations[orientation]);
                expected = rotateClockwise(expected);
            }
            assertEquals("A full rotation cycle must restore the original piece.",
                    piece, expected);
        }
    }

    /**
     * Performs rotate clockwise.
     * @param configuration the configuration to use in the test.
     * @return the result of rotate clockwise.
     */
    private static int rotateClockwise(int configuration) {
        return (configuration << 1 | configuration >>> 7) & 0xFF;
    }

    /**
     * Tests to string behavior.
     */
    public void testToString() {
        TrackConfiguration trackConfiguration = TrackConfiguration.getFlatInstance("10000000");
        assertEquals("north west", trackConfiguration.toString());
        trackConfiguration = TrackConfiguration.from8bitTemplate(0);
        assertEquals("no connections", trackConfiguration.toString());

        // Check that no two track configurations have the same String representation.
        HashSet<String> strings = new HashSet<>();

        for (int i = 0; i < 256; i++) {
            trackConfiguration = TrackConfiguration.from8bitTemplate(i);
            String toString = trackConfiguration.toString();

            if (strings.contains(toString)) {
                fail(toString + ' ' + i);
            }
            strings.add(toString);
        }
    }
}