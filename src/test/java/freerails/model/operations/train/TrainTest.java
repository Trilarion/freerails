/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the FreeRails Team, either version 3 of the License, or
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

package freerails.model.operations.train;

import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.motion.TrainMotion;
import freerails.model.world.TestTrainMotion;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests train behavior.
 */
public class TrainTest extends TestCase {

    /**
     * Tests configuration copies and protects wagon types behavior.
     */
    public void testConfigurationCopiesAndProtectsWagonTypes() {
        List<Integer> wagonTypes = new ArrayList<>(List.of(2, 3));
        TrainConfiguration configuration = new TrainConfiguration(1, wagonTypes);

        wagonTypes.set(0, 9);
        assertEquals(List.of(2, 3), configuration.wagonTypes());
        try {
            configuration.wagonTypes().add(4);
            fail("Expected immutable wagon types");
        } catch (UnsupportedOperationException expected) {
            // Expected.
        }
    }

    /**
     * Tests replacing configuration preserves runtime state behavior.
     */
    public void testReplacingConfigurationPreservesRuntimeState() {
        Train train = new Train(1, new TrainConfiguration(2, List.of(3)));
        TrainRuntimeState runtimeState = train.getRuntimeState();
        train.setSchedule(ScheduleState.EMPTY);
        TrainConfiguration replacement = new TrainConfiguration(4, List.of(5, 6));

        train.setConfiguration(replacement);

        assertSame(replacement, train.getConfiguration());
        assertSame(runtimeState, train.getRuntimeState());
        assertEquals(ScheduleState.EMPTY, train.getSchedule());
    }

    /**
     * Tests activities start no earlier than previous completion behavior.
     */
    public void testActivitiesStartNoEarlierThanPreviousCompletion() {
        Train train = new Train(1, 1);
        TrainMotion first = TestTrainMotion.of(10, 5);
        TrainMotion delayed = TestTrainMotion.of(12, 4);
        TrainMotion later = TestTrainMotion.of(30, 2);
        TrainMotion delayedAfterLater = TestTrainMotion.of(31, 1);

        train.addActivity(first);
        train.addActivity(delayed);
        train.addActivity(later);
        train.addActivity(delayedAfterLater);

        assertEquals(10.0, first.getStartTime(), 0.0);
        assertEquals(12.0, delayed.getStartTime(), 0.0);
        assertEquals(30.0, later.getStartTime(), 0.0);
        assertEquals(31.0, delayedAfterLater.getStartTime(), 0.0);

        var activities = train.getActivities();
        assertNotSame(first, activities.get());
        assertEquals(10.0, activities.get().getStartTime(), 0.0);
        activities.next();
        assertNotSame(delayed, activities.get());
        assertEquals(15.0, activities.get().getStartTime(), 0.0);
        activities.next();
        assertNotSame(later, activities.get());
        assertEquals(30.0, activities.get().getStartTime(), 0.0);
        activities.next();
        assertNotSame(delayedAfterLater, activities.get());
        assertEquals(32.0, activities.get().getStartTime(), 0.0);

        assertEquals(10.0, train.findCurrentMotion(14).getStartTime(), 0.0);
        assertEquals(15.0, train.findCurrentMotion(15).getStartTime(), 0.0);
    }
}
