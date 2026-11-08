/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
import freerails.model.operations.train.motion.TrainMotion;
import freerails.util.BidirectionalIterator;
import freerails.model.simulation.Time;
import freerails.model.track.OccupiedTracks;
import freerails.model.operations.train.motion.TrainMotion;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.move.*;
import freerails.move.generator.AddTrainMoveGenerator;
import freerails.move.generator.MoveTrainMoveGenerator;

import freerails.util.Utils;
import freerails.util.IntPoint2D;
import freerails.model.*;
import freerails.model.operations.cargo.*;
import freerails.model.player.Player;
import freerails.model.operations.station.StationDemand;
import freerails.model.operations.station.Station;
import freerails.model.terrain.TileTransition;
import freerails.model.operations.train.*;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import freerails.model.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Unit test for MoveTrainPreMove, tests stopping at stations.
 */
public class MoveTrainMoveGenerator2NdTest extends AbstractMoveTestCase {

    private Player player;
    private IntPoint2D station1Location;
    private IntPoint2D station2Location;

    /**
     * Performs increment time.
     * @param world the world state used by the test.
     * @param player the player to use in the test.
     */
    public static void incrementTime(World world, Player player) {
        BidirectionalIterator<TrainMotion> bidirectionalIterator = world.getTrain(player, 0).getActivities();
        while (bidirectionalIterator.hasNext())
            bidirectionalIterator.next();

        TrainMotion lastMotion = bidirectionalIterator.previous();
        double finishTime = lastMotion.getStartTime() + lastMotion.getDuration();
        Time fTime = new Time((int) Math.floor(finishTime));
        while (world.getClock().getCurrentTime().compareTo(fTime) < 0) {
            world.getClock().advanceTime();
        }
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
    /**
     * Sets up.
     * @throws Exception if the operation fails.
     */
    @Override
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
        IntPoint2D station0Location = new IntPoint2D(10, 10);

        Status status1 = trackBuilder.buildTrack(station0Location, track);
        assertTrue(status1.isSuccess());

        // Build 2 stations.
        Status ms1 = stationBuilder.buildStation(station0Location);
        assertTrue(ms1.isSuccess());
        station1Location = new IntPoint2D(20, 10);
        Status ms2 = stationBuilder.buildStation(station1Location);
        assertTrue(ms2.isSuccess());
        station2Location = new IntPoint2D(28, 10);
        Status ms3 = stationBuilder.buildStation(station2Location);
        assertTrue(ms3.isSuccess());

        TrainOrder order0 = TrainOrder.keepConsist(2);
        TrainOrder order1 = TrainOrder.keepConsist(0);
        ScheduleState schedule = new ScheduleState();
        schedule = schedule.addOrder(order0);
        schedule = schedule.addOrder(order1);

        IntPoint2D start = new IntPoint2D(10, 10);
        AddTrainMoveGenerator preMove = new AddTrainMoveGenerator(validEngineId, Arrays.asList(0, 0),
                start, player, schedule);
        Move move = preMove.generate(world);
        Status status = move.applicable(world);
        assertTrue(status.isSuccess());
        move.apply(world);
    }

    /**
     * Tests path finding behavior.
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
     * Advances to the next step.
     * @return the next step.
     */
    private TileTransition nextStep() {
        MoveTrainMoveGenerator preMove = new MoveTrainMoveGenerator(0, player, new OccupiedTracks(player, world));
        return preMove.nextStep(world);
    }

    /**
     * Test that when the train arrives at a non station tile it keeps moving.
     */
    public void testStops1() {

        for (int i = 0; i < 5; i++) {
            TrainMotion tm = moveTrain();
            PositionOnTrack positionOnTrack = tm.getFinalPosition();
            assertEquals(14 + i, positionOnTrack.getLocation().x());
            assertEquals(TrainState.READY, tm.getTrainState());
            assertTrue(tm.getSpeedAtEnd() > 0);
        }
    }

    /**
     * Verifies stop planning leaves the read-only world unchanged and drains moves.
     */
    public void testStopHandlerPlansWithoutMutatingWorld() {
        Train train = world.getTrain(player, 0);
        ScheduleState originalSchedule = new ScheduleState(train.getSchedule());
        TrainConfiguration originalConfiguration = train.getConfiguration();

        TrainStopsHandler handler = new TrainStopsHandler(0, player, world);
        handler.arrivesAtPoint(station2Location);

        assertEquals(originalSchedule, train.getSchedule());
        assertEquals(originalConfiguration, train.getConfiguration());
        assertFalse(handler.getMoves().isEmpty());
        assertEquals(originalSchedule, train.getSchedule());
        assertEquals(originalConfiguration, train.getConfiguration());
        assertTrue(handler.getMoves().isEmpty());
        assertEquals(originalConfiguration.getLength(), handler.getResultingTrainConfiguration().getLength());
    }

