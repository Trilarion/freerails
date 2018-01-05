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
import freerails.move.AbstractMoveTestCase;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.server.MapFixtureFactory2;
import freerails.util.ImInts;
import freerails.util.ImPoint;
import freerails.world.PositionOnTrack;
import freerails.world.TileTransition;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.train.*;

/**
 * JUnit test for MoveTrainPreMove, tests moving round a loop of track.
 */
public class MoveTrainPreMove1stTest extends AbstractMoveTestCase {

    TrackMoveProducer trackBuilder;

    StationBuilder stationBuilder;

    FreerailsPrincipal principal;

    ImmutableSchedule defaultSchedule;

    /**
     *
     */
    @Override
    protected void setupWorld() {
        world = MapFixtureFactory2.getCopy();
        MoveExecutor me = new SimpleMoveExecutor(world, 0);
        principal = me.getPrincipal();
        ModelRoot mr = new ModelRootImpl();
        trackBuilder = new TrackMoveProducer(me, world, mr);
        stationBuilder = new StationBuilder(me);

        // Build track.
        stationBuilder
                .setStationType(stationBuilder.getTrackTypeID("terminal"));
        TileTransition[] track = {TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        ImPoint stationA = new ImPoint(10, 10);
        MoveStatus ms0 = trackBuilder.buildTrack(stationA, track);
        assertTrue(ms0.ok);

        // Build 2 stations.
        MoveStatus ms1 = stationBuilder.buildStation(stationA);
        assertTrue(ms1.ok);
        ImPoint stationB = new ImPoint(19, 10);
        MoveStatus ms2 = stationBuilder.buildStation(stationB);
        assertTrue(ms2.ok);

        TrainOrdersModel order0 = new TrainOrdersModel(1, null, false, false);
        TrainOrdersModel order1 = new TrainOrdersModel(0, null, false, false);
        MutableSchedule s = new MutableSchedule();
        s.addOrder(order0);
        s.addOrder(order1);
        defaultSchedule = s.toImmutableSchedule();

        ImPoint start = new ImPoint(10, 10);
        AddTrainPreMove preMove = new AddTrainPreMove(0, new ImInts(0, 0),
                start, principal, defaultSchedule);
        Move m = preMove.generateMove(world);
        MoveStatus ms = m.doMove(world, principal);
        assertTrue(ms.ok);
    }

    /**
     *
     */
    public void testNextVector() {

        MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal,
                new OccupiedTracks(principal, world));
        TileTransition actual = preMove.nextStep(world);
        assertNotNull(actual);
        // The train is at station A, so should head east to station B.
        assertEquals(TileTransition.EAST, actual);
    }

