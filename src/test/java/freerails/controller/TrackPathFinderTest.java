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
package freerails.controller;

import freerails.util.Point2D;
import freerails.world.game.GameRules;
import freerails.world.ITEM;
import freerails.world.WorldImpl;
import freerails.world.player.Player;
import freerails.world.top.MapFixtureFactory;
import junit.framework.TestCase;

import java.util.List;

/**
 * Test for TrackPathFinder.
 */
public class TrackPathFinderTest extends TestCase {
    private final Player testPlayer = new Player("test", 0);
    private WorldImpl world;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        world = new WorldImpl(20, 20);
        world.addPlayer(testPlayer);
        world.set(ITEM.GAME_RULES, GameRules.NO_RESTRICTIONS);
        MapFixtureFactory.generateTrackRuleList(world);
    }

    /**
     *
     */
    public void testGeneratePath() {
        try {
            BuildTrackStrategy bts = BuildTrackStrategy.getSingleRuleInstance(
                    0, world);

            TrackPathFinder pathFinder = new TrackPathFinder(world, testPlayer
                    .getPrincipal());
            List l = pathFinder.generatePath(new Point2D(0, 0), new Point2D(0,
                    5), bts);
            assertEquals(5, l.size());

            List list2 = pathFinder.generatePath(new Point2D(5, 5),
                    new Point2D(5, 10), bts);
            assertEquals(5, list2.size());

            list2 = pathFinder.generatePath(new Point2D(5, 10), new Point2D(5,
                    5), bts);
            assertEquals(5, list2.size());
        } catch (PathNotFoundException e) {
            fail();
        }
    }
}