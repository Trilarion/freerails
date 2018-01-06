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
import freerails.world.ActivityIterator;
import freerails.world.PositionOnTrack;
import freerails.world.terrain.TileTransition;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.train.*;

/**
 * Junit test for AddTrainPreMove.
 */
public class AddTrainPreMoveTest extends AbstractMoveTestCase {

    TrackMoveProducer trackBuilder;

    StationBuilder stationBuilder;

    FreerailsPrincipal principal;
    ImmutableSchedule defaultSchedule;
    private ImPoint stationA;

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
        stationA = new ImPoint(10, 10);
        MoveStatus ms0 = trackBuilder.buildTrack(stationA, track);
        assertTrue(ms0.ok);

        // Build 2 stations.
        MoveStatus ms1 = stationBuilder.buildStation(stationA);
        assertTrue(ms1.ok);
        ImPoint stationB = new ImPoint(19, 10);
        MoveStatus ms2 = stationBuilder.buildStation(stationB);
        assertTrue(ms2.ok);

        TrainOrdersModel order0 = new TrainOrdersModel(0, null, false, false);
        TrainOrdersModel order1 = new TrainOrdersModel(1, null, false, false);
        MutableSchedule s = new MutableSchedule();
        s.addOrder(order0);
        s.addOrder(order1);
        defaultSchedule = s.toImmutableSchedule();

    }

    /**
     *
     */
    @Override
    public void testMove() {
        AddTrainPreMove preMove = new AddTrainPreMove(0, new ImInts(0, 0),
                stationA, principal, defaultSchedule);
        Move m = preMove.generateMove(world);
        assertDoMoveIsOk(m);

        assertUndoMoveIsOk(m);

        assertSurvivesSerialisation(m);
    }

    /**
     * Check that the path on tiles created for the new train is actually on the
     * track.
     */
    public void testPathOnTiles() {
        AddTrainPreMove preMove = new AddTrainPreMove(0, new ImInts(0, 0),
                stationA, principal, defaultSchedule);
        Move m = preMove.generateMove(world);
        MoveStatus ms = m.doMove(world, Player.AUTHORITATIVE);
        assertTrue(ms.ok);

        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        TrainMotion motion = ta.findCurrentMotion(0);
        assertNotNull(motion);
        PathOnTiles path = motion.getTiles(motion.duration());
        assertTrackHere(path);

    }

    /**
     *
     */
    public void testMove2() {
        AddTrainPreMove preMove = new AddTrainPreMove(0, new ImInts(0, 0),
                stationA, principal, defaultSchedule);
        Move m = preMove.generateMove(world);
        MoveStatus ms = m.doMove(world, Player.AUTHORITATIVE);
        assertTrue(ms.ok);
        ActivityIterator ai = world.getActivities(principal, 0);
        TrainMotion tm = (TrainMotion) ai.getActivity();
        assertEquals(0d, tm.duration());
        assertEquals(0d, tm.getSpeedAtEnd());
        assertEquals(0d, tm.getDistance(0));
        PositionOnTrack pot = tm.getFinalPosition();
        assertNotNull(pot);
        assertEquals(TileTransition.EAST, pot.facing());
        assertEquals(13, pot.getX());
        assertEquals(10, pot.getY());

    }

    /**
     *
     */
    public void testGetSchedule() {
        world = MapFixtureFactory2.getCopy();
        MoveExecutor me = new SimpleMoveExecutor(world, 0);
        principal = me.getPrincipal();
        ModelRoot mr = new ModelRootImpl();
        TrackMoveProducer producer = new TrackMoveProducer(me, world, mr);
        TileTransition[] trackPath = {TileTransition.EAST, TileTransition.SOUTH_EAST, TileTransition.SOUTH, TileTransition.SOUTH_WEST, TileTransition.WEST,
                TileTransition.NORTH_WEST, TileTransition.NORTH, TileTransition.NORTH_EAST};
        ImPoint from = new ImPoint(5, 5);
        MoveStatus ms = producer.buildTrack(from, trackPath);
        if (!ms.ok)
            throw new IllegalStateException(ms.message);

        TrainOrdersModel[] orders = {};
        ImmutableSchedule is = new ImmutableSchedule(orders, -1, false);
        AddTrainPreMove addTrain = new AddTrainPreMove(0, new ImInts(), from,
                principal, is);
        Move m = addTrain.generateMove(world);
        ms = m.doMove(world, principal);
        if (!ms.ok)
            throw new IllegalStateException(ms.message);

        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        assertNotNull(ta.getTarget());

    }

}