    /**
     * Performs move train.
     * @return the result of move train.
     */
    private TrainMotion moveTrain() {
        incrementTime(world, player);
        MoveTrainMoveGenerator preMove = new MoveTrainMoveGenerator(0, player, new OccupiedTracks(player, world));
        Move move = preMove.generate(world);
        Status status = move.applicable(world);
        assertTrue(status.getMessage(), status.isSuccess());
        move.apply(world);
        Train train = world.getTrain(player, 0);
        return train.findCurrentMotion(Integer.MAX_VALUE);
    }

    /**
     * Test that when the train arrives at a non scheduled station tile it stops,
     * drops off and picks up cargo, then continues
     */
    public void testStops2() {
        // Check that there two stations on the schedule: station0 and station2;
        Train train = world.getTrain(player, 0);
        UnmodifiableSchedule schedule = train.getSchedule();
        assertEquals(2, schedule.getNumberOfOrders());
        assertEquals(2, schedule.getOrder(0).getStationId());

        // Check the train should have 2 wagons for cargo #0
        List<Integer> expectedConsist = Arrays.asList(0, 0);
        List<Integer> actualConsist = train.getConsist();
        assertEquals(expectedConsist, actualConsist);

        addCargoAtStation(1, 800);

        // Move the train to just before station 1.
        PositionOnTrack positionOnTrack;
        TrainMotion trainMotion;
        do {
            trainMotion = moveTrain();
            positionOnTrack = trainMotion.getFinalPosition();
        } while (positionOnTrack.getLocation().x() < station1Location.x());
        assertEquals(station1Location.x(), positionOnTrack.getLocation().x());
        assertEquals(station1Location.y(), positionOnTrack.getLocation().y());
        assertEquals(TrainState.READY, trainMotion.getTrainState());

        // The next train motion should represent the stop at the station.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station1Location.x(), positionOnTrack.getLocation().x());
        assertEquals(station1Location.y(), positionOnTrack.getLocation().y());
        assertEquals(TrainState.STOPPED_AT_STATION, trainMotion.getTrainState());

        // 80 Units of cargo should have been transferred to the train!
        UnmodifiableCargoBatchBundle onTrain = train.getCargoBatchBundle();
        int amount = onTrain.getAmountOfType(0);
        assertEquals(80, amount);

        // Then the train should continue.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station1Location.x() + 1, positionOnTrack.getLocation().x());
        assertEquals(station1Location.y(), positionOnTrack.getLocation().y());
        assertEquals(TrainState.READY, trainMotion.getTrainState());
    }

    /**
     * Adds the specified amount of cargo #0 to the specified station.
     */
    private void addCargoAtStation(int stationId, int amount) {

        CargoBatch cargoBatch = new CargoBatch(0, new IntPoint2D(6, 6), new Time(0), stationId);
        CargoBatchBundle cargoBatchBundle = new CargoBatchBundle();
        cargoBatchBundle.addCargo(cargoBatch, amount);
        Station station = world.getStation(player, stationId);
        station.setCargoBatchBundle(cargoBatchBundle);
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
            x = positionOnTrack.getLocation().x();
        } while (x < station2Location.x());
        assertEquals(station2Location.x(), x);
        assertEquals(station2Location.y(), positionOnTrack.getLocation().y());
        assertEquals(TrainState.READY, trainMotion.getTrainState());

        // The train should be heading for station 1.
        Train train2 = world.getTrain(player, 0);
        UnmodifiableSchedule schedule1 = train2.getSchedule();
        assertEquals(0, schedule1.getCurrentOrderIndex());
        assertEquals(2, schedule1.getNextStationId());
        assertEquals(station2Location, TrainUtils.getTargetLocation(world, player, 0));

        // The next train motion should represent the stop at the station.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station2Location.x(), positionOnTrack.getLocation().x());
        assertEquals(station2Location.y(), positionOnTrack.getLocation().y());
        assertEquals(TrainState.STOPPED_AT_STATION, trainMotion.getTrainState());

