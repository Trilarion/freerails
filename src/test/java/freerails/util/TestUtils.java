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

package freerails.util;

import org.junit.Assert;

import java.io.Serializable;


/**
 * Utils for testing.
 */
public final class TestUtils {

    private TestUtils() {}

    /**
     *
     * @param serializable
     */
    public static void assertCloneBySerializationBehavesWell(Serializable serializable) {
        Assert.assertEquals(serializable, serializable);
        Serializable copy = Utils.cloneBySerialisation(serializable);
        Assert.assertEquals(copy, copy);
        Assert.assertEquals(serializable, copy);

        Assert.assertEquals(serializable.hashCode(), copy.hashCode());
    }

    /**
     *
     * @param a
     * @param b
     */
    public static void assertUnequalAndNoHashcodeCollision(Object a, Object b) {
        Assert.assertNotEquals(a, b);
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
    }

    /**
     *
     * @param action
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

    public static void assertLineSegmentEquals(int x1, int y1, int x2, int y2, LineSegment segment) {
        Assert.assertEquals(segment, new LineSegment(x1, y1, x2, y2));
    }

    public @FunctionalInterface interface FailingRunnable {
        void run() throws Exception;
    }
}
