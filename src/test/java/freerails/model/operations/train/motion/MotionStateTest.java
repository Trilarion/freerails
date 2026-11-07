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

package freerails.model.operations.train.motion;

import freerails.model.operations.train.TrainState;
import freerails.util.TestUtils;
import junit.framework.TestCase;

/**
 * Tests speed time and status behavior.
 */
public class MotionStateTest extends TestCase {

    /**
     * Tests equality and hash code include all sample values behavior.
     */
    public void testEqualityAndHashCodeIncludeAllSampleValues() {
        MotionState sample = new MotionState(12.5, 0.5, 30, 2, TrainState.READY);
        MotionState equalSample = new MotionState(12.5, 0.5, 30, 2, TrainState.READY);

        assertEquals(sample, sample);
        assertEquals(sample, equalSample);
        assertEquals(equalSample, sample);
        assertEquals(sample.hashCode(), equalSample.hashCode());

        assertNotEqual(sample, new MotionState(13, 0.5, 30, 2, TrainState.READY));
        assertNotEqual(sample, new MotionState(12.5, 1, 30, 2, TrainState.READY));
        assertNotEqual(sample, new MotionState(12.5, 0.5, 31, 2, TrainState.READY));
        assertNotEqual(sample, new MotionState(12.5, 0.5, 30, 3, TrainState.READY));
        assertNotEqual(sample, new MotionState(12.5, 0.5, 30, 2, TrainState.CRASHED));
        assertFalse(sample.equals(null));
        assertFalse(sample.equals("sample"));
    }

    /**
     * Tests constructor rejects negative time and null state behavior.
     */
    public void testConstructorRejectsNegativeTimeAndNullState() {
        IllegalArgumentException negativeTime = TestUtils.assertThrows(IllegalArgumentException.class,
                () -> new MotionState(0, 0, 0, -1, TrainState.READY));
        assertEquals("-1.0", negativeTime.getMessage());
        TestUtils.assertThrows(NullPointerException.class, () -> new MotionState(0, 0, 0, 0, null));
    }

    /**
     * Asserts not equal.
     * @param first the first to use in the test.
     * @param second the second to use in the test.
     */
    private void assertNotEqual(MotionState first, MotionState second) {
        assertFalse(first.equals(second));
        assertFalse(second.equals(first));
    }
}
