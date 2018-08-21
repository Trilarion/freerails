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
import freerails.model.train.activity.Activity;
import freerails.model.train.motion.TrainMotion;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.model.train.schedule.TrainOrder;
import freerails.move.*;
import freerails.move.generator.AddTrainMoveGenerator;
import freerails.model.MapFixtureFactory2;

import freerails.nove.Status;
import freerails.util.Vec2D;
import freerails.util.BidirectionalIterator;
import freerails.model.terrain.TileTransition;
import freerails.model.player.Player;
import freerails.model.train.*;
import freerails.model.train.schedule.Schedule;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Test for AddTrainPreMove.
 */
public class AddTrainMoveGeneratorTest extends AbstractMoveTestCase {

    private Player player;
    private UnmodifiableSchedule defaultSchedule;
    private Vec2D stationA;

    /**
     *
     */
    @Override
    protected void setupWorld() {
        world = MapFixtureFactory2.getCopy();
        validEngineId = world.getEngines().iterator().next().getId();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, world.getPlayer(0));
        player = moveExecutor.getPlayer();
        ModelRoot modelRoot = new ModelRootImpl();
        TrackMoveProducer trackBuilder = new TrackMoveProducer(moveExecutor, world, modelRoot);
        StationBuilder stationBuilder = new StationBuilder(moveExecutor);

        // Build track.
        stationBuilder.setStationType(stationBuilder.getTrackTypeID("terminal"));
        TileTransition[] track = {TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        stationA = new Vec2D(10, 10);
        Status ms0 = trackBuilder.buildTrack(stationA, track);
        assertTrue(ms0.isSuccess());

        // Build 2 stations.
        Status ms1 = stationBuilder.buildStation(stationA);
        assertTrue(ms1.isSuccess());
        Vec2D stationB = new Vec2D(19, 10);
        Status ms2 = stationBuilder.buildStation(stationB);
        assertTrue(ms2.isSuccess());

        TrainOrder order0 = new TrainOrder(0, null, false, false);
        TrainOrder order1 = new TrainOrder(1, null, false, false);
        Schedule schedule = new Schedule();
        schedule.addOrder(order0);
        schedule.addOrder(order1);
        defaultSchedule = schedule;
    }

    /**
     *
     */
    public void testMove() {
        AddTrainMoveGenerator preMove = new AddTrainMoveGenerator(validEngineId, Arrays.asList(0, 0), stationA, player, defaultSchedule);
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
        AddTrainMoveGenerator preMove = new AddTrainMoveGenerator(validEngineId, Arrays.asList(0, 0), stationA, player, defaultSchedule);
        Move move = preMove.generate(world);
        Status status = move.doMove(world, Player.AUTHORITATIVE);
        assertTrue(status.isSuccess());

        Train train = world.getTrain(player, 0);
        TrainMotion motion = train.findCurrentMotion(0);
        assertNotNull(motion);
        PathOnTiles path = motion.getTiles(motion.getDuration());
        assertTrackHere(path);
    }

    /**
     *
     */
    public void testMove2() {
        AddTrainMoveGenerator preMove = new AddTrainMoveGenerator(validEngineId, Arrays.asList(0, 0), stationA, player, defaultSchedule);
        Move move = preMove.generate(world);
        Status status = move.doMove(world, Player.AUTHORITATIVE);
        assertTrue(status.isSuccess());
        BidirectionalIterator<Activity> activities = world.getTrain(player, 0).getActivities();
        TrainMotion tm = (TrainMotion) activities.get();
        assertEquals(0.0d, tm.getDuration());
        assertEquals(0.0d, tm.getSpeedAtEnd());
        assertEquals(0.0d, tm.getDistance(0));
        PositionOnTrack positionOnTrack = tm.getFinalPosition();
        assertNotNull(positionOnTrack);
        assertEquals(TileTransition.EAST, positionOnTrack.getFacingTo());
        assertEquals(13, positionOnTrack.getLocation().x);
        assertEquals(10, positionOnTrack.getLocation().y);
    }

    /**
     *
     */
    public void testGetSchedule() {
        world = MapFixtureFactory2.getCopy();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, world.getPlayer(0));
        player = moveExecutor.getPlayer();
        ModelRoot modelRoot = new ModelRootImpl();
        TrackMoveProducer producer = new TrackMoveProducer(moveExecutor, world, modelRoot);
        TileTransition[] trackPath = {TileTransition.EAST, TileTransition.SOUTH_EAST, TileTransition.SOUTH, TileTransition.SOUTH_WEST, TileTransition.WEST,
                TileTransition.NORTH_WEST, TileTransition.NORTH, TileTransition.NORTH_EAST};
        Vec2D from = new Vec2D(5, 5);
        Status status = producer.buildTrack(from, trackPath);
        if (!status.isSuccess())
            throw new IllegalStateException(status.getMessage());

        TrainOrder[] orders = {};
        UnmodifiableSchedule schedule = new Schedule(orders, -1, false);
        AddTrainMoveGenerator addTrain = new AddTrainMoveGenerator(validEngineId, new ArrayList<>(), from, player, schedule);
        Move move = addTrain.generate(world);
        status = move.doMove(world, player);
        if (!status.isSuccess())
            throw new IllegalStateException(status.getMessage());

        assertNotNull(TrainUtils.getTargetLocation(world, player, 0));
    }
}
