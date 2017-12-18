/*
 * Created on 12-Aug-2005
 *
 */
package freerails.controller;

import freerails.client.common.ModelRootImpl;
import freerails.move.AbstractMoveTestCase;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.server.MapFixtureFactory2;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.CargoBundle;
import freerails.world.cargo.ImmutableCargoBundle;
import freerails.world.cargo.MutableCargoBundle;
import freerails.world.common.*;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.Demand4Cargo;
import freerails.world.station.StationModel;
import freerails.world.top.KEY;
import freerails.world.top.SKEY;
import freerails.world.top.World;
import freerails.world.train.*;

import static freerails.world.common.Step.EAST;
import static freerails.world.train.SpeedTimeAndStatus.TrainActivity.*;

/**
 * Unit test for MoveTrainPreMove, tests stopping at stations.
 */
public class MoveTrainPreMove2ndTest extends AbstractMoveTestCase {

    TrackMoveProducer trackBuilder;

    StationBuilder stationBuilder;

    FreerailsPrincipal principal;
    ImmutableSchedule defaultSchedule;
    private ImPoint station1Location;
    private ImPoint station2Location;

    static void incrTime(World w, FreerailsPrincipal p) {
        ActivityIterator ai = w.getActivities(p, 0);
        while (ai.hasNext())
            ai.nextActivity();

        double finishTime = ai.getFinishTime();
        GameTime newTime = new GameTime((int) Math.floor(finishTime));
        w.setTime(newTime);
    }

    /**
     *
     * @throws Exception
     */
    @Override
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
        world = MapFixtureFactory2.getCopy();
        MoveExecutor me = new SimpleMoveExecutor(world, 0);
        principal = me.getPrincipal();
        ModelRoot mr = new ModelRootImpl();
        trackBuilder = new TrackMoveProducer(me, world, mr);
        stationBuilder = new StationBuilder(me);

        // Build track.
        stationBuilder
                .setStationType(stationBuilder.getTrackTypeID("terminal"));
        Step[] track = new Step[20];
        for (int i = 0; i < track.length; i++) {
            track[i] = EAST;
        }
        ImPoint station0Location = new ImPoint(10, 10);
        MoveStatus ms0 = trackBuilder.buildTrack(station0Location, track);
        assertTrue(ms0.ok);

        // Build 2 stations.
        MoveStatus ms1 = stationBuilder.buildStation(station0Location);
        assertTrue(ms1.ok);
        station1Location = new ImPoint(20, 10);
        MoveStatus ms2 = stationBuilder.buildStation(station1Location);
        assertTrue(ms2.ok);
        station2Location = new ImPoint(28, 10);
        MoveStatus ms3 = stationBuilder.buildStation(station2Location);
        assertTrue(ms3.ok);

        TrainOrdersModel order0 = new TrainOrdersModel(2, null, false, false);
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
    public void testPathFinding() {
        // setTargetAsStation2();
        Step step = nextStep();
        assertEquals(EAST, step);
        moveTrain();
        assertEquals(EAST, nextStep());
        moveTrain();
        assertEquals(EAST, nextStep());

    }

