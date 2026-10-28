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

package freerails.model.train.motion;

import freerails.model.train.TrainState;
import junit.framework.TestCase;

public class SpeedTimeAndStatusTest extends TestCase {

    public void testEqualityAndHashCodeIncludeAllSampleValues() {
        SpeedTimeAndStatus sample = new SpeedTimeAndStatus(12.5, 0.5, 30, 2, TrainState.READY);
        SpeedTimeAndStatus equalSample = new SpeedTimeAndStatus(12.5, 0.5, 30, 2, TrainState.READY);

        assertEquals(sample, sample);
        assertEquals(sample, equalSample);
        assertEquals(equalSample, sample);
        assertEquals(sample.hashCode(), equalSample.hashCode());

        assertNotEqual(sample, new SpeedTimeAndStatus(13, 0.5, 30, 2, TrainState.READY));
        assertNotEqual(sample, new SpeedTimeAndStatus(12.5, 1, 30, 2, TrainState.READY));
        assertNotEqual(sample, new SpeedTimeAndStatus(12.5, 0.5, 31, 2, TrainState.READY));
        assertNotEqual(sample, new SpeedTimeAndStatus(12.5, 0.5, 30, 3, TrainState.READY));
        assertNotEqual(sample, new SpeedTimeAndStatus(12.5, 0.5, 30, 2, TrainState.CRASHED));
        assertFalse(sample.equals(null));
        assertFalse(sample.equals("sample"));
    }

    public void testConstructorRejectsNegativeTimeAndNullState() {
        try {
            new SpeedTimeAndStatus(0, 0, 0, -1, TrainState.READY);
            fail("Negative time must be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("-1.0", expected.getMessage());
        }

        try {
            new SpeedTimeAndStatus(0, 0, 0, 0, null);
            fail("Null state must be rejected");
        } catch (NullPointerException expected) {
            // Expected.
        }
    }

    private void assertNotEqual(SpeedTimeAndStatus first, SpeedTimeAndStatus second) {
        assertFalse(first.equals(second));
        assertFalse(second.equals(first));
    }
}
