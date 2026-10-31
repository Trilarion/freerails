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

package freerails.util;

import org.junit.Assert;

import java.io.Serializable;


/**
 * Utils for testing.
 */
public final class TestUtils {

    /**
     * Creates a test utils.
     */
    private TestUtils() {}

    /**
     * Asserts clone by serialization behaves well.
     * @param serializable the serializable to use in the test.
     */
    public static void assertCloneBySerializationBehavesWell(Serializable serializable) {
        Assert.assertEquals(serializable, serializable);
        Serializable copy = Utils.cloneBySerialisation(serializable);
        Assert.assertEquals(copy, copy);
        Assert.assertEquals(serializable, copy);

        Assert.assertEquals(serializable.hashCode(), copy.hashCode());
    }

    /**
     * Asserts unequal and no hashcode collision.
     * @param a the a to use in the test.
     * @param b the b to use in the test.
     */
    public static void assertUnequalAndNoHashcodeCollision(Object a, Object b) {
        Assert.assertNotEquals(a, b);
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
    }

    /**
     * Asserts throws.
     * @param action the action to use in the test.
     */
    public static void assertThrows(FailingRunnable action) {
        try {
            action.run();
        } catch (Exception e) {
            return;
        }
        // nothing thrown, we fail
        Assert.fail();
    }

    /**
     * Asserts line segment equals.
     * @param a the a to use in the test.
     * @param b the b to use in the test.
     * @param segment the segment to use in the test.
     */
    public static void assertLineSegmentEquals(Vector2D a, Vector2D b, Segment segment) {
        Assert.assertEquals(segment, new Segment(a, b));
    }

    /**
     * Provides test support for failing runnable.
     */
    public @FunctionalInterface interface FailingRunnable {
        /**
         * Provides test support for failing runnable.
         */
        void run() throws Exception;
    }
}
