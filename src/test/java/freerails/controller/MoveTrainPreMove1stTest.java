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

import freerails.client.ModelRootImpl;
import freerails.move.*;
import freerails.move.premove.AddTrainPreMove;
import freerails.move.premove.MoveTrainPreMove;
import freerails.world.MapFixtureFactory2;
import freerails.util.ImmutableList;
import freerails.util.Vector2D;
import freerails.world.terrain.TileTransition;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.train.*;
import freerails.world.train.motion.Motion;
import freerails.world.train.schedule.ImmutableSchedule;
import freerails.world.train.schedule.MutableSchedule;

/**
 * Test for MoveTrainPreMove, tests moving round a loop of track.
 */
public class MoveTrainPreMove1stTest extends AbstractMoveTestCase {

    private FreerailsPrincipal principal;

    /**
     *
     */
    @Override
    protected void setupWorld() {
        world = MapFixtureFactory2.getCopy();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, 0);
        principal = moveExecutor.getPrincipal();
        ModelRoot modelRoot = new ModelRootImpl();
        TrackMoveProducer trackBuilder = new TrackMoveProducer(moveExecutor, world, modelRoot);
        StationBuilder stationBuilder = new StationBuilder(moveExecutor);

        // Build track.
        stationBuilder.setStationType(stationBuilder.getTrackTypeID("terminal"));
        TileTransition[] track = {TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        Vector2D stationA = new Vector2D(10, 10);
        MoveStatus ms0 = trackBuilder.buildTrack(stationA, track);
        assertTrue(ms0.succeeds());

        // Build 2 stations.
        MoveStatus ms1 = stationBuilder.buildStation(stationA);
        assertTrue(ms1.succeeds());
        Vector2D stationB = new Vector2D(19, 10);
        MoveStatus ms2 = stationBuilder.buildStation(stationB);
        assertTrue(ms2.succeeds());

        TrainOrders order0 = new TrainOrders(1, null, false, false);
        TrainOrders order1 = new TrainOrders(0, null, false, false);
        MutableSchedule s = new MutableSchedule();
        s.addOrder(order0);
        s.addOrder(order1);
        ImmutableSchedule defaultSchedule = s.toImmutableSchedule();

        Vector2D start = new Vector2D(10, 10);
        AddTrainPreMove preMove = new AddTrainPreMove(0, new ImmutableList<>(0, 0), start, principal, defaultSchedule);
        Move move = preMove.generateMove(world);
        MoveStatus moveStatus = move.doMove(world, principal);
        assertTrue(moveStatus.succeeds());
    }