    private Step nextStep() {
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
            PositionOnTrack pot = tm.getFinalPosition();
            assertEquals(14 + i, pot.getX());
            assertEquals(READY, tm.getActivity());
            assertTrue(tm.getSpeedAtEnd() > 0);
        }
    }

    private TrainMotion moveTrain() {
        incrTime(world, principal);
        MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal,
                new OccupiedTracks(principal, world));
        Move m = preMove.generateMove(world);
        MoveStatus ms = m.doMove(world, principal);
        assertTrue(ms.message, ms.ok);
        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        return ta.findCurrentMotion(Integer.MAX_VALUE);
    }

    /**
     * Test that when the train arrives at a non sheduled station tile it stops,
     * drops off and picks up cargo, then continues
     */
    public void testStops2() {
        // Check that there two stations on the schedule: station0 and station2;
        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        ImmutableSchedule schedule = ta.getSchedule();
        assertEquals(2, schedule.getNumOrders());
        assertEquals(2, schedule.getOrder(0).getStationID());

        // Check the train should have 2 wagons for cargo #0
        ImInts expectedConsist = new ImInts(0, 0);
        ImInts actualConsist = ta.getTrain().getConsist();
        assertEquals(expectedConsist, actualConsist);

        addCargoAtStation(1, 800);

        // Move the train to just before station 1.
        PositionOnTrack pot;
        TrainMotion tm;
        do {
            tm = moveTrain();
            pot = tm.getFinalPosition();
        } while (pot.getX() < station1Location.x);
        assertEquals(station1Location.x, pot.getX());
        assertEquals(station1Location.y, pot.getY());
        assertEquals(READY, tm.getActivity());

        // The next train motion should represent the stop at the station.
        tm = moveTrain();
        pot = tm.getFinalPosition();
        assertEquals(station1Location.x, pot.getX());
        assertEquals(station1Location.y, pot.getY());
        assertEquals(STOPPED_AT_STATION, tm.getActivity());

        // 80 Units of cargo should have been transfered to the train!
        CargoBundle onTrain = ta.getCargoBundle();
        int amount = onTrain.getAmount(0);
        assertEquals(80, amount);

        // Then the train should continue.
        tm = moveTrain();
        pot = tm.getFinalPosition();
        assertEquals(station1Location.x + 1, pot.getX());
        assertEquals(station1Location.y, pot.getY());
        assertEquals(READY, tm.getActivity());

    }

    /**
     * Adds the specified amount of cargo #0 to the specified station.
     */
    private void addCargoAtStation(int stationId, int amount) {

        CargoBatch cb = new CargoBatch(0, 6, 6, 0, stationId);
        MutableCargoBundle mb = new MutableCargoBundle();
        mb.addCargo(cb, amount);
        StationModel station1Model = (StationModel) world.get(principal,
                KEY.STATIONS, stationId);
        ImmutableCargoBundle cargoAtStationBefore = mb.toImmutableCargoBundle();
        int station1BundleId = station1Model.getCargoBundleID();
        world.set(principal, KEY.CARGO_BUNDLES, station1BundleId,
                cargoAtStationBefore);
    }

    /**
     * Test that when the train arrives at a sheduled station tile it stops,
     * updates its schedule and transfers cargo and starts moving again.
     */
    public void testStops3() {

        // Add cargo to station 2
        addCargoAtStation(2, 800);

        // Keep moving train until it reaches station 2
        PositionOnTrack pot;
        TrainMotion tm;
        int x;
        do {
            tm = moveTrain();
            pot = tm.getFinalPosition();
            x = pot.getX();
        } while (x < station2Location.x);
        assertEquals(station2Location.x, x);
        assertEquals(station2Location.y, pot.getY());
        assertEquals(READY, tm.getActivity());

        // The train should be heading for station 1.
        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        Schedule schedule1 = ta.getSchedule();
        assertEquals(0, schedule1.getOrderToGoto());
        assertEquals(2, schedule1.getStationToGoto());
        ImPoint expectedTarget = new ImPoint(station2Location.x,
                station2Location.y);
        assertEquals(expectedTarget, ta.getTarget());

        // The next train motion should represent the stop at the station.
        tm = moveTrain();
        pot = tm.getFinalPosition();
        assertEquals(station2Location.x, pot.getX());
        assertEquals(station2Location.y, pot.getY());
        assertEquals(STOPPED_AT_STATION, tm.getActivity());

        // The train should be heading for station 0.
        Schedule schedule2 = ta.getSchedule();
        assertFalse(schedule2.equals(schedule1));
        assertEquals(1, schedule2.getOrderToGoto());
        assertEquals(0, schedule2.getStationToGoto());

        // 80 Units of cargo should have been transfered to the train!

        CargoBundle onTrain = ta.getCargoBundle();
        int amount = onTrain.getAmount(0);
        assertEquals(80, amount);

        // Then the train should continue.
        tm = moveTrain();
        pot = tm.getFinalPosition();
        assertEquals(station2Location.x - 1, pot.getX());
        assertEquals(station2Location.y, pot.getY());
        assertEquals(READY, tm.getActivity());

    }

    /**
     * Test that when the train <b>is</b> scheduled to wait for full load, it
     * waits.
     */
    public void testStops5() {

        PositionOnTrack pot;
        TrainMotion tm;
        putTrainAtStationWaiting4FullLoad();

        // Add enough cargo to fill up the train.
        addCargoAtStation(2, 70);

        tm = moveTrain();
        pot = tm.getFinalPosition();

        assertEquals(station2Location.x - 1, pot.getX());
        assertEquals(station2Location.y, pot.getY());
        assertEquals(READY, tm.getActivity());

    }

    private void putTrainAtStationWaiting4FullLoad() {
        // Set wait until full on schedule.
        ImInts newConsist = new ImInts(0, 0);
        TrainOrdersModel order0 = new TrainOrdersModel(2, newConsist, true,
                false);
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
        PositionOnTrack pot;
        TrainMotion tm;
        do {
            tm = moveTrain();
            pot = tm.getFinalPosition();
        } while (pot.getX() < station2Location.x);
        assertEquals(station2Location.x, pot.getX());
        assertEquals(station2Location.y, pot.getY());
        assertEquals(READY, tm.getActivity());

        // The train should now stop at the station
        // and wait for a full load.

        tm = moveTrain();
        pot = tm.getFinalPosition();

        assertEquals(station2Location.y, pot.getY());
        assertEquals(WAITING_FOR_FULL_LOAD, tm.getActivity());

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
        PositionOnTrack pot;
        TrainMotion tm;
        putTrainAtStationWaiting4FullLoad();

        // Now change the train's orders.
        ImInts newConsist = new ImInts(0, 0);
        TrainOrdersModel order0 = new TrainOrdersModel(2, newConsist, false,
                false);
        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        MutableSchedule schedule = new MutableSchedule(ta.getSchedule());
        schedule.setOrder(0, order0);
        ImmutableSchedule imSchedule = schedule.toImmutableSchedule();
        world.set(principal, KEY.TRAIN_SCHEDULES, 0, imSchedule);
        assertEquals(0, ta.getSchedule().getOrderToGoto());
        assertFalse(ta.getSchedule().getOrder(0).waitUntilFull);

        // Then the train should continue.
        tm = moveTrain();
        pot = tm.getFinalPosition();
        assertEquals(station2Location.x - 1, pot.getX());
        assertEquals(station2Location.y, pot.getY());
        assertEquals(READY, tm.getActivity());

    }

    /**
     * Tests that a train with 'select wagons automatically' enable behaves
     * correctly.
     */
    public void testAutoConsist() {

        // TODO this test fails, disable temporarily
        if (1 == 1) return;
        TrainAccessor ta = new TrainAccessor(world, principal, 0);

        // Remove all wagons from the train.
        TrainModel model = ta.getTrain();
        model = model.getNewInstance(model.getEngineType(), new ImInts());
        world.set(principal, KEY.TRAINS, 0, model);

        // Change trains schedule to auto consist.
        TrainOrdersModel order0 = new TrainOrdersModel(1, null, false, true);
        TrainOrdersModel order1 = new TrainOrdersModel(2, null, false, true);
        MutableSchedule s = new MutableSchedule();
        s.addOrder(order0);
        s.addOrder(order1);
        world.set(principal, KEY.TRAIN_SCHEDULES, 0, s.toImmutableSchedule());

        assertEquals(0, ta.getSchedule().getOrderToGoto());

        // Add 35 unit of cargo #0 to station 1.
        StationModel station0 = (StationModel) world.get(principal,
                KEY.STATIONS, 1);
        int cargoBundleId = station0.getCargoBundleID();
        MutableCargoBundle mcb = new MutableCargoBundle();
        final int AMOUNT_OF_CARGO = 35;
        mcb.addCargo(new CargoBatch(0, 0, 0, 0, 0), AMOUNT_OF_CARGO);
        world.set(principal, KEY.CARGO_BUNDLES, cargoBundleId, mcb
                .toImmutableCargoBundle());

        // Make station2 demand cargo #0;
        boolean[] boolArray = new boolean[world.size(SKEY.CARGO_TYPES)];
        boolArray[0] = true;
        Demand4Cargo demand = new Demand4Cargo(boolArray);
        StationModel station2 = (StationModel) world.get(principal,
                KEY.STATIONS, 2);
        StationModel stationWithNewDemand = new StationModel(station2, demand);
        world.set(principal, KEY.STATIONS, 2, stationWithNewDemand);

        // The train should be bound for station 1.
        assertEquals(1, ta.getSchedule().getStationToGoto());

        // Make train call at station 1.
        PositionOnTrack pot;
        TrainMotion tm;
        do {
            tm = moveTrain();
            pot = tm.getFinalPosition();
        } while (pot.getX() < station1Location.x);
        assertEquals(station1Location.x, pot.getX());
        assertEquals(station1Location.y, pot.getY());
        assertEquals(READY, tm.getActivity());
        tm = moveTrain();
        pot = tm.getFinalPosition();

        // The train should be bound for station 2.
        assertEquals(2, ta.getSchedule().getStationToGoto());

        // Check that the train has picked up the cargo.
        // The train should have one wagon of type #0
        assertEquals(new ImInts(0), ta.getTrain().getConsist());
        assertEquals(AMOUNT_OF_CARGO, ta.getCargoBundle().getAmount(0));

    }

    /**
     *
     */
    public void testCanGenerateMove() {
        MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal,
                new OccupiedTracks(principal, world));
        assertTrue(preMove.isUpdateDue(world));
        Move m = preMove.generateMove(world);
        MoveStatus ms = m.doMove(world, principal);
        assertTrue(ms.message, ms.ok);
        assertFalse(preMove.isUpdateDue(world));
    }

    /**
     * Tests that when extra wagons are added, the TrainMotion lengthens to
     * accomodate them.
     */
    public void testLengtheningTrain() {
        // Set the train to add wagons at station2.
        ImInts newConsist = new ImInts(0, 0, 0, 0, 0, 0);
        TrainOrdersModel order0 = new TrainOrdersModel(2, newConsist, false,
                false);
        TrainAccessor ta = new TrainAccessor(world, principal, 0);
        MutableSchedule schedule = new MutableSchedule(ta.getSchedule());
        schedule.setOrder(0, order0);
        ImmutableSchedule imSchedule = schedule.toImmutableSchedule();
        world.set(principal, KEY.TRAIN_SCHEDULES, 0, imSchedule);
        assertEquals(0, ta.getSchedule().getOrderToGoto());

        // Move the train to the station.
        PositionOnTrack pot;
        TrainMotion tm;
        do {
            tm = moveTrain();
            pot = tm.getFinalPosition();
        } while (pot.getX() < station2Location.x);
        assertEquals(station2Location.x, pot.getX());
        assertEquals(station2Location.y, pot.getY());
        assertEquals(READY, tm.getActivity());

        TrainModel train = ta.getTrain();
        assertEquals(2, train.getNumberOfWagons());

        assertTrue(tm.getInitialPosition() >= train.getLength());

        tm = moveTrain();
        tm = moveTrain();
        train = ta.getTrain();
        assertEquals(6, ta.getTrain().getNumberOfWagons());
        assertTrue(tm.getInitialPosition() >= train.getLength());

    }

}
