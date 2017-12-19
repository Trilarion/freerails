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
 * Created on 13-Aug-2005
 *
 */
package freerails.controller;

import freerails.client.common.ModelRootImpl;
import freerails.move.MoveStatus;
import freerails.server.MapFixtureFactory2;
import freerails.util.ImPoint;
import freerails.world.common.PositionOnTrack;
import freerails.world.common.Step;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.FreerailsTile;
import freerails.world.top.World;
import junit.framework.TestCase;

import static freerails.world.common.Step.*;

/**
 * Unit test for MoveTrainPreMove, tests pathfinding.
 */
public class MoveTrainPreMove3rdTest extends TestCase {

    final Step[] line1 = {EAST, NORTH_EAST, EAST, NORTH_EAST, NORTH};
    final Step[] line2 = {WEST, WEST, SOUTH_WEST, SOUTH, SOUTH_EAST, EAST};
    final Step[] line3 = {NORTH_WEST, NORTH_WEST, NORTH, NORTH, NORTH_EAST};
    TrackMoveProducer trackBuilder;
    StationBuilder stationBuilder;
    FreerailsPrincipal principal;
    World world;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        world = MapFixtureFactory2.getCopy();
        MoveExecutor me = new SimpleMoveExecutor(world, 0);
        principal = me.getPrincipal();
        ModelRoot mr = new ModelRootImpl();
        trackBuilder = new TrackMoveProducer(me, world, mr);
        stationBuilder = new StationBuilder(me);

        // Build track.
        stationBuilder
                .setStationType(stationBuilder.getTrackTypeID("terminal"));

        ImPoint stationA = new ImPoint(10, 10);
        MoveStatus ms0 = trackBuilder.buildTrack(stationA, line1);
        assertTrue(ms0.ok);
        ms0 = trackBuilder.buildTrack(stationA, line2);
        assertTrue(ms0.ok);
        ms0 = trackBuilder.buildTrack(stationA, line3);
        assertTrue(ms0.ok);

    }

    /**
     *
     */
    public void testFindingPath() {
        findPath2Target(new ImPoint(14, 7), line1);
        findPath2Target(new ImPoint(9, 13), line2);
        findPath2Target(new ImPoint(9, 13), line2);
    }

    private void findPath2Target(ImPoint target1, Step[] expectedPath) {
        FreerailsTile tile = (FreerailsTile) world
                .getTile(target1.x, target1.y);
        assertTrue(tile.hasTrack());
        PositionOnTrack pot = PositionOnTrack.createFacing(10, 10, EAST);
        for (int i = 0; i < expectedPath.length; i++) {
            Step expected = expectedPath[i];
            Step actual = MoveTrainPreMove.findNextStep(world, pot, target1);
            assertEquals(String.valueOf(i), expected, actual);
            pot.move(expected);
        }
    }

}
