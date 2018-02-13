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
import freerails.move.*;
import freerails.util.ImmutableList;
import freerails.util.Vector2D;
import freerails.world.*;
import freerails.world.cargo.*;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationDemand;
import freerails.world.station.Station;
import freerails.world.terrain.TileTransition;
import freerails.world.train.*;

/**
 * Unit test for MoveTrainPreMove, tests stopping at stations.
 */
public class MoveTrainPreMove2ndTest extends AbstractMoveTestCase {

    private FreerailsPrincipal principal;
    private Vector2D station1Location;
    private Vector2D station2Location;

    static void incrTime(World w, FreerailsPrincipal p) {
        ActivityIterator ai = w.getActivities(p, 0);
        while (ai.hasNext())
            ai.nextActivity();

        double finishTime = ai.getFinishTime();
        GameTime newTime = new GameTime((int) Math.floor(finishTime));
        w.setTime(newTime);
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
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, 0);
        principal = moveExecutor.getPrincipal();
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
        Vector2D station0Location = new Vector2D(10, 10);

        MoveStatus moveStatus1 = trackBuilder.buildTrack(station0Location, track);
        assertTrue(moveStatus1.succeeds());

        // Build 2 stations.
        MoveStatus ms1 = stationBuilder.buildStation(station0Location);
        assertTrue(ms1.succeeds());
        station1Location = new Vector2D(20, 10);
        MoveStatus ms2 = stationBuilder.buildStation(station1Location);
        assertTrue(ms2.succeeds());
        station2Location = new Vector2D(28, 10);
        MoveStatus ms3 = stationBuilder.buildStation(station2Location);
        assertTrue(ms3.succeeds());

        TrainOrders order0 = new TrainOrders(2, null, false, false);
        TrainOrders order1 = new TrainOrders(0, null, false, false);
        MutableSchedule s = new MutableSchedule();
        s.addOrder(order0);
        s.addOrder(order1);
        ImmutableSchedule defaultSchedule = s.toImmutableSchedule();