    /**
     *
     */
    public void testNextSpeeds() {

        MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal,
                new OccupiedTracks(principal, world));
        SpeedAgainstTime speeds = preMove.nextSpeeds(world, TileTransition.EAST);
        assertNotNull(speeds);
        assertEquals(speeds.calcV(0), 0d);
        assertTrue(speeds.getDistance() >= TileTransition.EAST.getLength());
        double t = speeds.getTime();
        assertTrue(t > 0);
        assertTrue(speeds.calcV(t) > 0);
    }

    /**
     *
     */
    @Override
    public void testMove() {
        MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal,
                new OccupiedTracks(principal, world));
        Move m = preMove.generateMove(world);
        assertNotNull(m);
        assertSurvivesSerialisation(m);

    }

    /**
     *
     */
    public void testMove2() {
        MoveStatus ms;
        Move m;
        setupLoopOfTrack();

        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        TrainMotion tm = ta.findCurrentMotion(3);

        assertEquals(0d, tm.duration());

        PathOnTiles expected = new PathOnTiles(new ImPoint(5, 5), TileTransition.SOUTH_WEST);
        assertEquals(expected, tm.getPath());
        PositionOnTrack pot = tm.getFinalPosition();
        int x = pot.getX();
        assertEquals(4, x);
        int y = pot.getY();
        assertEquals(6, y);
        assertEquals(TileTransition.SOUTH_WEST, pot.facing());

        MoveTrainPreMove moveTrain = new MoveTrainPreMove(0, principal,
                new OccupiedTracks(principal, world));

        assertEquals(TileTransition.NORTH_EAST, moveTrain.nextStep(world));

        m = moveTrain.generateMove(world);
        ms = m.doMove(world, principal);
        assertTrue(ms.ok);

        TrainMotion tm2 = ta.findCurrentMotion(3);
        assertFalse(tm.equals(tm2));

        expected = new PathOnTiles(new ImPoint(5, 5), TileTransition.SOUTH_WEST, TileTransition.NORTH_EAST);
        assertEquals(expected, tm2.getPath());

        assertTrue(tm2.duration() > 3d);
        // The expected value is 3.481641930846211, found from
        // stepping thu code in debugger.
        assertTrackHere(tm2.getTiles(tm2.duration()));

        pot = tm2.getFinalPosition();
        assertEquals(4, x);
        assertEquals(6, y);
        // assertEquals(SOUTH, pot.facing());

        assertTrackHere(x, y);

        assertEquals(TileTransition.EAST, moveTrain.nextStep(world));

        MoveTrainPreMove2ndTest.incrTime(world, principal);
        m = moveTrain.generateMove(world);
        ms = m.doMove(world, principal);
        assertTrue(ms.ok);

        TrainMotion tm3 = ta.findCurrentMotion(100);
        assertFalse(tm3.equals(tm2));
        expected = new PathOnTiles(new ImPoint(4, 6), TileTransition.NORTH_EAST, TileTransition.EAST);
        assertEquals(expected, tm3.getPath());

        assertTrackHere(tm3.getTiles(tm3.duration()));
        assertTrackHere(tm3.getTiles(tm3.duration() / 2));
        assertTrackHere(tm3.getTiles(0));
        assertTrackHere(tm3.getPath());

        assertEquals(TileTransition.SOUTH_EAST, moveTrain.nextStep(world));

        MoveTrainPreMove2ndTest.incrTime(world, principal);
        m = moveTrain.generateMove(world);

        ms = m.doMove(world, principal);
        assertTrue(ms.ok);

    }

    private void setupLoopOfTrack() {
        world = MapFixtureFactory2.getCopy();
        MoveExecutor me = new SimpleMoveExecutor(world, 0);
        principal = me.getPrincipal();
        ModelRoot mr = new ModelRootImpl();
        TrackMoveProducer producer = new TrackMoveProducer(me, world, mr);
        TileTransition[] trackPath = {TileTransition.EAST, TileTransition.SOUTH_EAST, TileTransition.SOUTH, TileTransition.SOUTH_WEST, TileTransition.WEST,
                TileTransition.NORTH_WEST, TileTransition.NORTH, TileTransition.NORTH_EAST};
        ImPoint from = new ImPoint(5, 5);
        MoveStatus ms = producer.buildTrack(from, trackPath);
        assertTrue(ms.ok);

        TrainOrdersModel[] orders = {};
        ImmutableSchedule is = new ImmutableSchedule(orders, -1, false);
        AddTrainPreMove addTrain = new AddTrainPreMove(0, new ImInts(), from,
                principal, is);

        Move m = addTrain.generateMove(world);
        ms = m.doMove(world, principal);
        assertTrue(ms.ok);
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

        MoveTrainPreMove moveTrain = new MoveTrainPreMove(0, principal,
                new OccupiedTracks(principal, world));
        Move m = moveTrain.generateMove(world);
        assertTrue(m.doMove(world, principal).ok);

    }

    /**
     *
     */
    public void testGetTiles() {
        setupLoopOfTrack();

        MoveTrainPreMove moveTrain = new MoveTrainPreMove(0, principal,
                new OccupiedTracks(principal, world));
        Move m = moveTrain.generateMove(world);
        assertTrue(m.doMove(world, principal).ok);

        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        TrainMotion motion = ta.findCurrentMotion(1);
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
        PositionOnTrack pot = PositionOnTrack.createFacing(4, 6, TileTransition.SOUTH_WEST);

        ImPoint target = new ImPoint();
        TileTransition expected = TileTransition.NORTH_EAST;
        assertEquals(expected, MoveTrainPreMove
                .findNextStep(world, pot, target));
        pot.move(expected);
        expected = TileTransition.EAST;
        assertEquals(expected, MoveTrainPreMove
                .findNextStep(world, pot, target));
        pot.move(expected);

        expected = TileTransition.SOUTH_EAST;
        assertEquals(expected, MoveTrainPreMove
                .findNextStep(world, pot, target));
        pot.move(expected);

        expected = TileTransition.SOUTH;
        assertEquals(expected, MoveTrainPreMove
                .findNextStep(world, pot, target));
        pot.move(expected);

    }

}
