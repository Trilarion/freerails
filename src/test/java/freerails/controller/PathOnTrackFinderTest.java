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
import freerails.server.MapFixtureFactory2;
import freerails.util.ImPoint;
import freerails.world.terrain.TileTransition;
import freerails.world.World;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 */
public class PathOnTrackFinderTest extends TestCase {

    World w;

    TrackMoveProducer producer;

    PathOnTrackFinder pathFinder;

    StationBuilder stationBuilder;

    BuildTrackStrategy bts;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        w = MapFixtureFactory2.getCopy();
        MoveExecutor me = new SimpleMoveExecutor(w, 0);
        ModelRoot mr = new ModelRootImpl();
        producer = new TrackMoveProducer(me, w, mr);
        pathFinder = new PathOnTrackFinder(w);
        stationBuilder = new StationBuilder(me);
        bts = BuildTrackStrategy.getDefault(w);
    }

    /**
     *
     */
    public void testPathAsVectors1() {
        TileTransition[] path = {TileTransition.EAST, TileTransition.EAST, TileTransition.SOUTH_EAST};
        ImPoint start = new ImPoint(5, 5);
        ImPoint end = TileTransition.move(start, path);
        producer.buildTrack(start, path);
        try {
            pathFinder.setupSearch(start, end);
            pathFinder.search(-1);
            assertEquals(IncrementalPathFinder.PATH_FOUND, pathFinder
                    .getStatus());
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
        ImPoint start = new ImPoint(5, 5);
        ImPoint end = TileTransition.move(start, path);
        producer.buildTrack(start, path);
        try {
            pathFinder.setupSearch(start, end);
            pathFinder.search(-1);
            assertEquals(IncrementalPathFinder.PATH_FOUND, pathFinder
                    .getStatus());
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
        ImPoint start = new ImPoint(5, 5);
        ImPoint end = TileTransition.move(start, path);
        producer.buildTrack(start, path);
        try {
            pathFinder.setupSearch(start, end);
        } catch (PathNotFoundException e) {
            fail("Track at both of the points so no exception should be thrown");
        }
        try {
            pathFinder.setupSearch(start, new ImPoint(10, 10));
            fail("No track at one of the points so an exception should be thrown");
        } catch (PathNotFoundException e) {

        }
        try {
            pathFinder.setupSearch(new ImPoint(10, 10), end);
            fail("No track at one of the points so an exception should be thrown");
        } catch (PathNotFoundException e) {

        }
    }

}
