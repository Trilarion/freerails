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
import freerails.move.MoveTrainPreMove;
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

    private final TileTransition[] line1 = {TileTransition.EAST, TileTransition.NORTH_EAST, TileTransition.EAST, TileTransition.NORTH_EAST, TileTransition.NORTH};
    private final TileTransition[] line2 = {TileTransition.WEST, TileTransition.WEST, TileTransition.SOUTH_WEST, TileTransition.SOUTH, TileTransition.SOUTH_EAST, TileTransition.EAST};
    private final TileTransition[] line3 = {TileTransition.NORTH_WEST, TileTransition.NORTH_WEST, TileTransition.NORTH, TileTransition.NORTH, TileTransition.NORTH_EAST};
    private World world;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        world = MapFixtureFactory2.getCopy();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, 0);
        FreerailsPrincipal principal = moveExecutor.getPrincipal();
        ModelRoot modelRoot = new ModelRootImpl();
        TrackMoveProducer trackBuilder = new TrackMoveProducer(moveExecutor, world, modelRoot);
        StationBuilder stationBuilder = new StationBuilder(moveExecutor);

        // Build track.
        stationBuilder
                .setStationType(stationBuilder.getTrackTypeID("terminal"));

        Point2D stationA = new Point2D(10, 10);
        MoveStatus moveStatus = trackBuilder.buildTrack(stationA, line1);
        assertTrue(moveStatus.succeeds());
        moveStatus = trackBuilder.buildTrack(stationA, line2);
        assertTrue(moveStatus.succeeds());
        moveStatus = trackBuilder.buildTrack(stationA, line3);
        assertTrue(moveStatus.succeeds());

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
                .getTile(target1);
        assertTrue(tile.hasTrack());
        PositionOnTrack positionOnTrack = PositionOnTrack.createFacing(new Point2D(10, 10), TileTransition.EAST);
        for (int i = 0; i < expectedPath.length; i++) {
            TileTransition expected = expectedPath[i];
            TileTransition actual = MoveTrainPreMove.findNextStep(world, positionOnTrack, target1);
            assertEquals(String.valueOf(i), expected, actual);
            positionOnTrack.move(expected);
        }
    }

}
