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

import freerails.model.train.motion.TrainMotion;
import freerails.util.BidirectionalIterator;
import freerails.model.game.Time;
import freerails.model.player.Player;
import freerails.model.world.TestTrainMotion;
import freerails.model.world.World;

/**
 *
 */
public class AddActivityMoveTest extends AbstractMoveTestCase {

    /**
     *
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
     *
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

        assertEquals(bidirectionalIterator.get(), act);
        assertNotSame(act, bidirectionalIterator.get());
        assertEquals(Double.MIN_VALUE, act.getStartTime(), 0.00001);
        assertEquals(bidirectionalIterator.get().getStartTime(), currentTime.ticks(), 0.00001);
        assertEquals(50.0d, bidirectionalIterator.get().getDuration(), 0.00001);
        assertEquals(50.0d, bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration(), 0.00001);

        assertTrue(bidirectionalIterator.hasNext());
        bidirectionalIterator.next();
        assertEquals(bidirectionalIterator.get(), act2);
        assertNotSame(act2, bidirectionalIterator.get());
        assertEquals(Double.MIN_VALUE, act2.getStartTime(), 0.00001);
        assertEquals(50, bidirectionalIterator.get().getStartTime(), 0.00001);
        assertEquals(60, bidirectionalIterator.get().getDuration(), 0.0001d);
        assertEquals(110, bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration(), 0.00001);
    }

}
