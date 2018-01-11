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

import freerails.util.Point2D;
import freerails.world.terrain.TileTransition;
import junit.framework.TestCase;

/**
 *
 */
public class TrainMotionTest extends TestCase {
    /*
     *
     * this= TrainMotion (id=49) activity= SpeedTimeAndStatus$TrainActivity
     * (id=107) name= "READY" ordinal= 1 distanceEngineWillTravel= 30.0
     * duration= 3.9936298481613424 initialPosition= 42.42640687119285 path=
     * PathOnTiles (id=48) start= Point2D (id=73) x= 14 y= 5 vectors= ImmutableList<E>
     * (id=75) elementData= FreerailsSerializable[2] (id=77) [0]= TileTransition (id=79)
     * deltaX= 1 deltaY= 1 flatTrackTemplate= 256 length= 42.42640687119285 [1]=
     * TileTransition (id=82) deltaX= 1 deltaY= 0 flatTrackTemplate= 32 length= 30.0
     * speeds= CompositeSpeedAgainstTime (id=111) duration= 10.972888751347389
     * totalDistance= 97.57359312880715 values= ImmutableList<E> (id=114) elementData=
     * FreerailsSerializable[2] (id=118) [0]= ConstantAcceleration (id=119) a= 0.5 dt=
     * 6.972888751347389 u= 6.5135556243263055 [1]= ConstantAcceleration (id=121) a= 0.0 dt=
     * 4.0 u= 10.0 trainLength= 24 t= 3.9936298481613424 offset=
     * 48.42640687119287 length= 24.0
     *
     * 72.42640687119285
     */

    /**
     *
     */


    public void test4Bug1266695() {
        // The figures are copied from the debugger.
        Point2D start = new Point2D(14, 5);
        TileTransition[] vectors = {TileTransition.getInstance(1, 1), TileTransition.getInstance(1, 0)};
        PathOnTiles path = new PathOnTiles(start, vectors);

        ConstantAcceleration constantAcceleration0 = ConstantAcceleration.uat(6.5135556243263055d, 0.5d,
                6.972888751347389d);
        ConstantAcceleration constantAcceleration1 = ConstantAcceleration.uat(10.0, 0.0d, 4.0d);

        SpeedAgainstTime speeds = new CompositeSpeedAgainstTime(constantAcceleration0,
                constantAcceleration1);

        double expectedTotalDistance = 97.57359312880715d; // Copied from
        // debugger.
        double actualTotalDistance = speeds.getDistance();

        assertEquals(expectedTotalDistance, actualTotalDistance, 0.0d);

        double expectedDuration = 10.972888751347389d;
        double actualDuration = speeds.getTime();
        assertEquals(expectedDuration, actualDuration, 0.0d);

        int engineStep = 1;
        int trainLength = 24;

        TrainMotion motion = new TrainMotion(path, engineStep, trainLength,
                speeds);

        double expectedInitialPosition = 42.42640687119285;
        double actualInitialPosition = motion.getInitialPosition();
        assertEquals(expectedInitialPosition, actualInitialPosition, 0.0d);

        // Different from above
        double tooLongDuration = 3.9936298481613424d;
        actualDuration = motion.duration();

        assertTrue(tooLongDuration > actualDuration);

        // This method used to throw an exception
        @SuppressWarnings("unused")
        Object o = motion.getState(actualDuration);

    }

}
