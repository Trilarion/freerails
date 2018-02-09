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
 *
 */
package freerails.world.train;

import junit.framework.TestCase;

import java.util.Random;

/**
 *
 */
public class ConstantAccelerationMotionTest extends TestCase {

    /**
     *
     */
    public void testTandS() {
        Motion accelerationMotion1 = ConstantAccelerationMotion.fromSpeedAccelerationTime(0, 10, 5);

        double distance1 = accelerationMotion1.getTotalDistance();
        Motion accelerationMotion2 = ConstantAccelerationMotion.fromSpeedAccelerationDistance(0, 10, distance1);
        assertEquals(accelerationMotion1, accelerationMotion2);

        accelerationMotion1 = ConstantAccelerationMotion.fromSpeedAccelerationTime(10, 0, 5);
        assertEquals(50, accelerationMotion1.getTotalDistance(), 0.00001);

        accelerationMotion2 = ConstantAccelerationMotion.fromSpeedAccelerationDistance(10, 0, accelerationMotion1.getTotalDistance());
        assertEquals(accelerationMotion1, accelerationMotion2);
    }

    /**
     *
     */
    public void testEquals() {
        Motion accelerationMotion1 = ConstantAccelerationMotion.fromSpeedAccelerationTime(0, 10, 4);
        Motion accelerationMotion2 = ConstantAccelerationMotion.fromSpeedAccelerationTime(0, 10, 4);
        assertEquals(accelerationMotion1, accelerationMotion2);
    }

    /**
     *
     */
    public void testContract() {
        Random r = new Random(88);
        for (int i = 0; i < 2000; i++) {
            ConstantAccelerationMotion acc1 = ConstantAccelerationMotion.fromSpeedAccelerationTime(r.nextDouble(), r.nextDouble(), r.nextDouble());
            checkContract(acc1);
            ConstantAccelerationMotion acc2 = ConstantAccelerationMotion.fromSpeedAccelerationDistance(r.nextDouble(), r.nextDouble(), r.nextDouble());
            checkContract(acc2);
        }
    }

    /**
     * Checks the specified object satisfies the contract defined by the
     * interface Motion.
     *
     * @param sat
     */
    public static void checkContract(Motion sat) {
        double s = sat.getTotalDistance();
        double ulps = Math.ulp(s);
        double t = sat.getTotalTime();
        double ulpt = Math.ulp(t);

        // Check calculateDistance()
        checkCalcSCalcVandCalcA(sat, 0 - Double.MIN_VALUE);
        checkCalcSCalcVandCalcA(sat, t + ulpt);
        checkCalcSCalcVandCalcA(sat, 0 + Double.MIN_VALUE);
        checkCalcSCalcVandCalcA(sat, t - ulpt);
        for (double d = 0; d < 1.0d; d += 0.1d) {
            checkCalcSCalcVandCalcA(sat, t * d);
        }
        double actualS = sat.calculateDistanceAtTime(0);
        assertEquals(0.0d, actualS);
        actualS = sat.calculateDistanceAtTime(t);
        assertEquals(s, actualS);

        // Check calculateTime()
        checkCalcT(sat, 0 - Double.MIN_VALUE);
        checkCalcT(sat, s + ulps);
        checkCalcT(sat, 0 + Double.MIN_VALUE);
        checkCalcT(sat, s - ulps);
        for (double d = 0; d < 1.0d; d += 0.1d) {
            checkCalcT(sat, s * d);
        }
        double actualT = sat.calculateTimeAtDistance(0);
        assertEquals(0.0d, actualT);
        actualT = sat.calculateTimeAtDistance(s);
        assertEquals(t, actualT);
    }

    /**
     *
     * @param sat
     * @param t
     */
    private static void checkCalcSCalcVandCalcA(Motion sat, double t) {
        boolean exceptionExpected = (t < 0) || (t > sat.getTotalTime());
        try {
            double actualS = sat.calculateDistanceAtTime(t);
            assertTrue(actualS >= 0);
            assertTrue(actualS <= sat.getTotalDistance());
            assertFalse(exceptionExpected);
        } catch (IllegalArgumentException e) {
            assertTrue(exceptionExpected);
        }
        // Also check getV and getA
        try {
            double v = sat.calculateSpeedAtTime(t);
            assertTrue(v >= 0);
            assertFalse(exceptionExpected);
        } catch (IllegalArgumentException e) {
            assertTrue(exceptionExpected);
        }
        try {
            sat.calculateAccelerationAtTime(t);
            assertFalse(exceptionExpected);
        } catch (IllegalArgumentException e) {
            assertTrue(exceptionExpected);
        }
    }

    /**
     *
     * @param sat
     * @param s
     */
    private static void checkCalcT(Motion sat, double s) {
        boolean exceptionExpected = (s < 0) || (s > sat.getTotalDistance());
        try {
            double actualT = sat.calculateTimeAtDistance(s);
            assertTrue(actualT >= 0);
            assertTrue(actualT <= sat.getTotalTime());
            assertFalse(exceptionExpected);
        } catch (IllegalArgumentException e) {
            assertTrue(exceptionExpected);
        }
    }

}