    /**
     *
     */
    public void testNextVector() {
        MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal, new OccupiedTracks(principal, world));
        TileTransition actual = preMove.nextStep(world);
        assertNotNull(actual);
        // The train is at station A, so should head east to station B.
        assertEquals(TileTransition.EAST, actual);
    }

    /**
     *
     */
    public void testNextSpeeds() {
        MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal, new OccupiedTracks(principal, world));
        Motion speeds = preMove.nextSpeeds(world, TileTransition.EAST);
        assertNotNull(speeds);
        assertEquals(speeds.calculateSpeedAtTime(0), 0.0d);
        assertTrue(speeds.getTotalDistance() >= TileTransition.EAST.getLength());
        double t = speeds.getTotalTime();
        assertTrue(t > 0);
        assertTrue(speeds.calculateSpeedAtTime(t) > 0);
    }

    /**
     *
     */
    public void testMove() {
        MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal, new OccupiedTracks(principal, world));
        Move move = preMove.generateMove(world);
        assertNotNull(move);
        assertSurvivesSerialisation(move);
    }

    /**
     *
     */
    public void testMove2() {
        MoveStatus moveStatus;
        Move move;
        setupLoopOfTrack();

        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        TrainMotion trainMotion = ta.findCurrentMotion(3);

        assertEquals(0.0d, trainMotion.duration());

        PathOnTiles expected = new PathOnTiles(new Vector2D(5, 5), TileTransition.SOUTH_WEST);
        assertEquals(expected, trainMotion.getPath());
        PositionOnTrack positionOnTrack = trainMotion.getFinalPosition();
        int x = positionOnTrack.getLocation().x;
        assertEquals(4, x);
        int y = positionOnTrack.getLocation().y;
        assertEquals(6, y);
        assertEquals(TileTransition.SOUTH_WEST, positionOnTrack.facing());

        MoveTrainPreMove moveTrain = new MoveTrainPreMove(0, principal,
                new OccupiedTracks(principal, world));

        assertEquals(TileTransition.NORTH_EAST, moveTrain.nextStep(world));

        move = moveTrain.generateMove(world);
        moveStatus = move.doMove(world, principal);
        assertTrue(moveStatus.succeeds());

        TrainMotion tm2 = ta.findCurrentMotion(3);
        assertFalse(trainMotion.equals(tm2));

        expected = new PathOnTiles(new Vector2D(5, 5), TileTransition.SOUTH_WEST, TileTransition.NORTH_EAST);
        assertEquals(expected, tm2.getPath());

        assertTrue(tm2.duration() > 3.0d);
        // The expected value is 3.481641930846211, found from
        // stepping thu code in debugger.
        assertTrackHere(tm2.getTiles(tm2.duration()));

        positionOnTrack = tm2.getFinalPosition();
        assertEquals(4, x);
        assertEquals(6, y);
        // assertEquals(SOUTH, pot.facing());

        assertTrackHere(x, y);

        assertEquals(TileTransition.EAST, moveTrain.nextStep(world));

        MoveTrainPreMove2ndTest.incrTime(world, principal);
        move = moveTrain.generateMove(world);
        moveStatus = move.doMove(world, principal);
        assertTrue(moveStatus.succeeds());

        TrainMotion tm3 = ta.findCurrentMotion(100);
        assertFalse(tm3.equals(tm2));
        expected = new PathOnTiles(new Vector2D(4, 6), TileTransition.NORTH_EAST, TileTransition.EAST);
        assertEquals(expected, tm3.getPath());

        assertTrackHere(tm3.getTiles(tm3.duration()));
        assertTrackHere(tm3.getTiles(tm3.duration() / 2));
        assertTrackHere(tm3.getTiles(0));
        assertTrackHere(tm3.getPath());

        assertEquals(TileTransition.SOUTH_EAST, moveTrain.nextStep(world));

        MoveTrainPreMove2ndTest.incrTime(world, principal);
        move = moveTrain.generateMove(world);

        moveStatus = move.doMove(world, principal);
        assertTrue(moveStatus.succeeds());
    }

    private void setupLoopOfTrack() {
        world = MapFixtureFactory2.getCopy();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, 0);
        principal = moveExecutor.getPrincipal();
        ModelRoot modelRoot = new ModelRootImpl();
        TrackMoveProducer producer = new TrackMoveProducer(moveExecutor, world, modelRoot);
        TileTransition[] trackPath = {TileTransition.EAST, TileTransition.SOUTH_EAST, TileTransition.SOUTH, TileTransition.SOUTH_WEST, TileTransition.WEST,
                TileTransition.NORTH_WEST, TileTransition.NORTH, TileTransition.NORTH_EAST};
        Vector2D from = new Vector2D(5, 5);
        MoveStatus moveStatus = producer.buildTrack(from, trackPath);
        assertTrue(moveStatus.succeeds());

        TrainOrders[] orders = {};
        ImmutableSchedule is = new ImmutableSchedule(orders, -1, false);
        AddTrainPreMove addTrain = new AddTrainPreMove(0, new ImmutableList<>(), from,
                principal, is);

        Move move = addTrain.generateMove(world);
        moveStatus = move.doMove(world, principal);
        assertTrue(moveStatus.succeeds());
        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        TrainMotion motion = ta.findCurrentMotion(0);
        assertNotNull(motion);

        PathOnTiles expected = new PathOnTiles(from, TileTransition.SOUTH_WEST);
        PathOnTiles actual = motion.getTiles(motion.duration());
        assertEquals(expected, actual);
    }

    /**
     *
     */
    public void testMovingRoundLoop() {
        setupLoopOfTrack();

        MoveTrainPreMove moveTrain = new MoveTrainPreMove(0, principal, new OccupiedTracks(principal, world));
        Move move = moveTrain.generateMove(world);
        assertTrue(move.doMove(world, principal).succeeds());
    }

    /**
     *
     */
    public void testGetTiles() {
        setupLoopOfTrack();

        MoveTrainPreMove moveTrain = new MoveTrainPreMove(0, principal, new OccupiedTracks(principal, world));
        Move move = moveTrain.generateMove(world);
        assertTrue(move.doMove(world, principal).succeeds());

        TrainAccessor trainAccessor = new TrainAccessor(world, principal, 0);
        TrainMotion motion = trainAccessor.findCurrentMotion(1);
        double duration = motion.duration();
        assertTrue(duration > 1);
        int trainLength = motion.getTrainLength();
        for (int i = 0; i < 10; i++) {
            double t = i == 0 ? 0 : duration * i / 10;
            PathOnTiles tiles = motion.getTiles(t);
            assertTrue("t=" + t, tiles.steps() > 0);

            assertTrue("t=" + t, tiles.getTotalDistance() >= trainLength);
        }
    }

    /**
     *
     */
    public void testFindNextVector() {
        setupLoopOfTrack();
        PositionOnTrack positionOnTrack = PositionOnTrack.createFacing(new Vector2D(4, 6), TileTransition.SOUTH_WEST);

        Vector2D target = new Vector2D();
        TileTransition expected = TileTransition.NORTH_EAST;
        assertEquals(expected, MoveTrainPreMove.findNextStep(world, positionOnTrack, target));
        positionOnTrack.move(expected);
        expected = TileTransition.EAST;
        assertEquals(expected, MoveTrainPreMove.findNextStep(world, positionOnTrack, target));
        positionOnTrack.move(expected);

        expected = TileTransition.SOUTH_EAST;
        assertEquals(expected, MoveTrainPreMove.findNextStep(world, positionOnTrack, target));
        positionOnTrack.move(expected);

        expected = TileTransition.SOUTH;
        assertEquals(expected, MoveTrainPreMove.findNextStep(world, positionOnTrack, target));
        positionOnTrack.move(expected);
    }

}
