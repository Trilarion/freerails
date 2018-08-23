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

package freerails.controller;

import freerails.client.ModelRoot;
import freerails.client.ModelRootImpl;
import freerails.model.track.pathfinding.PathFinderStatus;
import freerails.model.track.pathfinding.PathNotFoundException;
import freerails.model.track.pathfinding.TrackPathFinder;
import freerails.model.track.BuildTrackStrategy;
import freerails.move.MoveExecutor;
import freerails.move.Status;
import freerails.model.MapFixtureFactory2;
import freerails.move.SimpleMoveExecutor;
import freerails.util.Vec2D;
import freerails.model.terrain.TileTransition;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.model.track.TrackPiece;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 *
 */
public class TrackBuildingTest extends TestCase {

    private World world;
    private TrackMoveProducer producer;
    private TrackPathFinder pathFinder;
    private StationBuilder stationBuilder;
    private BuildTrackStrategy bts;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, world.getPlayer(0));
        ModelRoot modelRoot = new ModelRootImpl();
        producer = new TrackMoveProducer(moveExecutor, world, modelRoot);
        Player player = world.getPlayer(0);
        pathFinder = new TrackPathFinder(world, player);
        stationBuilder = new StationBuilder(moveExecutor);
        bts = BuildTrackStrategy.getDefault(world);
    }

    /**
     * Tests building track from 5,5 to 10,5
     */
    public void testBuildingStraight() {

        Vec2D from = new Vec2D(5, 5);
        Vec2D to = new Vec2D(10, 5);
        try {
            // Check there is no track before we build it.
            for (int x = 5; x <= 10; x++) {
                TrackPiece trackPiece = world.getTile(new Vec2D(x, 5)).getTrackPiece();
                assertEquals(null, trackPiece);
            }
            pathFinder.setupSearch(from, to, bts);
            pathFinder.search(-1);
            assertEquals(pathFinder.getStatus(), PathFinderStatus.PATH_FOUND);
            TileTransition[] path = pathFinder.pathAsVectors();
            assertEquals(path.length, 5);
            for (int i = 0; i < 5; i++) {
                assertEquals(TileTransition.EAST, path[i]);
            }
            Status status = producer.buildTrack(from, path);
            assertTrue(status.getMessage(), status.isSuccess());
            // Check track has been built.
            for (int x = 5; x <= 10; x++) {
                TrackPiece tp = world.getTile(new Vec2D(x, 5)).getTrackPiece();
                assertEquals(0, tp.getTrackType().getId());
            }
        } catch (PathNotFoundException e) {
            fail();
        }
    }

    /**
     * Tests building track from 5,5 to 6,5
     */
    public void testBuildingOneTrackPiece() {

        Vec2D from = new Vec2D(5, 5);
        Vec2D to = new Vec2D(6, 5);
        try {
            // Check there is no track before we build it.

            TrackPiece tp1 = world.getTile(new Vec2D(5, 5)).getTrackPiece();
            assertEquals(null, tp1);

            TrackPiece tp2 = world.getTile(new Vec2D(6, 5)).getTrackPiece();
            assertEquals(null, tp2);

            pathFinder.setupSearch(from, to, bts);
            pathFinder.search(-1);
            assertEquals(pathFinder.getStatus(), PathFinderStatus.PATH_FOUND);
            TileTransition[] path = pathFinder.pathAsVectors();
            assertEquals(path.length, 1);

            assertEquals(TileTransition.EAST, path[0]);

            Status status = producer.buildTrack(from, path);
            assertTrue(status.getMessage(), status.isSuccess());
            // Check track has been built.
            tp1 = world.getTile(new Vec2D(5, 5)).getTrackPiece();
            assertEquals(0, tp1.getTrackType().getId());

            tp2 = world.getTile(new Vec2D(6, 5)).getTrackPiece();
            assertEquals(0, tp2.getTrackType().getId());
        } catch (PathNotFoundException e) {
            fail();
        }
    }

    /**
     * There is a bug where if a section of track has a terminal on the end, you
     * cannot extend the track through the terminal. Instead, the track path
     * finder finds a route that misses out the terminal.
     */
    public void testTerminalProblem() {
        try {
            Vec2D from = new Vec2D(5, 5);
            TileTransition[] path = {TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
            Status status = producer.buildTrack(from, path);
            assertTrue(status.isSuccess());
            int terminalStationType = stationBuilder.getTrackTypeID("terminal");
            stationBuilder.setStationType(terminalStationType);
            status = stationBuilder.buildStation(new Vec2D(8, 5));
            assertTrue(status.isSuccess());
            pathFinder.setupSearch(new Vec2D(7, 5), new Vec2D(9, 5), bts);
            pathFinder.search(-1);
            path = pathFinder.pathAsVectors();
            assertEquals(2, path.length);
            TileTransition[] expectedPath = {TileTransition.EAST, TileTransition.EAST};
            assertTrue(Arrays.equals(expectedPath, path));
        } catch (PathNotFoundException e) {
            fail();
        }
    }

    /**
     * There is a bug where if you build a straight section of double track
     * going E, then move the cursor to the end and attempt to build more double
     * track going SE, the track path finder builds a loop rather than just
     * building track going SE
     */
    public void testDoubleTrackProblem() {
        try {

            int trackTypeID = stationBuilder.getTrackTypeID("double track");
            bts = BuildTrackStrategy.getSingleRuleInstance(trackTypeID, world);
            producer.setBuildTrackStrategy(bts);
            Vec2D a = new Vec2D(5, 5);
            Vec2D b = new Vec2D(6, 5);
            Vec2D c = new Vec2D(7, 6);

            pathFinder.setupSearch(a, b, bts);
            pathFinder.search(-1);
            TileTransition[] path = pathFinder.pathAsVectors();
            TileTransition[] expectedPath = {TileTransition.EAST};
            assertTrue(Arrays.equals(expectedPath, path));
            Status status = producer.buildTrack(a, path);
            assertTrue(status.isSuccess());

            TrackPiece tp = world.getTile(b).getTrackPiece();
            assertEquals("We just build double track here.", trackTypeID, tp.getTrackType().getId());

            pathFinder.setupSearch(b, c, bts);
            pathFinder.search(-1);
            path = pathFinder.pathAsVectors();
            assertEquals(1, path.length);

            expectedPath = new TileTransition[]{TileTransition.SOUTH_EAST};
            assertTrue(Arrays.equals(expectedPath, path));
        } catch (PathNotFoundException e) {
            fail();
        }
    }

    /**
     * There is a bug where if you try to start building track on a 90 degree
     * bend, no track path is found even when one should exist.
     */
    public void testStartSearchOnSharpCurve() {
        try {
            Vec2D from = new Vec2D(5, 5);
            TileTransition[] path = {TileTransition.EAST, TileTransition.SOUTH};
            Status status = producer.buildTrack(from, path);
            assertTrue(status.isSuccess());
            pathFinder.setupSearch(new Vec2D(6, 5), new Vec2D(6, 7), bts);
            pathFinder.search(-1);
            path = pathFinder.pathAsVectors();
            assertEquals(2, path.length);
            assertEquals(TileTransition.SOUTH, path[0]);
            assertEquals(TileTransition.SOUTH, path[1]);
        } catch (PathNotFoundException e) {
            fail();
        }
    }

}
