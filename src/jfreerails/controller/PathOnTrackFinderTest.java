/*
 * Created on 05-Jan-2005
 *
 */
package jfreerails.controller;

import java.awt.Point;
import java.util.Arrays;

import jfreerails.server.MapFixtureFactory2;
import jfreerails.world.common.OneTileMoveVector;
import static jfreerails.world.common.OneTileMoveVector.*;
import jfreerails.world.top.World;
import junit.framework.TestCase;

/**
 * @author Luke
 *
 */
public class PathOnTrackFinderTest extends TestCase {

	World w;

	TrackMoveProducer producer;

	PathOnTrackFinder pathFinder;
	
	StationBuilder stationBuilder;
	BuildTrackStrategy bts ;

	protected void setUp() throws Exception {
		super.setUp();
		w = MapFixtureFactory2.getCopy();
		MoveExecutor me = new SimpleMoveExecutor(w, 0);
		producer = new TrackMoveProducer(me, w);
		pathFinder = new PathOnTrackFinder(w);
		stationBuilder = new StationBuilder(me);
		bts = BuildTrackStrategy.getDefault(w);
	}

	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testPathAsVectors1() {
		OneTileMoveVector[] path = {EAST, EAST, SOUTH_EAST};
		Point start = new Point(5,5);
		Point end = OneTileMoveVector.move(start, path);
		producer.buildTrack(start, path);
		try {
			pathFinder.setupSearch(start, end, bts);
			pathFinder.search(-1);
			assertEquals(IncrementalPathFinder.PATH_FOUND, pathFinder.getStatus());
			OneTileMoveVector[] pathFound = pathFinder.pathAsVectors();
			assertTrue(Arrays.equals(path, pathFound));
		} catch (PathNotFoundException e) {
			fail();
		}
	}
	
	public void testPathAsVectors2() {
		OneTileMoveVector[] path = {EAST, EAST, SOUTH_EAST, EAST, EAST, NORTH_EAST};
		Point start = new Point(5,5);
		Point end = OneTileMoveVector.move(start, path);
		producer.buildTrack(start, path);
		try {
			pathFinder.setupSearch(start, end, bts);
			pathFinder.search(-1);
			assertEquals(IncrementalPathFinder.PATH_FOUND, pathFinder.getStatus());
			OneTileMoveVector[] pathFound = pathFinder.pathAsVectors();
			assertTrue(Arrays.equals(path, pathFound));
		} catch (PathNotFoundException e) {
			fail();
		}
	}

	public void testSetupSearch() {
		OneTileMoveVector[] path = {EAST, EAST, SOUTH_EAST};
		Point start = new Point(5,5);
		Point end = OneTileMoveVector.move(start, path);
		producer.buildTrack(start, path);
		try {
			pathFinder.setupSearch(start, end, bts);			
		} catch (PathNotFoundException e) {
			fail("Track at both of the points so no excepton should be thrown");
		}
		try {
			pathFinder.setupSearch(start, new Point(10, 10), bts);	
			fail("No track at one of the points so an excepton should be thrown");
		} catch (PathNotFoundException e) {
			
		}
		try {
			pathFinder.setupSearch(new Point(10, 10), end, bts);	
			fail("No track at one of the points so an excepton should be thrown");
		} catch (PathNotFoundException e) {
			
		}
	}

}
