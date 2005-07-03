/*
 * Created on 04-Mar-2005
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
import jfreerails.world.train.SpeedAgainstTime;
import jfreerails.world.train.TrainOrdersModel;

/**
 * JUnit test for MoveTrainPreMove.
 * 
 * @author Luke
 *
 */
public class MoveTrainPreMoveTest extends AbstractMoveTestCase {
	
	
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
		
		Point start = new Point(10, 10);
		AddTrainPreMove preMove = new AddTrainPreMove(0, new int[] { 0, 0 },
				start, principal, defaultSchedule);
		Move m = preMove.generateMove(world);
		MoveStatus ms = m.doMove(world, principal);
		assertTrue(ms.ok);				
	}
	
	public void testNextVector(){
		
		MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal);
		OneTileMoveVector actual = preMove.nextVector(world);
		assertNotNull(actual);
		//The train is at station A, so should head east to station B.
		assertEquals(EAST, actual);
	}
	
	public void testNextSpeeds(){
		MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal);
		GameTime t0 = new GameTime(0);
		SpeedAgainstTime speeds = preMove.nextSpeeds(world, EAST, t0);
		assertNotNull(speeds);
		assertEquals(t0, speeds.getStart());
		assertEquals(0, speeds.getDistance(t0));
		
		assertEquals(1, speeds.getSpeed(new GameTime(1)));
	}
	
//	public void testCanGenerateMove(){
//		MoveTrainPreMove preMove = new MoveTrainPreMove(0, principal);
//		assertTrue(preMove.canGenerateMove(world));
//	}
	
	public void testMove() {
		
		
		
	}

}
