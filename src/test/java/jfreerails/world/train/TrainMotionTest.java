/*
 * Created on 24-Aug-2005
 *
 */
package jfreerails.world.train;

import jfreerails.world.common.ImPoint;
import jfreerails.world.common.Step;
import junit.framework.TestCase;

public class TrainMotionTest extends TestCase {
    /*
     * 
     * this= TrainMotion (id=49) activity= SpeedTimeAndStatus$TrainActivity
     * (id=107) name= "READY" ordinal= 1 distanceEngineWillTravel= 30.0
     * duration= 3.9936298481613424 initialPosition= 42.42640687119285 path=
     * PathOnTiles (id=48) start= ImPoint (id=73) x= 14 y= 5 vectors= ImList<E>
     * (id=75) elementData= FreerailsSerializable[2] (id=77) [0]= Step (id=79)
     * deltaX= 1 deltaY= 1 flatTrackTemplate= 256 length= 42.42640687119285 [1]=
     * Step (id=82) deltaX= 1 deltaY= 0 flatTrackTemplate= 32 length= 30.0
     * speeds= CompositeSpeedAgainstTime (id=111) duration= 10.972888751347389
     * totalDistance= 97.57359312880715 values= ImList<E> (id=114) elementData=
     * FreerailsSerializable[2] (id=118) [0]= ConstAcc (id=119) a= 0.5 dt=
     * 6.972888751347389 u= 6.5135556243263055 [1]= ConstAcc (id=121) a= 0.0 dt=
     * 4.0 u= 10.0 trainLength= 24 t= 3.9936298481613424 offset=
     * 48.42640687119287 length= 24.0
     * 
     * 72.42640687119285
     */

    public void test4Bug1266695() {
        // The figures are copied from the debugger.
        ImPoint start = new ImPoint(14, 5);
        Step[] vectors = { Step.getInstance(1, 1), Step.getInstance(1, 0) };
        PathOnTiles path = new PathOnTiles(start, vectors);

        ConstAcc constAcc0 = ConstAcc.uat(6.5135556243263055d, 0.5d,
                6.972888751347389d);
        ConstAcc constAcc1 = ConstAcc.uat(10.0, 0.0d, 4.0d);

        SpeedAgainstTime speeds = new CompositeSpeedAgainstTime(constAcc0,
                constAcc1);

        double expectedTotalDistance = 97.57359312880715d; // Copied from
                                                            // debugger.
        double actualTotalDistance = speeds.getS();

        assertEquals(expectedTotalDistance, actualTotalDistance, 0d);

        double expectedDuration = 10.972888751347389d;
        double actualDuration = speeds.getT();
        assertEquals(expectedDuration, actualDuration, 0d);

        int engineStep = 1;
        int trainLength = 24;

        TrainMotion motion = new TrainMotion(path, engineStep, trainLength,
                speeds);

        double expectedInitialPosition = 42.42640687119285;
        double actualInitialPosition = motion.getInitialPosition();
        assertEquals(expectedInitialPosition, actualInitialPosition, 0d);

        // Different from above
        double tooLongDuration = 3.9936298481613424d;
        actualDuration = motion.duration();

        assertTrue(tooLongDuration > actualDuration);

        // This method used to throw an exception
        @SuppressWarnings("unused")
        Object o = motion.getState(actualDuration);

    }

}
