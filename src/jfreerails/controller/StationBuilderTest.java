/*
 * Created on 24-Dec-2004
 *
 */
package jfreerails.controller;

import java.awt.Point;

import jfreerails.move.MoveStatus;
import jfreerails.server.MapFixtureFactory2;
import jfreerails.world.top.World;
import junit.framework.TestCase;
import jfreerails.world.common.OneTileMoveVector;
import static jfreerails.world.common.OneTileMoveVector.*;
/** A Junit test.
 * @author Luke Lindsay
 *
 
 */
public class StationBuilderTest extends TestCase {
	
	World w;
	TrackMoveProducer trackBuilder;
	StationBuilder stationBuilder;

	
	protected void setUp() throws Exception {
		super.setUp();
		w = MapFixtureFactory2.getCopy();
		MoveExecutor me = new SimpleMoveExecutor(w, 0);
		trackBuilder = new TrackMoveProducer(me, w);
		stationBuilder = new StationBuilder(me);
	}

	public void testCanBuiltStationHere() {
		
	}

	public void testBuildStation() {
		stationBuilder.setStationType(stationBuilder.getTrackTypeID("terminal"));
		OneTileMoveVector[] track = {EAST, EAST, EAST};
		MoveStatus ms = trackBuilder.buildTrack(new Point(10,10), track);
		assertTrue(ms.ok);
		assertTrue(stationBuilder.tryBuildingStation(new Point(10,10)).ok);
		assertTrue(stationBuilder.tryBuildingStation(new Point(13,10)).ok);
		MoveStatus ms1 = stationBuilder.buildStation(new Point(10,10));
		assertTrue(ms1.ok);
		
		MoveStatus ms2 = stationBuilder.buildStation(new Point(13,10));
		assertFalse(ms2.ok);
	}
	
	
	
	

}
