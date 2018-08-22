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
package freerails.move;

import freerails.model.train.activity.Activity;
import freerails.util.BidirectionalIterator;
import freerails.model.game.Time;
import freerails.model.player.Player;
import freerails.model.world.TestActivity;
import freerails.model.world.World;

/**
 *
 */
public class NextActivityMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    public void testMove() {
        World world = getWorld();
        Player player = getPlayer();
        Activity act = new TestActivity(50);
        world.addActivity(player, 0, act);

        Activity act2 = new TestActivity(60);
        Move move = new NextActivityMove(act2, 0, player);
        assertSurvivesSerialisation(move);
        assertOkAndRepeatable(move);
    }

    /**
     *
     */
    public void testStackingOfActivities() {
        World world = getWorld();
        Player player = getPlayer();
        Activity act = new TestActivity(50);
        world.addActivity(player, 0, act);

        Activity act2 = new TestActivity(60);
        Move move = new NextActivityMove(act2, 0, player);
        assertMoveApplyIsOk(move);

        Time currentTime = new Time(0);
        assertEquals(currentTime, world.getClock().getCurrentTime());
        BidirectionalIterator<Activity> bidirectionalIterator = world.getTrain(player, 0).getActivities();

        assertEquals(bidirectionalIterator.get(), act);
        assertEquals(bidirectionalIterator.get().getStartTime(), currentTime.getTicks(), 0.00001);
        assertEquals(50.0d, bidirectionalIterator.get().getDuration(), 0.00001);
        assertEquals(50.0d, bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration(), 0.00001);

        assertTrue(bidirectionalIterator.hasNext());
        bidirectionalIterator.next();
        assertEquals(bidirectionalIterator.get(), act2);
        assertEquals(50, bidirectionalIterator.get().getStartTime(), 0.00001);
        assertEquals(60, bidirectionalIterator.get().getDuration(), 0.0001d);
        assertEquals(110, bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration(), 0.00001);
    }

}