        // The train should be heading for station 0.
        Train train1 = world.getTrain(player, 0);
        UnmodifiableSchedule schedule2 = train1.getSchedule();
        assertFalse(schedule2.equals(schedule1));
        assertEquals(1, schedule2.getCurrentOrderIndex());
        assertEquals(0, schedule2.getNextStationId());

        // 80 Units of cargo should have been transferred to the train!

        Train train = world.getTrain(player, 0);
        UnmodifiableCargoBatchBundle onTrain = train.getCargoBatchBundle();
        int amount = onTrain.getAmountOfType(0);
        assertEquals(80, amount);

        // Then the train should continue.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station2Location.x() - 1, positionOnTrack.getLocation().x());
        assertEquals(station2Location.y(), positionOnTrack.getLocation().y());
        assertEquals(TrainState.READY, trainMotion.getTrainState());
    }

    /**
     * Test that when the train <b>is</b> scheduled to wait for full load, it
     * waits.
     */
    public void testStops5() {
        putTrainAtStationWaiting4FullLoad();

        // Add enough cargo to fill up the train.
        addCargoAtStation(2, 70);

        TrainMotion trainMotion = moveTrain();
        PositionOnTrack positionOnTrack = trainMotion.getFinalPosition();

        assertEquals(station2Location.x() - 1, positionOnTrack.getLocation().x());
        assertEquals(station2Location.y(), positionOnTrack.getLocation().y());
        assertEquals(TrainState.READY, trainMotion.getTrainState());
    }

    /**
     * Performs put train at station waiting4 full load.
     */
    private void putTrainAtStationWaiting4FullLoad() {
        // Set wait until full on schedule.
        List<Integer> newConsist = Arrays.asList(0, 0);
        TrainOrder order0 = TrainOrder.specifiedConsist(2, newConsist, true);
        Train train2 = world.getTrain(player, 0);
        ScheduleState schedule = new ScheduleState(train2.getSchedule());
        schedule = schedule.setOrder(0, order0);
        train2.setSchedule(schedule);
        assertEquals(0, train2.getSchedule().getCurrentOrderIndex());
        assertTrue(train2.getSchedule().getOrder(0).isWaitUntilFull());

        // Add some cargo to station #2, but not enough to fill the train.
        addCargoAtStation(2, 20);

        // Move the train to just before station 2.
        PositionOnTrack positionOnTrack;
        TrainMotion trainMotion;
        do {
            trainMotion = moveTrain();
            positionOnTrack = trainMotion.getFinalPosition();
        } while (positionOnTrack.getLocation().x() < station2Location.x());
        assertEquals(station2Location.x(), positionOnTrack.getLocation().x());
        assertEquals(station2Location.y(), positionOnTrack.getLocation().y());
        assertEquals(TrainState.READY, trainMotion.getTrainState());

        // The train should now stop at the station
        // and wait for a full load.

        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();

        assertEquals(station2Location.y(), positionOnTrack.getLocation().y());
        assertEquals(TrainState.WAITING_FOR_FULL_LOAD, trainMotion.getTrainState());

        assertFalse("The train isn't full and there is no cargo to add, so we should be able to generate a move.",
                TrainUtils.isUpdateDue(world, player, 0));
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
        TrainOrder order0 = TrainOrder.specifiedConsist(2, newConsist, false);
        Train train = world.getTrain(player, 0);
        ScheduleState schedule = new ScheduleState(train.getSchedule());
        schedule = schedule.setOrder(0, order0);
        train.setSchedule(schedule);
        assertEquals(0, train.getSchedule().getCurrentOrderIndex());
        assertFalse(train.getSchedule().getOrder(0).isWaitUntilFull());

        // Then the train should continue.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station2Location.x() - 1, positionOnTrack.getLocation().x());
        assertEquals(station2Location.y(), positionOnTrack.getLocation().y());
        assertEquals(TrainState.READY, trainMotion.getTrainState());
    }

    /**
     * Tests that a train with 'select wagons automatically' enabled behaves correctly.
     */
    public void testAutoConsist() {
        // Remove all wagons from the train.
        Train train = world.getTrain(player, 0);
        train.setConsist(new ArrayList<>());

        // Change trains schedule to auto consist.
        TrainOrder order0 = TrainOrder.autoConsist(1);
        TrainOrder order1 = TrainOrder.autoConsist(2);
        ScheduleState schedule = new ScheduleState();
        schedule = schedule.addOrder(order0);
        schedule = schedule.addOrder(order1);

        Train train4 = world.getTrain(player, 0);
        assertEquals(0, train4.getSchedule().getCurrentOrderIndex());

        // Add 35 unit of cargo #0 to station 1.
        Station station0 = world.getStation(player, 1);
        CargoBatchBundle cargoBatchBundle = new CargoBatchBundle();
        final int AMOUNT_OF_CARGO = 35;
        cargoBatchBundle.addCargo(new CargoBatch(0, IntPoint2D.ZERO, new Time(0), 0), AMOUNT_OF_CARGO);
        station0.setCargoBatchBundle(cargoBatchBundle);

        // Make station2 demand cargo #0;
        StationDemand demand = new StationDemand(Set.of(0));
        Station station2 = world.getStation(player, 2);

        Station stationWithNewDemand = (Station) Utils.cloneBySerialisation(station2);
        stationWithNewDemand.setDemandForCargo(demand);
        world.removeStation(player, 2);
        world.addStation(player, stationWithNewDemand);

        // The current auto-consist behavior schedules the next stop as station 2.
        Train train3 = world.getTrain(player, 0);
        assertEquals(2, train3.getSchedule().getNextStationId());

        // Make train call at station 1.
        PositionOnTrack positionOnTrack;
        TrainMotion trainMotion;
        do {
            trainMotion = moveTrain();
            positionOnTrack = trainMotion.getFinalPosition();
        } while (positionOnTrack.getLocation().x() < station1Location.x());
        assertEquals(station1Location.x(), positionOnTrack.getLocation().x());
        assertEquals(station1Location.y(), positionOnTrack.getLocation().y());
        assertEquals(TrainState.READY, trainMotion.getTrainState());
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();

        // The train should be bound for station 2.
        Train train2 = world.getTrain(player, 0);
        assertEquals(2, train2.getSchedule().getNextStationId());

        // Under the current behavior, the auto-consist logic does not carry the
        // cargo into the train after the station visit.
        assertEquals(new ArrayList<>(0), world.getTrain(player, 0).getConsist());
        Train train1 = world.getTrain(player, 0);
        assertEquals(0, train1.getCargoBatchBundle().getAmountOfType(0));
    }

    /**
     * Verifies that the train move generator can generate an applicable move when an update is due.
     */
    public void testCanGenerateMove() {
        MoveTrainMoveGenerator preMove = new MoveTrainMoveGenerator(0, player, new OccupiedTracks(player, world));
        assertTrue(TrainUtils.isUpdateDue(world, player, 0));
        Move move = preMove.generate(world);
        Status status = move.applicable(world);
        assertTrue(status.getMessage(), status.isSuccess());
        move.apply(world);
        assertFalse(TrainUtils.isUpdateDue(world, player, 0));
    }

    /**
     * Tests that when extra wagons are added, the TrainMotion lengthens to
     * accommodate them.
     */
    public void testLengtheningTrain() {
        // Set the train to add wagons at station2.
        List<Integer> newConsist = Arrays.asList(0, 0, 0, 0, 0, 0);
        TrainOrder order0 = TrainOrder.specifiedConsist(2, newConsist, false);
        Train train = world.getTrain(player, 0);
        ScheduleState schedule = new ScheduleState(train.getSchedule());
        schedule = schedule.setOrder(0, order0);
        train.setSchedule(schedule);
        assertEquals(0, train.getSchedule().getCurrentOrderIndex());

        // Move the train to the station.
        PositionOnTrack positionOnTrack;
        TrainMotion trainMotion;
        do {
            trainMotion = moveTrain();
            positionOnTrack = trainMotion.getFinalPosition();
        } while (positionOnTrack.getLocation().x() < station2Location.x());
        assertEquals(station2Location.x(), positionOnTrack.getLocation().x());
        assertEquals(station2Location.y(), positionOnTrack.getLocation().y());
        assertEquals(TrainState.READY, trainMotion.getTrainState());

        assertEquals(2, train.getNumberOfWagons());

        assertTrue(trainMotion.getInitialPosition() >= train.getLength());

        trainMotion = moveTrain();
        train = world.getTrain(player, 0);
        assertEquals(6, train.getNumberOfWagons());
        assertTrue(trainMotion.getInitialPosition() >= train.getLength());
    }
}
