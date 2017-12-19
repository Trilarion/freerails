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
 * Created on 18-Jul-2005
 *
 */
package freerails.world.train;

import junit.framework.TestCase;

import java.util.Random;

/**
 *
 */
public class CompositeSpeedAgainstTimeTest extends TestCase {

    /**
     *
     */
    public void testBounds() {
        SpeedAgainstTime sat = ConstAcc.uas(10, 2, 400d);
        CompositeSpeedAgainstTime csat = new CompositeSpeedAgainstTime(sat);
        double t = csat.duration();
        double t2 = csat.calcT(400d);
        assertEquals(t, t2);
        double s = csat.calcS(t);
        assertEquals(400d, s);
        double t3 = csat.calcT(0d);
        assertEquals(0d, t3);
    }

    /**
     *
     */
    public void testContract() {
        Random r = new Random(123);
        for (int i = 0; i < 1000; i++) {
            int numberOfParts = r.nextInt(10) + 1;
            SpeedAgainstTime[] parts = new SpeedAgainstTime[numberOfParts];
            for (int j = 0; j < numberOfParts; j++) {
                parts[j] = ConstAcc.uat(r.nextDouble(), r.nextDouble(), r
                        .nextDouble());
            }
            CompositeSpeedAgainstTime csat = new CompositeSpeedAgainstTime(
                    parts);
            ConstAccTest.checkContract(csat);
        }
    }

}
