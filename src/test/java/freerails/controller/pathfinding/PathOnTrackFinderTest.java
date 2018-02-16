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
package freerails.controller.pathfinding;

import freerails.client.ModelRoot;
import freerails.client.ModelRootImpl;
import freerails.controller.*;
import freerails.model.MapFixtureFactory2;
import freerails.util.Vector2D;
import freerails.model.terrain.TileTransition;
import freerails.model.world.World;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 */
public class PathOnTrackFinderTest extends TestCase {

    private TrackMoveProducer producer;
    private PathOnTrackFinder pathFinder;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        World world = MapFixtureFactory2.getCopy();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, 0);
        ModelRoot modelRoot = new ModelRootImpl();
        producer = new TrackMoveProducer(moveExecutor, world, modelRoot);
        pathFinder = new PathOnTrackFinder(world);
        StationBuilder stationBuilder = new StationBuilder(moveExecutor);
        BuildTrackStrategy bts = BuildTrackStrategy.getDefault(world);
    }

    /**
     *
     */
    public void testPathAsVectors1() {
        TileTransition[] path = {TileTransition.EAST, TileTransition.EAST, TileTransition.SOUTH_EAST};
        Vector2D start = new Vector2D(5, 5);
        Vector2D end = TileTransition.move(start, path);
        producer.buildTrack(start, path);
        try {
            pathFinder.setupSearch(start, end);
            pathFinder.search(-1);
            assertEquals(PathFinderStatus.PATH_FOUND, pathFinder.getStatus());
            TileTransition[] pathFound = pathFinder.pathAsVectors();
            assertTrue(Arrays.equals(path, pathFound));
        } catch (PathNotFoundException e) {
            fail();
        }
    }

    /**
     *
     */
    public void testPathAsVectors2() {
        TileTransition[] path = {TileTransition.EAST, TileTransition.EAST, TileTransition.SOUTH_EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.NORTH_EAST};
        Vector2D start = new Vector2D(5, 5);
        Vector2D end = TileTransition.move(start, path);
        producer.buildTrack(start, path);
        try {
            pathFinder.setupSearch(start, end);
            pathFinder.search(-1);
            assertEquals(PathFinderStatus.PATH_FOUND, pathFinder.getStatus());
            TileTransition[] pathFound = pathFinder.pathAsVectors();
            assertTrue(Arrays.equals(path, pathFound));
        } catch (PathNotFoundException e) {
            fail();
        }
    }

    /**
     *
     */
    public void testSetupSearch() {
        TileTransition[] path = {TileTransition.EAST, TileTransition.EAST, TileTransition.SOUTH_EAST};
        Vector2D start = new Vector2D(5, 5);
        Vector2D end = TileTransition.move(start, path);
        producer.buildTrack(start, path);
        try {
            pathFinder.setupSearch(start, end);
        } catch (PathNotFoundException e) {
            fail("Track at both of the points so no exception should be thrown");
        }
        try {
            pathFinder.setupSearch(start, new Vector2D(10, 10));
            fail("No track at one of the points so an exception should be thrown");
        } catch (PathNotFoundException e) {
        }
        try {
            pathFinder.setupSearch(new Vector2D(10, 10), end);
            fail("No track at one of the points so an exception should be thrown");
        } catch (PathNotFoundException e) {
        }
    }
}
