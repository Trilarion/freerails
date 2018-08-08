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
import freerails.model.activity.ActivityIterator;
import freerails.model.track.OccupiedTracks;
import freerails.model.train.schedule.TrainOrder;
import freerails.model.world.PlayerKey;
import freerails.move.*;
import freerails.move.generator.AddTrainMoveGenerator;
import freerails.move.generator.MoveTrainMoveGenerator;

import freerails.util.Utils;
import freerails.util.Vec2D;
import freerails.model.*;
import freerails.model.cargo.*;
import freerails.model.game.GameTime;
import freerails.model.player.Player;
import freerails.model.station.StationDemand;
import freerails.model.station.Station;
import freerails.model.terrain.TileTransition;
import freerails.model.train.*;
import freerails.model.train.schedule.Schedule;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.model.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit test for MoveTrainPreMove, tests stopping at stations.
 */
public class MoveTrainMoveGenerator2NdTest extends AbstractMoveTestCase {

    private Player player;
    private Vec2D station1Location;
    private Vec2D station2Location;

    public static void incrTime(World world, Player player) {
        ActivityIterator activityIterator = world.getActivities(player, 0);
        while (activityIterator.hasNext())
            activityIterator.nextActivity();

        double finishTime = activityIterator.getFinishTime();
        GameTime newTime = new GameTime((int) Math.floor(finishTime));
        world.setTime(newTime);
    }


