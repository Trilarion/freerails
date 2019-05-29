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

import freerails.client.ModelRoot;
import freerails.client.ModelRootImpl;
import freerails.model.track.TrackUtils;
import freerails.move.MoveExecutor;
import freerails.move.Status;
import freerails.move.SimpleMoveExecutor;
import freerails.model.MapFixtureFactory2;
import freerails.util.Vec2D;
import freerails.model.train.PositionOnTrack;
import freerails.model.terrain.TileTransition;
import freerails.model.world.World;
import freerails.model.terrain.TerrainTile;
import junit.framework.TestCase;

/**
 * Unit test for MoveTrainPreMove, tests path finding.
 */
public class MoveTrainMoveGenerator3RdTest extends TestCase {

    private final TileTransition[] line1 = {TileTransition.EAST, TileTransition.NORTH_EAST, TileTransition.EAST, TileTransition.NORTH_EAST, TileTransition.NORTH};
    private final TileTransition[] line2 = {TileTransition.WEST, TileTransition.WEST, TileTransition.SOUTH_WEST, TileTransition.SOUTH, TileTransition.SOUTH_EAST, TileTransition.EAST};
    private final TileTransition[] line3 = {TileTransition.NORTH_WEST, TileTransition.NORTH_WEST, TileTransition.NORTH, TileTransition.NORTH, TileTransition.NORTH_EAST};
    private World world;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, world.getPlayer(0));
        ModelRoot modelRoot = new ModelRootImpl();
        TrackMoveProducer trackBuilder = new TrackMoveProducer(moveExecutor, world, modelRoot);
        StationBuilder stationBuilder = new StationBuilder(moveExecutor);

        // Build track.
        stationBuilder.setStationType(stationBuilder.getTrackTypeID("terminal"));

        Vec2D stationA = new Vec2D(10, 10);
        Status status = trackBuilder.buildTrack(stationA, line1);
        assertTrue(status.isSuccess());
        status = trackBuilder.buildTrack(stationA, line2);
        assertTrue(status.isSuccess());
        status = trackBuilder.buildTrack(stationA, line3);
        assertTrue(status.isSuccess());
    }

    /**
     *
     */
    public void testFindingPath() {
        findPath2Target(new Vec2D(14, 7), line1);
        findPath2Target(new Vec2D(9, 13), line2);
        findPath2Target(new Vec2D(9, 13), line2);
    }

    private void findPath2Target(Vec2D target1, TileTransition[] expectedPath) {
        TerrainTile tile = world.getTile(target1);
        assertTrue(tile.hasTrack());
        PositionOnTrack positionOnTrack = new PositionOnTrack(new Vec2D(10, 10), TileTransition.EAST.getOpposite());
        for (int i = 0; i < expectedPath.length; i++) {
            TileTransition expected = expectedPath[i];
            TileTransition actual = TrackUtils.findNextStep(world, positionOnTrack, target1);
            assertEquals(String.valueOf(i), expected, actual);
            positionOnTrack.move(expected);
        }
    }

}
