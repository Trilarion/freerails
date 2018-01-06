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
public class ConstAccTest extends TestCase {

    /**
     * Checks the specified object satisfies the contract defined by the
     * interface SpeedAgainstTime.
     *
     * @param sat
     */
    public static void checkContract(SpeedAgainstTime sat) {
        double s = sat.getDistance();
        double ulps = Math.ulp(s);
        double t = sat.getTime();
        double ulpt = Math.ulp(t);

        // Check calculateDistance()
        checkCalcSCalcVandCalcA(sat, 0 - Double.MIN_VALUE);
        checkCalcSCalcVandCalcA(sat, t + ulpt);
        checkCalcSCalcVandCalcA(sat, 0 + Double.MIN_VALUE);
        checkCalcSCalcVandCalcA(sat, t - ulpt);
        for (double d = 0; d < 1d; d += 0.1d) {
            checkCalcSCalcVandCalcA(sat, t * d);
        }
        double actualS = sat.calculateDistance(0);
        assertEquals(0d, actualS);
        actualS = sat.calculateDistance(t);
        assertEquals(s, actualS);

        // Check calculateTime()
        checkCalcT(sat, 0 - Double.MIN_VALUE);
        checkCalcT(sat, s + ulps);
        checkCalcT(sat, 0 + Double.MIN_VALUE);
        checkCalcT(sat, s - ulps);
        for (double d = 0; d < 1d; d += 0.1d) {
            checkCalcT(sat, s * d);
        }
        double actualT = sat.calculateTime(0);
        assertEquals(0d, actualT);
        actualT = sat.calculateTime(s);
        assertEquals(t, actualT);

    }

    private static void checkCalcSCalcVandCalcA(SpeedAgainstTime sat, double t) {
        boolean exceptionExpected = (t < 0) || (t > sat.getTime());
        try {
            double actualS = sat.calculateDistance(t);
            assertTrue(actualS >= 0);
            assertTrue(actualS <= sat.getDistance());
            assertFalse(exceptionExpected);
        } catch (IllegalArgumentException e) {
            assertTrue(exceptionExpected);
        }
        // Also check getV and getA
        try {
            double v = sat.calcVelocity(t);
            assertTrue(v >= 0);
            assertFalse(exceptionExpected);
        } catch (IllegalArgumentException e) {
            assertTrue(exceptionExpected);
        }
        try {
            sat.calcAcceleration(t);
            assertFalse(exceptionExpected);
        } catch (IllegalArgumentException e) {
            assertTrue(exceptionExpected);
        }

    }

    private static void checkCalcT(SpeedAgainstTime sat, double s) {
        boolean exceptionExpected = (s < 0) || (s > sat.getDistance());
        try {
            double actualT = sat.calculateTime(s);
            assertTrue(actualT >= 0);
            assertTrue(actualT <= sat.getTime());
            assertFalse(exceptionExpected);
        } catch (IllegalArgumentException e) {
            assertTrue(exceptionExpected);
        }
    }

    /**
     *
     */
    public void testTandS() {
        SpeedAgainstTime acc1 = ConstAcc.uat(0, 10, 5);
        double s = acc1.getDistance();
        SpeedAgainstTime acc2 = ConstAcc.uas(0, 10, s);
        assertEquals(acc1, acc2);

        acc1 = ConstAcc.uat(10, 0, 5);
        assertEquals(50, acc1.getDistance(), 0.00001);
        acc2 = ConstAcc.uas(10, 0, acc1.getDistance());
        assertEquals(acc1, acc2);

    }

    /**
     *
     */
    public void testEquals() {
        SpeedAgainstTime acc1 = ConstAcc.uat(0, 10, 4);
        SpeedAgainstTime acc2 = ConstAcc.uat(0, 10, 4);
        assertEquals(acc1, acc2);
    }

    /**
     *
     */
    public void testContract() {
        Random r = new Random(88);
        for (int i = 0; i < 1000; i++) {
            ConstAcc acc1 = ConstAcc.uat(r.nextDouble(), r.nextDouble(), r
                    .nextDouble());
            checkContract(acc1);
            ConstAcc acc2 = ConstAcc.uas(r.nextDouble(), r.nextDouble(), r
                    .nextDouble());
            checkContract(acc2);
        }
    }

}
