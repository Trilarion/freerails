/*
 * Created on 05-Jan-2005
 *
 */
package jfreerails.controller;

import java.awt.Point;
import java.util.logging.Logger;

import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackType;

/**
 * Finds a path along existing track. Used for upgrading or removing track
 * between two points on the track.
 * 
 * @author Luke
 * 
 */
public class PathOnTrackFinder implements IncrementalPathFinder {

	private static final Logger logger = Logger
			.getLogger(IncrementalPathFinder.class.getName());

	private SimpleAStarPathFinder m_pathFinder = new SimpleAStarPathFinder();

	private Point m_startPoint;

	private final ReadOnlyWorld m_world;

	public PathOnTrackFinder(ReadOnlyWorld world) {
		m_world = world;
	}

	public void abandonSearch() {
		m_pathFinder.abandonSearch();
	}

	public int getStatus() {
		return m_pathFinder.getStatus();
	}

	public OneTileMoveVector[] pathAsVectors() {
		int[] pathAsInts = m_pathFinder.retrievePath().toArray();
		OneTileMoveVector[] vectors = new  OneTileMoveVector[pathAsInts.length];
		int x = m_startPoint.x;
		int y = m_startPoint.y;
		for(int i = 0; i < pathAsInts.length; i++){
			PositionOnTrack p2 = new PositionOnTrack(pathAsInts[i]);
			vectors[i] = OneTileMoveVector.getInstance(p2.getX() - x, p2.getY()- y);
			x = p2.getX();
			y = p2.getY();
		}
		return vectors;
	}

	public void search(long maxDuration) throws PathNotFoundException {
		m_pathFinder.search(maxDuration);
	}

	public void setupSearch(Point startPoint, Point targetPoint,
			BuildTrackStrategy bts) throws PathNotFoundException {
		m_startPoint = new Point(startPoint);
		logger
				.fine("Find track path from " + startPoint + " to "
						+ targetPoint);
		/* Check there is track at both the points.*/
		FreerailsTile tileA = (FreerailsTile)m_world.getTile(startPoint.x, startPoint.y);
		FreerailsTile tileB = (FreerailsTile)m_world.getTile(targetPoint.x, targetPoint.y);
		if(tileA.getTrackTypeID() == NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER){
			throw new PathNotFoundException("No track at "+startPoint.x+", "+startPoint.y+".");
		}
		if(tileB.getTrackTypeID() == NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER){
			throw new PathNotFoundException("No track at "+targetPoint.x+", "+targetPoint.y+".");
		}
		
		PositionOnTrack[] startPoints = FlatTrackExplorer.getPossiblePositions(m_world, startPoint);
		PositionOnTrack[] targetPoints = FlatTrackExplorer.getPossiblePositions(m_world, targetPoint);
		FlatTrackExplorer explorer = new FlatTrackExplorer(m_world, startPoints[0]);
		m_pathFinder.setupSearch(PositionOnTrack.toInts(startPoints), PositionOnTrack.toInts(targetPoints), explorer);		
	}

}
