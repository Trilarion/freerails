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

/*
 *
 */
package freerails.model.operations.train;

import freerails.model.operations.train.motion.TrainMotion;
import freerails.util.Vec2D;
import freerails.model.terrain.TileTransition;
import freerails.model.operations.train.motion.CompositeMotion;
import freerails.model.operations.train.motion.ConstantAccelerationMotion;
import freerails.model.operations.train.motion.Motion;
import freerails.model.ModelConstants;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

/**
 * Tests train motion behavior.
 */
public class TrainMotionTest extends TestCase {

    /**
     * Tests 4 bug1266695 behavior.
     */
    public void test4Bug1266695() {
        double modelScale = ModelConstants.MODEL_UNITS_PER_TILE / 30.0d;
        // The figures are copied from the debugger.
        Vec2D start = new Vec2D(14, 5);
        TileTransition[] vectors = {TileTransition.getInstance(new Vec2D(1, 1)), TileTransition.getInstance(new Vec2D(1, 0))};
        PathOnTiles path = new PathOnTiles(start, vectors);

        ConstantAccelerationMotion constantAccelerationMovement0 = ConstantAccelerationMotion.fromSpeedAccelerationTime(6.5135556243263055d * modelScale, 0.5d * modelScale,
                6.972888751347389d);
        ConstantAccelerationMotion constantAccelerationMovement1 = ConstantAccelerationMotion.fromSpeedAccelerationTime(10.0 * modelScale, 0.0d, 4.0d);

        Motion speeds = new CompositeMotion(constantAccelerationMovement0, constantAccelerationMovement1);

        double expectedTotalDistance = 97.57359312880715d * modelScale; // Copied from
        // debugger.
        double actualTotalDistance = speeds.getTotalDistance();

        assertEquals(expectedTotalDistance, actualTotalDistance, 1.0e-12d);

        double expectedDuration = 10.972888751347389d;
        double actualDuration = speeds.getTotalTime();
        assertEquals(expectedDuration, actualDuration, 0.0d);

        int engineStep = 1;
        int trainLength = 80;

        TrainMotion motion = new TrainMotion(path, engineStep, trainLength, speeds);

        double expectedInitialPosition = ModelConstants.MODEL_DIAGONAL_TILE_LENGTH;
        double actualInitialPosition = motion.getInitialPosition();
        assertEquals(expectedInitialPosition, actualInitialPosition, 0.0d);

        // Different from above
        double tooLongDuration = 3.9936298481613424d;
        actualDuration = motion.getDuration();

        assertTrue(tooLongDuration > actualDuration);

        // This method used to throw an exception
        motion.getStateAtTime(actualDuration);
    }

    /**
     * Tests state at time includes start and end but rejects outside interval behavior.
     */
    public void testStateAtTimeIncludesStartAndEndButRejectsOutsideInterval() {
        PathOnTiles path = new PathOnTiles(Vec2D.ZERO, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST);
        Motion speeds = ConstantAccelerationMotion.fromSpeedAccelerationTime(
                ModelConstants.MODEL_UNITS_PER_TILE / 3.0d, 0.0, 6.0);
        TrainMotion motion = new TrainMotion(path, 1, 80, speeds);
        double duration = motion.getDuration();

        assertEquals(0.0, motion.getDistance(0.0), 0.0);
        assertTrue(motion.getStateAtTime(0.0).getLength() > 0);
        assertEquals(ModelConstants.MODEL_UNITS_PER_TILE / 3.0d * (duration - 0.001), motion.getDistance(duration - 0.001), 0.000001);
        assertTrue(motion.getStateAtTime(duration - 0.001).getLength() > 0);
        assertEquals(path.getFinalPosition(), motion.getFinalPosition());
        assertEquals(200.0, motion.getDistance(duration), 0.0);
        assertTrue(motion.getStateAtTime(duration).getLength() > 0);
        assertThrowsIllegalArgument(() -> motion.getStateAtTime(-0.001));
        assertThrowsIllegalArgument(() -> motion.getStateAtTime(duration + 0.001));
    }

    /**
     * Tests legacy serialized motion is scaled to current model units behavior.
     * @throws Exception if the test legacy serialized motion is scaled to current model units operation fails.
     */
    public void testLegacySerializedMotionIsScaledToCurrentModelUnits() throws Exception {
        PathOnTiles path = new PathOnTiles(Vec2D.ZERO, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST);
        TrainMotion legacyMotion = new TrainMotion(path, 80, 6, freerails.model.operations.train.TrainState.READY);
        setField(legacyMotion, "modelUnitsPerTile", 0);
        setField(legacyMotion, "trainLength", 24);
        setField(legacyMotion, "initialPosition", 30.0);
        setField(legacyMotion, "distanceEngineWillTravel", 60.0);
        setField(legacyMotion, "speeds", ConstantAccelerationMotion.fromSpeedAccelerationTime(10.0, 0.0, 6.0));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(legacyMotion);
        }

        TrainMotion migrated;
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            migrated = (TrainMotion) input.readObject();
        }

        assertEquals(80, migrated.getTrainLength());
        assertEquals(100.0, migrated.getInitialPosition(), 0.0);
        assertEquals(200.0, migrated.getDistance(6.0), 0.0);
    }

    /**
     * Sets field.
     * @param target the target used by this test.
     * @param name the name used by this test.
     * @param value the value used by this test.
     * @throws Exception if the operation fails.
     */
    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Asserts throws illegal argument.
     * @param action the action to use in the test.
     */
    private static void assertThrowsIllegalArgument(Runnable action) {
        try {
            action.run();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // The queried time lies outside the motion interval.
        }
    }
}