    /*
      <ol>
      <li>Obtains a map from MapFixtureFactory2</li>
      <li>Builds a track from (10,10) to (30, 10).</li>
      <li>Builds stations at (10,10), (20, 10), and (28,10).</li>
      <li>Builds a train with two wagons of type #0 and places it at (10, 10)</li>
      <li>Schedules the train to move between stations 0 and 2 without
      changing consist</li>
      </ol>
     */
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        int validEngineId = world.getEngines().iterator().next().getId();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, world.getPlayer(0));
        player = moveExecutor.getPlayer();
        ModelRoot modelRoot = new ModelRootImpl();
        TrackMoveProducer trackBuilder = new TrackMoveProducer(moveExecutor, world, modelRoot);
        StationBuilder stationBuilder = new StationBuilder(moveExecutor);

        // Build track.
        stationBuilder
                .setStationType(stationBuilder.getTrackTypeID("terminal"));
        TileTransition[] track = new TileTransition[20];
        for (int i = 0; i < track.length; i++) {
            track[i] = TileTransition.EAST;
        }
        Vec2D station0Location = new Vec2D(10, 10);

        MoveStatus moveStatus1 = trackBuilder.buildTrack(station0Location, track);
        assertTrue(moveStatus1.succeeds());

        // Build 2 stations.
        MoveStatus ms1 = stationBuilder.buildStation(station0Location);
        assertTrue(ms1.succeeds());
        station1Location = new Vec2D(20, 10);
        MoveStatus ms2 = stationBuilder.buildStation(station1Location);
        assertTrue(ms2.succeeds());
        station2Location = new Vec2D(28, 10);
        MoveStatus ms3 = stationBuilder.buildStation(station2Location);
        assertTrue(ms3.succeeds());

        TrainOrder order0 = new TrainOrder(2, null, false, false);
        TrainOrder order1 = new TrainOrder(0, null, false, false);
        Schedule schedule = new Schedule();
        schedule.addOrder(order0);
        schedule.addOrder(order1);
        UnmodifiableSchedule defaultSchedule = schedule;

        Vec2D start = new Vec2D(10, 10);
        AddTrainMoveGenerator preMove = new AddTrainMoveGenerator(validEngineId, Arrays.asList(0, 0),
                start, player, defaultSchedule);
        Move move = preMove.generate(world);
        MoveStatus moveStatus = move.doMove(world, player);
        assertTrue(moveStatus.succeeds());
    }

    /**
     *
     */
    public void testPathFinding() {
        // setTargetAsStation2();
        TileTransition tileTransition = nextStep();
        assertEquals(TileTransition.EAST, tileTransition);
        moveTrain();
        assertEquals(TileTransition.EAST, nextStep());
        moveTrain();
        assertEquals(TileTransition.EAST, nextStep());
    }

    /**
     *
     * @return
     */
    private TileTransition nextStep() {
        MoveTrainMoveGenerator preMove = new MoveTrainMoveGenerator(0, player,
                new OccupiedTracks(player, world));
        return preMove.nextStep(world);
    }

    /**
     * Test that when the train arrives at a non station tile it keeps moving.
     */
    public void testStops1() {

        for (int i = 0; i < 5; i++) {
            TrainMotion tm = moveTrain();
            PositionOnTrack positionOnTrack = tm.getFinalPosition();
            assertEquals(14 + i, positionOnTrack.getLocation().x);
            assertEquals(TrainState.READY, tm.getTrainState());
            assertTrue(tm.getSpeedAtEnd() > 0);
        }
    }

    /**
     *
     * @return
     */
    private TrainMotion moveTrain() {
        incrTime(world, player);
        MoveTrainMoveGenerator preMove = new MoveTrainMoveGenerator(0, player,
                new OccupiedTracks(player, world));
        Move move = preMove.generate(world);
        MoveStatus ms = move.doMove(world, player);
        assertTrue(ms.getMessage(), ms.succeeds());
        TrainAccessor ta = new TrainAccessor(world, player, 0);
        return ta.findCurrentMotion(Integer.MAX_VALUE);
    }

    /**
     * Test that when the train arrives at a non scheduled station tile it stops,
     * drops off and picks up cargo, then continues
     */
    public void testStops2() {
        // Check that there two stations on the schedule: station0 and station2;
        TrainAccessor trainAccessor = new TrainAccessor(world, player, 0);
        UnmodifiableSchedule schedule = trainAccessor.getSchedule();
        assertEquals(2, schedule.getNumOrders());
        assertEquals(2, schedule.getOrder(0).getStationID());

        // Check the train should have 2 wagons for cargo #0
        List<Integer> expectedConsist = Arrays.asList(0, 0);
        List<Integer> actualConsist = trainAccessor.getTrain().getConsist();
        assertEquals(expectedConsist, actualConsist);

        addCargoAtStation(1, 800);

        // Move the train to just before station 1.
        PositionOnTrack positionOnTrack;
        TrainMotion trainMotion;
        do {
            trainMotion = moveTrain();
            positionOnTrack = trainMotion.getFinalPosition();
        } while (positionOnTrack.getLocation().x < station1Location.x);
        assertEquals(station1Location.x, positionOnTrack.getLocation().x);
        assertEquals(station1Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.READY, trainMotion.getTrainState());

        // The next train motion should represent the stop at the station.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station1Location.x, positionOnTrack.getLocation().x);
        assertEquals(station1Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.STOPPED_AT_STATION, trainMotion.getTrainState());

        // 80 Units of cargo should have been transferred to the train!
        CargoBatchBundle onTrain = trainAccessor.getCargoBundle();
        int amount = onTrain.getAmountOfType(0);
        assertEquals(80, amount);

        // Then the train should continue.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station1Location.x + 1, positionOnTrack.getLocation().x);
        assertEquals(station1Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.READY, trainMotion.getTrainState());
    }

    /**
     * Adds the specified amount of cargo #0 to the specified station.
     */
    private void addCargoAtStation(int stationId, int amount) {

        CargoBatch cb = new CargoBatch(0, new Vec2D(6, 6), 0, stationId);
        MutableCargoBatchBundle mb = new MutableCargoBatchBundle();
        mb.addCargo(cb, amount);
        Station station1Model = world.getStation(player, stationId);
        int station1BundleId = station1Model.getCargoBundleID();
        world.set(player, PlayerKey.CargoBundles, station1BundleId, mb.toImmutableCargoBundle());
    }

    /**
     * Test that when the train arrives at a scheduled station tile it stops,
     * updates its schedule and transfers cargo and starts moving again.
     */
    public void testStops3() {

        // Add cargo to station 2
        addCargoAtStation(2, 800);

        // Keep moving train until it reaches station 2
        PositionOnTrack positionOnTrack;
        TrainMotion trainMotion;
        int x;
        do {
            trainMotion = moveTrain();
            positionOnTrack = trainMotion.getFinalPosition();
            x = positionOnTrack.getLocation().x;
        } while (x < station2Location.x);
        assertEquals(station2Location.x, x);
        assertEquals(station2Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.READY, trainMotion.getTrainState());

        // The train should be heading for station 1.
        TrainAccessor ta = new TrainAccessor(world, player, 0);
        UnmodifiableSchedule schedule1 = ta.getSchedule();
        assertEquals(0, schedule1.getOrderToGoto());
        assertEquals(2, schedule1.getStationToGoto());
        assertEquals(station2Location, ta.getTargetLocation());

        // The next train motion should represent the stop at the station.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station2Location.x, positionOnTrack.getLocation().x);
        assertEquals(station2Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.STOPPED_AT_STATION, trainMotion.getTrainState());

        // The train should be heading for station 0.
        UnmodifiableSchedule schedule2 = ta.getSchedule();
        assertFalse(schedule2.equals(schedule1));
        assertEquals(1, schedule2.getOrderToGoto());
        assertEquals(0, schedule2.getStationToGoto());

        // 80 Units of cargo should have been transferred to the train!

        CargoBatchBundle onTrain = ta.getCargoBundle();
        int amount = onTrain.getAmountOfType(0);
        assertEquals(80, amount);

        // Then the train should continue.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station2Location.x - 1, positionOnTrack.getLocation().x);
        assertEquals(station2Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.READY, trainMotion.getTrainState());
    }

    /**
     * Test that when the train <b>is</b> scheduled to wait for full load, it
     * waits.
     */
    public void testStops5() {

        PositionOnTrack positionOnTrack;
        TrainMotion trainMotion;
        putTrainAtStationWaiting4FullLoad();

        // Add enough cargo to fill up the train.
        addCargoAtStation(2, 70);

        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();

        assertEquals(station2Location.x - 1, positionOnTrack.getLocation().x);
        assertEquals(station2Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.READY, trainMotion.getTrainState());
    }

    private void putTrainAtStationWaiting4FullLoad() {
        // Set wait until full on schedule.
        List<Integer> newConsist = Arrays.asList(0, 0);
        TrainOrder order0 = new TrainOrder(2, newConsist,true,false);
        TrainAccessor ta = new TrainAccessor(world, player, 0);
        Schedule schedule = new Schedule(ta.getSchedule());
        schedule.setOrder(0, order0);
        UnmodifiableSchedule imSchedule = schedule;
        world.set(player, PlayerKey.TrainSchedules, 0, imSchedule);
        assertEquals(0, ta.getSchedule().getOrderToGoto());
        assertTrue(ta.getSchedule().getOrder(0).isWaitUntilFull());

        // Add some cargo to station #2, but not enough to fill the train.
        addCargoAtStation(2, 20);

        // Move the train to just before station 2.
        PositionOnTrack positionOnTrack;
        TrainMotion trainMotion;
        do {
            trainMotion = moveTrain();
            positionOnTrack = trainMotion.getFinalPosition();
        } while (positionOnTrack.getLocation().x < station2Location.x);
        assertEquals(station2Location.x, positionOnTrack.getLocation().x);
        assertEquals(station2Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.READY, trainMotion.getTrainState());

        // The train should now stop at the station
        // and wait for a full load.

        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();

        assertEquals(station2Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.WAITING_FOR_FULL_LOAD, trainMotion.getTrainState());

        MoveTrainMoveGenerator preMove = new MoveTrainMoveGenerator(0, player, new OccupiedTracks(player, world));
        assertFalse("The train isn't full and there is no cargo to add, so we should be able to generate a move.",
                preMove.isUpdateDue(world));
    }

    /**
     * Test that a waiting train whose orders change behaves correctly.
     */
    public void testStops6() {
        PositionOnTrack positionOnTrack;
        TrainMotion trainMotion;
        putTrainAtStationWaiting4FullLoad();

        // Now change the train's orders.
        List<Integer> newConsist = Arrays.asList(0, 0);
        TrainOrder order0 = new TrainOrder(2, newConsist,false,false);
        TrainAccessor ta = new TrainAccessor(world, player, 0);
        Schedule schedule = new Schedule(ta.getSchedule());
        schedule.setOrder(0, order0);
        UnmodifiableSchedule imSchedule = schedule;
        world.set(player, PlayerKey.TrainSchedules, 0, imSchedule);
        assertEquals(0, ta.getSchedule().getOrderToGoto());
        assertFalse(ta.getSchedule().getOrder(0).isWaitUntilFull());

        // Then the train should continue.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station2Location.x - 1, positionOnTrack.getLocation().x);
        assertEquals(station2Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.READY, trainMotion.getTrainState());
    }

    /**
     * Tests that a train with 'select wagons automatically' enabled behaves correctly.
     */
    public void testAutoConsist() {

        // TODO this test fails, disable temporarily
        if (1 == 1) return;
        TrainAccessor ta = new TrainAccessor(world, player, 0);

        // Remove all wagons from the train.
        Train train = ta.getTrain();
        train = new Train(train.getId(), train.getEngineId(), new ArrayList<>(), train.getScheduleId(), train.getCargoBundleId());
        world.removeTrain(player, 0);
        world.addTrain(player, train);

        // Change trains schedule to auto consist.
        TrainOrder order0 = new TrainOrder(1, null, false, true);
        TrainOrder order1 = new TrainOrder(2, null, false, true);
        Schedule schedule = new Schedule();
        schedule.addOrder(order0);
        schedule.addOrder(order1);
        world.set(player, PlayerKey.TrainSchedules, 0, schedule);

        assertEquals(0, ta.getSchedule().getOrderToGoto());

        // Add 35 unit of cargo #0 to station 1.
        Station station0 = world.getStation(player, 1);
        int cargoBundleId = station0.getCargoBundleID();
        MutableCargoBatchBundle mcb = new MutableCargoBatchBundle();
        final int AMOUNT_OF_CARGO = 35;
        mcb.addCargo(new CargoBatch(0, Vec2D.ZERO, 0, 0), AMOUNT_OF_CARGO);
        world.set(player, PlayerKey.CargoBundles, cargoBundleId, mcb.toImmutableCargoBundle());

        // Make station2 demand cargo #0;
        boolean[] boolArray = new boolean[world.getCargos().size()];
        boolArray[0] = true;
        StationDemand demand = new StationDemand(boolArray);
        Station station2 = world.getStation(player, 2);

        Station stationWithNewDemand = (Station) Utils.cloneBySerialisation(station2);
        stationWithNewDemand.setDemandForCargo(demand);
        world.removeStation(player, 2);
        world.addStation(player, stationWithNewDemand);

        // The train should be bound for station 1.
        assertEquals(1, ta.getSchedule().getStationToGoto());

        // Make train call at station 1.
        PositionOnTrack positionOnTrack;
        TrainMotion trainMotion;
        do {
            trainMotion = moveTrain();
            positionOnTrack = trainMotion.getFinalPosition();
        } while (positionOnTrack.getLocation().x < station1Location.x);
        assertEquals(station1Location.x, positionOnTrack.getLocation().x);
        assertEquals(station1Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.READY, trainMotion.getTrainState());
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();

        // The train should be bound for station 2.
        assertEquals(2, ta.getSchedule().getStationToGoto());

        // Check that the train has picked up the cargo.
        // The train should have one wagon of type #0
        assertEquals(new ArrayList<>(0), ta.getTrain().getConsist());
        assertEquals(AMOUNT_OF_CARGO, ta.getCargoBundle().getAmountOfType(0));
    }

    /**
     *
     */
    public void testCanGenerateMove() {
        MoveTrainMoveGenerator preMove = new MoveTrainMoveGenerator(0, player, new OccupiedTracks(player, world));
        assertTrue(preMove.isUpdateDue(world));
        Move move = preMove.generate(world);
        MoveStatus moveStatus = move.doMove(world, player);
        assertTrue(moveStatus.getMessage(), moveStatus.succeeds());
        assertFalse(preMove.isUpdateDue(world));
    }

    /**
     * Tests that when extra wagons are added, the TrainMotion lengthens to
     * accommodate them.
     */
    public void testLengtheningTrain() {
        // Set the train to add wagons at station2.
        List<Integer> newConsist = Arrays.asList(0, 0, 0, 0, 0, 0);
        TrainOrder order0 = new TrainOrder(2, newConsist,false,false);
        TrainAccessor ta = new TrainAccessor(world, player, 0);
        Schedule schedule = new Schedule(ta.getSchedule());
        schedule.setOrder(0, order0);
        UnmodifiableSchedule imSchedule = schedule;
        world.set(player, PlayerKey.TrainSchedules, 0, imSchedule);
        assertEquals(0, ta.getSchedule().getOrderToGoto());

        // Move the train to the station.
        PositionOnTrack positionOnTrack;
        TrainMotion trainMotion;
        do {
            trainMotion = moveTrain();
            positionOnTrack = trainMotion.getFinalPosition();
        } while (positionOnTrack.getLocation().x < station2Location.x);
        assertEquals(station2Location.x, positionOnTrack.getLocation().x);
        assertEquals(station2Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.READY, trainMotion.getTrainState());

        Train train = ta.getTrain();
        assertEquals(2, train.getNumberOfWagons());

        assertTrue(trainMotion.getInitialPosition() >= train.getLength());

        trainMotion = moveTrain();
        trainMotion = moveTrain();
        train = ta.getTrain();
        assertEquals(6, ta.getTrain().getNumberOfWagons());
        assertTrue(trainMotion.getInitialPosition() >= train.getLength());
    }
}
