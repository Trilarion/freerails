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

import freerails.client.common.ModelRootImpl;
import freerails.move.MoveStatus;
import freerails.server.MapFixtureFactory2;
import freerails.util.Point2D;
import freerails.world.train.PositionOnTrack;
import freerails.world.terrain.TileTransition;
import freerails.world.World;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.FullTerrainTile;
import junit.framework.TestCase;

/**
 * Unit test for MoveTrainPreMove, tests path finding.
 */
public class MoveTrainPreMove3rdTest extends TestCase {

    final TileTransition[] line1 = {TileTransition.EAST, TileTransition.NORTH_EAST, TileTransition.EAST, TileTransition.NORTH_EAST, TileTransition.NORTH};
    final TileTransition[] line2 = {TileTransition.WEST, TileTransition.WEST, TileTransition.SOUTH_WEST, TileTransition.SOUTH, TileTransition.SOUTH_EAST, TileTransition.EAST};
    final TileTransition[] line3 = {TileTransition.NORTH_WEST, TileTransition.NORTH_WEST, TileTransition.NORTH, TileTransition.NORTH, TileTransition.NORTH_EAST};
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

        Point2D stationA = new Point2D(10, 10);
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
        findPath2Target(new Point2D(14, 7), line1);
        findPath2Target(new Point2D(9, 13), line2);
        findPath2Target(new Point2D(9, 13), line2);
    }

    private void findPath2Target(Point2D target1, TileTransition[] expectedPath) {
        FullTerrainTile tile = (FullTerrainTile) world
                .getTile(target1.x, target1.y);
        assertTrue(tile.hasTrack());
        PositionOnTrack pot = PositionOnTrack.createFacing(10, 10, TileTransition.EAST);
        for (int i = 0; i < expectedPath.length; i++) {
            TileTransition expected = expectedPath[i];
            TileTransition actual = MoveTrainPreMove.findNextStep(world, pot, target1);
            assertEquals(String.valueOf(i), expected, actual);
            pot.move(expected);
        }
    }

}
