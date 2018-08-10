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

import freerails.model.activity.Activity;
import freerails.model.activity.ActivityIterator;
import freerails.model.game.GameTime;
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
        world.addActiveEntity(player, act);

        Activity act2 = new TestActivity(60);
        Move move = new NextActivityMove(act2, 0, player);
        assertSurvivesSerialisation(move);
        assertOkAndRepeatable(move);
    }

    /**
     *
     */
    public void testMove2() {
        World world = getWorld();
        Player player = getPlayer();
        Activity act = new TestActivity(50);
        world.addActiveEntity(player, act);

        Activity act2 = new TestActivity(60);
        Move move = new NextActivityMove(act2, 0, player);
        assertDoThenUndoLeavesWorldUnchanged(move);
    }

    /**
     *
     */
    public void testStackingOfActivities() {
        World world = getWorld();
        Player player = getPlayer();
        Activity act = new TestActivity(50);
        world.addActiveEntity(player, act);

        Activity act2 = new TestActivity(60);
        Move move = new NextActivityMove(act2, 0, player);
        assertDoMoveIsOk(move);

        GameTime currentTime = new GameTime(0);
        assertEquals(currentTime, world.currentTime());
        ActivityIterator activityIterator = world.getActivities(player, 0);

        assertEquals(activityIterator.getActivity(), act);
        assertEquals(activityIterator.getStartTime(), currentTime.getTicks(), 0.00001);
        assertEquals(50.0d, activityIterator.getActivity().duration(), 0.00001);
        assertEquals(50.0d, activityIterator.getStartTime() + activityIterator.getActivity().duration(), 0.00001);

        assertTrue(activityIterator.hasNext());
        activityIterator.nextActivity();
        assertEquals(activityIterator.getActivity(), act2);
        assertEquals(50, activityIterator.getStartTime(), 0.00001);
        assertEquals(60, activityIterator.getActivity().duration(), 0.0001d);
        assertEquals(110, activityIterator.getStartTime() + activityIterator.getActivity().duration(), 0.00001);
    }

}
