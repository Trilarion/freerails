package jfreerails.controller;

import static jfreerails.world.common.Step.EAST;
import static jfreerails.world.common.Step.SOUTH;
import static jfreerails.world.common.Step.SOUTH_EAST;

import java.util.Arrays;

import jfreerails.client.common.ModelRootImpl;
import jfreerails.move.MoveStatus;
import jfreerails.server.MapFixtureFactory2;
import jfreerails.world.common.ImPoint;
import jfreerails.world.common.Step;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackPiece;
import junit.framework.TestCase;

public class TrackBuildingTest extends TestCase {

	World w;

	TrackMoveProducer producer;

	TrackPathFinder pathFinder;

	StationBuilder stationBuilder;

	BuildTrackStrategy bts;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		w = MapFixtureFactory2.getCopy();
		MoveExecutor me = new SimpleMoveExecutor(w, 0);
		ModelRoot mr = new ModelRootImpl();
		producer = new TrackMoveProducer(me, w, mr);
		FreerailsPrincipal principle = w.getPlayer(0).getPrincipal();
		pathFinder = new TrackPathFinder(w, principle);
		stationBuilder = new StationBuilder(me);
		bts = BuildTrackStrategy.getDefault(w);
	}

	/** Tests building track from 5,5 to 10,5 */
	public void testBuildingStraight() {

		ImPoint from = new ImPoint(5, 5);
		ImPoint to = new ImPoint(10, 5);
		try {
			// Check there is no track before we build it.
			for (int x = 5; x <= 10; x++) {
				TrackPiece tp = ((FreerailsTile) w.getTile(x, 5)).getTrackPiece();
				assertEquals(NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER, tp
						.getTrackTypeID());
			}
			pathFinder.setupSearch(from, to, bts);
			pathFinder.search(-1);
			assertEquals(pathFinder.getStatus(),
					IncrementalPathFinder.PATH_FOUND);
			Step[] path = pathFinder.pathAsVectors();
			assertEquals(path.length, 5);
			for (int i = 0; i < 5; i++) {
				assertEquals(Step.EAST, path[i]);
			}
			MoveStatus ms = producer.buildTrack(from, path);
			assertTrue(ms.message, ms.ok);
			// Check track has been built.
			for (int x = 5; x <= 10; x++) {
				TrackPiece tp = ((FreerailsTile) w.getTile(x, 5)).getTrackPiece();
				assertEquals(0, tp.getTrackTypeID());
			}
		} catch (PathNotFoundException e) {
			fail();
		}

	}

	/** Tests building track from 5,5 to 6,5 */
	public void testBuildingOneTrackPiece() {

		ImPoint from = new ImPoint(5, 5);
		ImPoint to = new ImPoint(6, 5);
		try {
			// Check there is no track before we build it.

			TrackPiece tp1 = ((FreerailsTile) w.getTile(5, 5)).getTrackPiece();
			assertEquals(NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER, tp1
					.getTrackTypeID());

			TrackPiece tp2 = ((FreerailsTile) w.getTile(6, 5)).getTrackPiece();
			assertEquals(NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER, tp2
					.getTrackTypeID());

			pathFinder.setupSearch(from, to, bts);
			pathFinder.search(-1);
			assertEquals(pathFinder.getStatus(),
					IncrementalPathFinder.PATH_FOUND);
			Step[] path = pathFinder.pathAsVectors();
			assertEquals(path.length, 1);

			assertEquals(Step.EAST, path[0]);

			MoveStatus ms = producer.buildTrack(from, path);
			assertTrue(ms.message, ms.ok);
			// Check track has been built.
			tp1 = ((FreerailsTile) w.getTile(5, 5)).getTrackPiece();
			assertEquals(0, tp1.getTrackTypeID());

			tp2 = ((FreerailsTile) w.getTile(6, 5)).getTrackPiece();
			assertEquals(0, tp2.getTrackTypeID());
		} catch (PathNotFoundException e) {
			fail();
		}

	}

	/**
	 * There is a bug where if a section of track has a terminal on the end, you
	 * cannot extend the track through the terminal. Instead, the track path
	 * finder finds a route that misses out the terminal.
	 * 
	 */
	public void testTerminalProblem() {
		try {
			ImPoint from = new ImPoint(5, 5);
			Step[] path = { EAST, EAST, EAST };
			MoveStatus ms = producer.buildTrack(from, path);
			assertTrue(ms.ok);
			int terminalStationType = stationBuilder.getTrackTypeID("terminal");
			stationBuilder.setStationType(terminalStationType);
			ms = stationBuilder.buildStation(new ImPoint(8, 5));
			assertTrue(ms.ok);
			pathFinder.setupSearch(new ImPoint(7, 5), new ImPoint(9, 5), bts);
			pathFinder.search(-1);
			path = pathFinder.pathAsVectors();
			assertEquals(2, path.length);
			Step[] expectedPath = { EAST, EAST };
			assertTrue(Arrays.equals(expectedPath, path));
		} catch (PathNotFoundException e) {
			fail();
		}
	}

	/**
	 * There is a bug where if you build a straight section of double track
	 * going E, then move the curor to the end and attempt to build more double
	 * track going SE, the track path finder builds a loop rather than just
	 * building track going SE
	 * 
	 */
	public void testDoubleTrackProblem() {
		try {

			int trackTypeID = stationBuilder.getTrackTypeID("double track");
			bts = BuildTrackStrategy.getSingleRuleInstance(trackTypeID, w);
			producer.setBuildTrackStrategy(bts);
			ImPoint a = new ImPoint(5, 5);
			ImPoint b = new ImPoint(6, 5);
			ImPoint c = new ImPoint(7, 6);

			pathFinder.setupSearch(a, b, bts);
			pathFinder.search(-1);
			Step[] path = pathFinder.pathAsVectors();
			Step[] expectedPath = { EAST };
			assertTrue(Arrays.equals(expectedPath, path));
			MoveStatus ms = producer.buildTrack(a, path);
			assertTrue(ms.ok);

			TrackPiece tp = ((FreerailsTile) w.getTile(b.x, b.y)).getTrackPiece();
			assertEquals("We just build double track here.", trackTypeID, tp
					.getTrackTypeID());

			pathFinder.setupSearch(b, c, bts);
			pathFinder.search(-1);
			path = pathFinder.pathAsVectors();
			assertEquals(1, path.length);

			expectedPath = new Step[] { SOUTH_EAST };
			assertTrue(Arrays.equals(expectedPath, path));
		} catch (PathNotFoundException e) {
			fail();
		}
	}

	/**
	 * There is a bug where if you try to start building track on a 90 degree
	 * bend, no track path is found even when one should exist.
	 * 
	 */
	public void testStartSearchOnSharpCurve() {
		try {
			ImPoint from = new ImPoint(5, 5);
			Step[] path = { EAST, SOUTH };
			MoveStatus ms = producer.buildTrack(from, path);
			assertTrue(ms.ok);
			pathFinder.setupSearch(new ImPoint(6, 5), new ImPoint(6, 7), bts);
			pathFinder.search(-1);
			path = pathFinder.pathAsVectors();
			assertEquals(2, path.length);
			assertEquals(SOUTH, path[0]);
			assertEquals(SOUTH, path[1]);
		} catch (PathNotFoundException e) {
			fail();
		}
	}

}
