/*
 * Created on 18-Jul-2005
 *
 */
package freerails.world.train;

import java.util.Random;

import junit.framework.TestCase;

public class CompositeSpeedAgainstTimeTest extends TestCase {

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
