/*
 * Created on 12-Aug-2005
 *
 */
package jfreerails.controller;

import static jfreerails.world.common.Step.EAST;
import static jfreerails.world.train.SpeedTimeAndStatus.Activity.READY;
import static jfreerails.world.train.SpeedTimeAndStatus.Activity.STOPPED_AT_STATION;
import static jfreerails.world.train.SpeedTimeAndStatus.Activity.WAITING_FOR_FULL_LOAD;
import jfreerails.client.common.ModelRootImpl;
import jfreerails.move.AbstractMoveTestCase;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.server.MapFixtureFactory2;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.cargo.MutableCargoBundle;
import jfreerails.world.common.ActivityIterator;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.ImInts;
import jfreerails.world.common.ImPoint;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.common.Step;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.Schedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainMotion;
import jfreerails.world.train.TrainOrdersModel;
/** Unit test for MoveTrainPreMove, tests stopping at stations.*/
public class MoveTrainPreMove2ndTest extends AbstractMoveTestCase {

	TrackMoveProducer trackBuilder;

	StationBuilder stationBuilder;

	FreerailsPrincipal principal;

	private ImPoint station0;

	private ImPoint station1;

	private ImPoint station2;

	ImmutableSchedule defaultSchedule;

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
		station0 = new ImPoint(10, 10);
		MoveStatus ms0 = trackBuilder.buildTrack(station0, track);
		assertTrue(ms0.ok);

		// Build 2 stations.
		MoveStatus ms1 = stationBuilder.buildStation(station0);
		assertTrue(ms1.ok);
		station1 = new ImPoint(20, 10);
		MoveStatus ms2 = stationBuilder.buildStation(station1);
		assertTrue(ms2.ok);
		station2 = new ImPoint(28, 10);
		MoveStatus ms3 = stationBuilder.buildStation(station2);
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
		MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal);
		Step step = preMove.nextStep(world);
		return step;
	}

	/** Test that when the train arrives at a non station tile it keeps moving. */
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
		MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal);
		Move m = preMove.generateMove(world);
		MoveStatus ms = m.doMove(world, principal);
		assertTrue(ms.message, ms.ok);
		TrainAccessor ta = new TrainAccessor(world, principal, 0);
		TrainMotion tm = ta.findCurrentMotion(Integer.MAX_VALUE);
		return tm;
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
		} while (pot.getX() < station1.x);
		assertEquals(station1.x, pot.getX());
		assertEquals(station1.y, pot.getY());
		assertEquals(READY, tm.getActivity());

		// The next train motion should represent the stop at the station.
		tm = moveTrain();
		pot = tm.getFinalPosition();
		assertEquals(station1.x, pot.getX());
		assertEquals(station1.y, pot.getY());
		assertEquals(STOPPED_AT_STATION, tm.getActivity());

		// 80 Units of cargo should have been transfered to the train!
		CargoBundle onTrain = ta.getCargoBundle();
		int amount = onTrain.getAmount(0);
		assertEquals(80, amount);

		// Then the train should continue.
		tm = moveTrain();
		pot = tm.getFinalPosition();
		assertEquals(station1.x + 1, pot.getX());
		assertEquals(station1.y, pot.getY());
		assertEquals(READY, tm.getActivity());

	}

	/** Adds the specified amount of cargo #0 to the specified station. */
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
		} while (x < station2.x);
		assertEquals(station2.x, x);
		assertEquals(station2.y, pot.getY());
		assertEquals(READY, tm.getActivity());

		// The train should be heading for station 1.
		TrainAccessor ta = new TrainAccessor(world, principal, 0);
		Schedule schedule1 = ta.getSchedule();
		assertEquals(0, schedule1.getOrderToGoto());
		assertEquals(2, schedule1.getStationToGoto());
		ImPoint expectedTarget = new ImPoint(station2.x, station2.y);
		assertEquals(expectedTarget, ta.getTarget());

		// The next train motion should represent the stop at the station.
		tm = moveTrain();
		pot = tm.getFinalPosition();
		assertEquals(station2.x, pot.getX());
		assertEquals(station2.y, pot.getY());
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
		assertEquals(station2.x - 1, pot.getX());
		assertEquals(station2.y, pot.getY());
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
		
		//Add enough cargo to fill up the train.
		addCargoAtStation(2, 70);
		
		tm = moveTrain();
		pot = tm.getFinalPosition();

		assertEquals(station2.x -1, pot.getX());
		assertEquals(station2.y, pot.getY());
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
		} while (pot.getX() < station2.x);
		assertEquals(station2.x, pot.getX());
		assertEquals(station2.y, pot.getY());
		assertEquals(READY, tm.getActivity());
		
		//The train should now stop at the station
		//and wait for a full load.
		for(int i = 0; i < 4; i++){
			tm = moveTrain();
			pot = tm.getFinalPosition();
	
			assertEquals(String.valueOf(i), station2.x, pot.getX());
			assertEquals(station2.y, pot.getY());
			assertEquals(WAITING_FOR_FULL_LOAD, tm.getActivity());
		}
	}

	/** Test that a waiting train whose orders change behaves correctly. */
	public void testStops6() {
		PositionOnTrack pot;
		TrainMotion tm;
		putTrainAtStationWaiting4FullLoad();
		
		//Now change the train's orders.
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
		
		//Then the train should continue.
		tm = moveTrain();
		pot = tm.getFinalPosition();
		assertEquals(station2.x - 1, pot.getX());
		assertEquals(station2.y, pot.getY());
		assertEquals(READY, tm.getActivity());

	}
	
	public void testCanGenerateMove(){
		MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal);
		assertTrue(preMove.canGenerateMove(world));
		Move m = preMove.generateMove(world);
		MoveStatus ms = m.doMove(world, principal);
		assertTrue(ms.message, ms.ok);
		assertFalse(preMove.canGenerateMove(world));
	}	
	
	static void incrTime(World w, FreerailsPrincipal p){
		ActivityIterator ai = w.getActivities(p, 0);
		while (ai.hasNext())
			ai.nextActivity();
				
		double finishTime = ai.getFinishTime();
		GameTime newTime = new GameTime((int) Math.floor(finishTime));
		w.setTime(newTime);
	}
	
	/** Tests that we extra wagons are added, the TrainMotion lengthens to
	 * accomodate them.
	 */
	public void testLengtheningTrain(){
		//Set the train to add wagons at station2.
		ImInts newConsist = new ImInts(0, 0, 0, 0, 0, 0);
		TrainOrdersModel order0 = new TrainOrdersModel(2, newConsist, false,
				false);
		TrainAccessor ta = new TrainAccessor(world, principal, 0);
		MutableSchedule schedule = new MutableSchedule(ta.getSchedule());
		schedule.setOrder(0, order0);
		ImmutableSchedule imSchedule = schedule.toImmutableSchedule();
		world.set(principal, KEY.TRAIN_SCHEDULES, 0, imSchedule);
		assertEquals(0, ta.getSchedule().getOrderToGoto());
		
		//Move the train to the station.
		PositionOnTrack pot;
		TrainMotion tm;
		do {
			tm = moveTrain();
			pot = tm.getFinalPosition();
		} while (pot.getX() < station2.x);
		assertEquals(station2.x, pot.getX());
		assertEquals(station2.y, pot.getY());
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
