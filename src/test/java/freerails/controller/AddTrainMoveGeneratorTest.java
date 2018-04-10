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
import freerails.move.*;
import freerails.move.generator.AddTrainMoveGenerator;
import freerails.model.MapFixtureFactory2;
import freerails.util.ImmutableList;
import freerails.util.Vec2D;
import freerails.model.ActivityIterator;
import freerails.model.terrain.TileTransition;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.player.Player;
import freerails.model.train.*;
import freerails.model.train.schedule.ImmutableSchedule;
import freerails.model.train.schedule.MutableSchedule;

/**
 * Test for AddTrainPreMove.
 */
public class AddTrainMoveGeneratorTest extends AbstractMoveTestCase {

    private FreerailsPrincipal principal;
    private ImmutableSchedule defaultSchedule;
    private Vec2D stationA;

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
        stationA = new Vec2D(10, 10);
        MoveStatus ms0 = trackBuilder.buildTrack(stationA, track);
        assertTrue(ms0.succeeds());

        // Build 2 stations.
        MoveStatus ms1 = stationBuilder.buildStation(stationA);
        assertTrue(ms1.succeeds());
        Vec2D stationB = new Vec2D(19, 10);
        MoveStatus ms2 = stationBuilder.buildStation(stationB);
        assertTrue(ms2.succeeds());

        TrainOrders order0 = new TrainOrders(0, null, false, false);
        TrainOrders order1 = new TrainOrders(1, null, false, false);
        MutableSchedule schedule = new MutableSchedule();
        schedule.addOrder(order0);
        schedule.addOrder(order1);
        defaultSchedule = schedule.toImmutableSchedule();
    }

    /**
     *
     */
    public void testMove() {
        AddTrainMoveGenerator preMove = new AddTrainMoveGenerator(0, new ImmutableList<>(0, 0), stationA, principal, defaultSchedule);
        Move move = preMove.generate(world);
        assertDoMoveIsOk(move);
        assertUndoMoveIsOk(move);
        assertSurvivesSerialisation(move);
    }

    /**
     * Check that the path on tiles created for the new train is actually on the
     * track.
     */
    public void testPathOnTiles() {
        AddTrainMoveGenerator preMove = new AddTrainMoveGenerator(0, new ImmutableList<>(0, 0), stationA, principal, defaultSchedule);
        Move move = preMove.generate(world);
        MoveStatus moveStatus = move.doMove(world, Player.AUTHORITATIVE);
        assertTrue(moveStatus.succeeds());

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
        AddTrainMoveGenerator preMove = new AddTrainMoveGenerator(0, new ImmutableList<>(0, 0),
                stationA, principal, defaultSchedule);
        Move move = preMove.generate(world);
        MoveStatus moveStatus = move.doMove(world, Player.AUTHORITATIVE);
        assertTrue(moveStatus.succeeds());
        ActivityIterator ai = world.getActivities(principal, 0);
        TrainMotion tm = (TrainMotion) ai.getActivity();
        assertEquals(0.0d, tm.duration());
        assertEquals(0.0d, tm.getSpeedAtEnd());
        assertEquals(0.0d, tm.getDistance(0));
        PositionOnTrack positionOnTrack = tm.getFinalPosition();
        assertNotNull(positionOnTrack);
        assertEquals(TileTransition.EAST, positionOnTrack.facing());
        assertEquals(13, positionOnTrack.getLocation().x);
        assertEquals(10, positionOnTrack.getLocation().y);
    }

    /**
     *
     */
    public void testGetSchedule() {
        world = MapFixtureFactory2.getCopy();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, 0);
        principal = moveExecutor.getPrincipal();
        ModelRoot modelRoot = new ModelRootImpl();
        TrackMoveProducer producer = new TrackMoveProducer(moveExecutor, world, modelRoot);
        TileTransition[] trackPath = {TileTransition.EAST, TileTransition.SOUTH_EAST, TileTransition.SOUTH, TileTransition.SOUTH_WEST, TileTransition.WEST,
                TileTransition.NORTH_WEST, TileTransition.NORTH, TileTransition.NORTH_EAST};
        Vec2D from = new Vec2D(5, 5);
        MoveStatus moveStatus = producer.buildTrack(from, trackPath);
        if (!moveStatus.succeeds())
            throw new IllegalStateException(moveStatus.getMessage());

        TrainOrders[] orders = {};
        ImmutableSchedule is = new ImmutableSchedule(orders, -1, false);
        AddTrainMoveGenerator addTrain = new AddTrainMoveGenerator(0, new ImmutableList<>(), from, principal, is);
        Move move = addTrain.generate(world);
        moveStatus = move.doMove(world, principal);
        if (!moveStatus.succeeds())
            throw new IllegalStateException(moveStatus.getMessage());

        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        assertNotNull(ta.getTargetLocation());
    }
}