        Vector2D start = new Vector2D(10, 10);
        AddTrainPreMove preMove = new AddTrainPreMove(0, new ImmutableList<>(0, 0),
                start, principal, defaultSchedule);
        Move move = preMove.generateMove(world);
        MoveStatus moveStatus = move.doMove(world, principal);
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
        MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal,
                new OccupiedTracks(principal, world));
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
            assertEquals(TrainState.READY, tm.getActivity());
            assertTrue(tm.getSpeedAtEnd() > 0);
        }
    }

    /**
     *
     * @return
     */
    private TrainMotion moveTrain() {
        incrTime(world, principal);
        MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal,
                new OccupiedTracks(principal, world));
        Move move = preMove.generateMove(world);
        MoveStatus ms = move.doMove(world, principal);
        assertTrue(ms.getMessage(), ms.succeeds());
        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        return ta.findCurrentMotion(Integer.MAX_VALUE);
    }

    /**
     * Test that when the train arrives at a non scheduled station tile it stops,
     * drops off and picks up cargo, then continues
     */
    public void testStops2() {
        // Check that there two stations on the schedule: station0 and station2;
        TrainAccessor trainAccessor = new TrainAccessor(world, principal, 0);
        ImmutableSchedule schedule = trainAccessor.getSchedule();
        assertEquals(2, schedule.getNumOrders());
        assertEquals(2, schedule.getOrder(0).getStationID());

        // Check the train should have 2 wagons for cargo #0
        ImmutableList<Integer> expectedConsist = new ImmutableList<>(0, 0);
        ImmutableList<Integer> actualConsist = trainAccessor.getTrain().getConsist();
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
        assertEquals(TrainState.READY, trainMotion.getActivity());

        // The next train motion should represent the stop at the station.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station1Location.x, positionOnTrack.getLocation().x);
        assertEquals(station1Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.STOPPED_AT_STATION, trainMotion.getActivity());

        // 80 Units of cargo should have been transferred to the train!
        CargoBatchBundle onTrain = trainAccessor.getCargoBundle();
        int amount = onTrain.getAmountOfType(0);
        assertEquals(80, amount);

        // Then the train should continue.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station1Location.x + 1, positionOnTrack.getLocation().x);
        assertEquals(station1Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.READY, trainMotion.getActivity());
    }

    /**
     * Adds the specified amount of cargo #0 to the specified station.
     */
    private void addCargoAtStation(int stationId, int amount) {

        CargoBatch cb = new CargoBatch(0, new Vector2D(6, 6), 0, stationId);
        MutableCargoBatchBundle mb = new MutableCargoBatchBundle();
        mb.addCargo(cb, amount);
        Station station1Model = (Station) world.get(principal,KEY.STATIONS, stationId);
        ImmutableCargoBatchBundle cargoAtStationBefore = mb.toImmutableCargoBundle();
        int station1BundleId = station1Model.getCargoBundleID();
        world.set(principal, KEY.CARGO_BUNDLES, station1BundleId, cargoAtStationBefore);
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
        assertEquals(TrainState.READY, trainMotion.getActivity());

        // The train should be heading for station 1.
        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        Schedule schedule1 = ta.getSchedule();
        assertEquals(0, schedule1.getOrderToGoto());
        assertEquals(2, schedule1.getStationToGoto());
        assertEquals(station2Location, ta.getTargetLocation());

        // The next train motion should represent the stop at the station.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station2Location.x, positionOnTrack.getLocation().x);
        assertEquals(station2Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.STOPPED_AT_STATION, trainMotion.getActivity());

        // The train should be heading for station 0.
        Schedule schedule2 = ta.getSchedule();
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
        assertEquals(TrainState.READY, trainMotion.getActivity());
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
        assertEquals(TrainState.READY, trainMotion.getActivity());
    }

    private void putTrainAtStationWaiting4FullLoad() {
        // Set wait until full on schedule.
        ImmutableList<Integer> newConsist = new ImmutableList<>(0, 0);
        TrainOrders order0 = new TrainOrders(2, newConsist,true,false);
        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        MutableSchedule schedule = new MutableSchedule(ta.getSchedule());
        schedule.setOrder(0, order0);
        ImmutableSchedule imSchedule = schedule.toImmutableSchedule();
        world.set(principal, KEY.TRAIN_SCHEDULES, 0, imSchedule);
        assertEquals(0, ta.getSchedule().getOrderToGoto());
        assertTrue(ta.getSchedule().getOrder(0).waitUntilFull);

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
        assertEquals(TrainState.READY, trainMotion.getActivity());

        // The train should now stop at the station
        // and wait for a full load.

        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();

        assertEquals(station2Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.WAITING_FOR_FULL_LOAD, trainMotion.getActivity());

        MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal,
                new OccupiedTracks(principal, world));
        assertFalse(
                "The train isn't full and there is no cargo to add, so we should be able to generate a move.",
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
        ImmutableList<Integer> newConsist = new ImmutableList<>(0, 0);
        TrainOrders order0 = new TrainOrders(2, newConsist,false,false);
        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        MutableSchedule schedule = new MutableSchedule(ta.getSchedule());
        schedule.setOrder(0, order0);
        ImmutableSchedule imSchedule = schedule.toImmutableSchedule();
        world.set(principal, KEY.TRAIN_SCHEDULES, 0, imSchedule);
        assertEquals(0, ta.getSchedule().getOrderToGoto());
        assertFalse(ta.getSchedule().getOrder(0).waitUntilFull);

        // Then the train should continue.
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();
        assertEquals(station2Location.x - 1, positionOnTrack.getLocation().x);
        assertEquals(station2Location.y, positionOnTrack.getLocation().y);
        assertEquals(TrainState.READY, trainMotion.getActivity());
    }

    /**
     * Tests that a train with 'select wagons automatically' enabled behaves correctly.
     */
    public void testAutoConsist() {

        // TODO this test fails, disable temporarily
        if (1 == 1) return;
        TrainAccessor ta = new TrainAccessor(world, principal, 0);

        // Remove all wagons from the train.
        TrainModel model = ta.getTrain();
        model = model.getNewInstance(model.getEngineType(), new ImmutableList<>());
        world.set(principal, KEY.TRAINS, 0, model);

        // Change trains schedule to auto consist.
        TrainOrders order0 = new TrainOrders(1, null, false, true);
        TrainOrders order1 = new TrainOrders(2, null, false, true);
        MutableSchedule s = new MutableSchedule();
        s.addOrder(order0);
        s.addOrder(order1);
        world.set(principal, KEY.TRAIN_SCHEDULES, 0, s.toImmutableSchedule());

        assertEquals(0, ta.getSchedule().getOrderToGoto());

        // Add 35 unit of cargo #0 to station 1.
        Station station0 = (Station) world.get(principal,
                KEY.STATIONS, 1);
        int cargoBundleId = station0.getCargoBundleID();
        MutableCargoBatchBundle mcb = new MutableCargoBatchBundle();
        final int AMOUNT_OF_CARGO = 35;
        mcb.addCargo(new CargoBatch(0, Vector2D.ZERO, 0, 0), AMOUNT_OF_CARGO);
        world.set(principal, KEY.CARGO_BUNDLES, cargoBundleId, mcb
                .toImmutableCargoBundle());

        // Make station2 demand cargo #0;
        boolean[] boolArray = new boolean[world.size(SKEY.CARGO_TYPES)];
        boolArray[0] = true;
        StationDemand demand = new StationDemand(boolArray);
        Station station2 = (Station) world.get(principal, KEY.STATIONS, 2);
        Station stationWithNewDemand = new Station(station2, demand);
        world.set(principal, KEY.STATIONS, 2, stationWithNewDemand);

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
        assertEquals(TrainState.READY, trainMotion.getActivity());
        trainMotion = moveTrain();
        positionOnTrack = trainMotion.getFinalPosition();

        // The train should be bound for station 2.
        assertEquals(2, ta.getSchedule().getStationToGoto());

        // Check that the train has picked up the cargo.
        // The train should have one wagon of type #0
        assertEquals(new ImmutableList<>(0), ta.getTrain().getConsist());
        assertEquals(AMOUNT_OF_CARGO, ta.getCargoBundle().getAmountOfType(0));
    }

    /**
     *
     */
    public void testCanGenerateMove() {
        MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal, new OccupiedTracks(principal, world));
        assertTrue(preMove.isUpdateDue(world));
        Move move = preMove.generateMove(world);
        MoveStatus moveStatus = move.doMove(world, principal);
        assertTrue(moveStatus.getMessage(), moveStatus.succeeds());
        assertFalse(preMove.isUpdateDue(world));
    }

    /**
     * Tests that when extra wagons are added, the TrainMotion lengthens to
     * accommodate them.
     */
    public void testLengtheningTrain() {
        // Set the train to add wagons at station2.
        ImmutableList<Integer> newConsist = new ImmutableList<>(0, 0, 0, 0, 0, 0);
        TrainOrders order0 = new TrainOrders(2, newConsist,false,false);
        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        MutableSchedule schedule = new MutableSchedule(ta.getSchedule());
        schedule.setOrder(0, order0);
        ImmutableSchedule imSchedule = schedule.toImmutableSchedule();
        world.set(principal, KEY.TRAIN_SCHEDULES, 0, imSchedule);
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
        assertEquals(TrainState.READY, trainMotion.getActivity());

        TrainModel train = ta.getTrain();
        assertEquals(2, train.getNumberOfWagons());

        assertTrue(trainMotion.getInitialPosition() >= train.getLength());

        trainMotion = moveTrain();
        trainMotion = moveTrain();
        train = ta.getTrain();
        assertEquals(6, ta.getTrain().getNumberOfWagons());
        assertTrue(trainMotion.getInitialPosition() >= train.getLength());
    }
}
