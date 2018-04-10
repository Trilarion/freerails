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
package freerails.model.train;

import freerails.util.Vec2D;
import freerails.model.terrain.TileTransition;
import freerails.model.train.motion.CompositeMotion;
import freerails.model.train.motion.ConstantAccelerationMotion;
import freerails.model.train.motion.Motion;
import junit.framework.TestCase;

/**
 *
 */
public class TrainMotionTest extends TestCase {

    /**
     *
     */
    public void test4Bug1266695() {
        // The figures are copied from the debugger.
        Vec2D start = new Vec2D(14, 5);
        TileTransition[] vectors = {TileTransition.getInstance(new Vec2D(1, 1)), TileTransition.getInstance(new Vec2D(1, 0))};
        PathOnTiles path = new PathOnTiles(start, vectors);

        ConstantAccelerationMotion constantAccelerationMovement0 = ConstantAccelerationMotion.fromSpeedAccelerationTime(6.5135556243263055d, 0.5d,
                6.972888751347389d);
        ConstantAccelerationMotion constantAccelerationMovement1 = ConstantAccelerationMotion.fromSpeedAccelerationTime(10.0, 0.0d, 4.0d);

        Motion speeds = new CompositeMotion(constantAccelerationMovement0, constantAccelerationMovement1);

        double expectedTotalDistance = 97.57359312880715d; // Copied from
        // debugger.
        double actualTotalDistance = speeds.getTotalDistance();

        assertEquals(expectedTotalDistance, actualTotalDistance, 0.0d);

        double expectedDuration = 10.972888751347389d;
        double actualDuration = speeds.getTotalTime();
        assertEquals(expectedDuration, actualDuration, 0.0d);

        int engineStep = 1;
        int trainLength = 24;

        TrainMotion motion = new TrainMotion(path, engineStep, trainLength, speeds);

        double expectedInitialPosition = 42.42640687119285;
        double actualInitialPosition = motion.getInitialPosition();
        assertEquals(expectedInitialPosition, actualInitialPosition, 0.0d);

        // Different from above
        double tooLongDuration = 3.9936298481613424d;
        actualDuration = motion.duration();

        assertTrue(tooLongDuration > actualDuration);

        // This method used to throw an exception
        Object o = motion.getStateAtTime(actualDuration);
    }
}
