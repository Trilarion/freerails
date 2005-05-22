/*
 * Created on 18-Feb-2005
 *
 */
package jfreerails.controller;

import static jfreerails.world.common.OneTileMoveVector.EAST;

import java.awt.Point;

import jfreerails.move.AbstractMoveTestCase;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.server.MapFixtureFactory2;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.PathOnTiles;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainMotion;
import jfreerails.world.train.TrainOrdersModel;
import jfreerails.world.train.TrainPositionOnMap;

/**
 * Junit test for AddTrainPreMove.
 * 
 * @author Luke
 * 
 */
public class AddTrainPreMoveTest extends AbstractMoveTestCase {
	
	TrackMoveProducer trackBuilder;

	StationBuilder stationBuilder;

	FreerailsPrincipal principal;

	private Point stationA;

	private Point stationB;
	
	ImmutableSchedule defaultSchedule;

	protected void setupWorld() {		
		world = MapFixtureFactory2.getCopy();
		MoveExecutor me = new SimpleMoveExecutor(world, 0);
		principal = me.getPrincipal();
		trackBuilder = new TrackMoveProducer(me, world);
		stationBuilder = new StationBuilder(me);

		// Build track.
		stationBuilder
				.setStationType(stationBuilder.getTrackTypeID("terminal"));
		OneTileMoveVector[] track = { EAST, EAST, EAST, EAST, EAST, EAST, EAST,
				EAST, EAST };
		stationA = new Point(10, 10);
		MoveStatus ms0 = trackBuilder.buildTrack(stationA, track);
		assertTrue(ms0.ok);

		// Build 2 stations.
		MoveStatus ms1 = stationBuilder.buildStation(stationA);
		assertTrue(ms1.ok);
		stationB = new Point(19, 10);
		MoveStatus ms2 = stationBuilder.buildStation(stationB);
		assertTrue(ms2.ok);

		TrainOrdersModel order0 = new TrainOrdersModel(0, null, false, false);
		TrainOrdersModel order1 = new TrainOrdersModel(1, null, false, false);
		MutableSchedule s = new MutableSchedule();
		s.addOrder(order0);
		s.addOrder(order1);				
		defaultSchedule = s.toImmutableSchedule();
		
	}

	public void testInitPosition() {

		//Test step 1
		
		TrainModel train = new TrainModel(0, new int[] { 0, 0 }, 0);
		assertEquals(24 * 3, train.getLength());
		Point start = new Point(10, 10);
		AddTrainPreMove preMove = new AddTrainPreMove(0, new int[] { 0, 0 },
				start, principal, defaultSchedule);
		PathOnTiles path = preMove.initPositionStep1(world);
		assertEquals(start, path.getStart());
		assertEquals(3, path.steps());
		assertEquals(EAST, path.getStep(0));
		assertEquals(EAST, path.getStep(1));
		assertEquals(EAST, path.getStep(2));

		//Test step 2
		TrainMotion tm = preMove.initPositionStep2(path);
		assertEquals(GameTime.BIG_BANG, tm.getStart());
		assertEquals(GameTime.END_OF_THE_WORLD, tm.getEnd());
		
		
		GameTime t = GameTime.BIG_BANG;		
		PathOnTiles actual = tm.getTiles(t);
		assertEquals(new Point(10,10), actual.getStart());
		assertEquals(3, actual.steps());
		assertEquals(EAST, actual.getStep(0));
		assertEquals(EAST, actual.getStep(1));
		assertEquals(EAST, actual.getStep(2));
		
		
		//Check distance
		assertEquals(0, tm.getDistance(new GameTime(0)));
		assertEquals(0, tm.getDistance(new GameTime(100)));
		assertEquals(0, tm.getDistance(GameTime.BIG_BANG));
		assertEquals(0, tm.getDistance(GameTime.END_OF_THE_WORLD));
		
		//Check speed
		assertEquals(0, tm.getSpeed(new GameTime(0)));
		assertEquals(0, tm.getSpeed(new GameTime(100)));
		assertEquals(0, tm.getSpeed(GameTime.BIG_BANG));
		assertEquals(0, tm.getSpeed(GameTime.END_OF_THE_WORLD));
		
		//Check train position.
		TrainPositionOnMap pos = tm.getPosition(new GameTime(0));
		assertNotNull(pos);
		
	}

	/* (non-Javadoc)
	 * @see jfreerails.move.AbstractMoveTestCase#testMove()
	 */
	public void testMove() {
		AddTrainPreMove preMove = new AddTrainPreMove(0, new int[]{0,0}, stationA, principal, defaultSchedule);
		Move m = preMove.generateMove(world);
		assertDoMoveIsOk(m);
		
		assertUndoMoveIsOk(m);
		
		assertEqualsSurvivesSerialisation(m);
	}
		

}
