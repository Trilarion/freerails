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
package freerails.move;

import freerails.model.operations.train.motion.TrainMotion;
import freerails.util.BidirectionalIterator;
import freerails.model.simulation.Time;
import freerails.model.player.Player;
import freerails.model.world.TestTrainMotion;
import freerails.model.world.World;

/**
 * Tests add activity move behavior.
 */
public class AddActivityMoveTest extends AbstractMoveTestCase {

    /**
     * Tests move behavior.
     */
    public void testMove() {
        World world = getWorld();
        Player player = getPlayer();
        TrainMotion act = TestTrainMotion.of(50);
        world.addActivity(player, 0, act);

        TrainMotion act2 = TestTrainMotion.of(60);
        Move move = new AddActivityMove(player, 0, act2);
        assertSurvivesSerialisation(move);
        assertOkAndRepeatable(move);
    }

    /**
     * Tests stacking of activities behavior.
     */
    public void testStackingOfActivities() {
        World world = getWorld();
        Player player = getPlayer();
        TrainMotion act = TestTrainMotion.of(50);
        world.addActivity(player, 0, act);

        TrainMotion act2 = TestTrainMotion.of(60);
        Move move = new AddActivityMove(player, 0, act2);
        assertMoveApplyIsOk(move);

        Time currentTime = new Time(0);
        assertEquals(currentTime, world.getClock().getCurrentTime());
        BidirectionalIterator<TrainMotion> bidirectionalIterator = world.getTrain(player, 0).getActivities();

        TrainMotion firstActivity = bidirectionalIterator.next();
        assertEquals(firstActivity, act);
        assertNotSame(act, firstActivity);
        assertEquals(Double.MIN_VALUE, act.getStartTime(), 0.00001);
        assertEquals(firstActivity.getStartTime(), currentTime.ticks(), 0.00001);
        assertEquals(50.0d, firstActivity.getDuration(), 0.00001);
        assertEquals(50.0d, firstActivity.getStartTime() + firstActivity.getDuration(), 0.00001);

        assertTrue(bidirectionalIterator.hasNext());
        TrainMotion secondActivity = bidirectionalIterator.next();
        assertEquals(secondActivity, act2);
        assertNotSame(act2, secondActivity);
        assertEquals(Double.MIN_VALUE, act2.getStartTime(), 0.00001);
        assertEquals(50, secondActivity.getStartTime(), 0.00001);
        assertEquals(60, secondActivity.getDuration(), 0.0001d);
        assertEquals(110, secondActivity.getStartTime() + secondActivity.getDuration(), 0.00001);
    }

}
