/*
 * Created on Sep 4, 2004
 *
 */
package jfreerails.controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jfreerails.util.IntArray;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackRule;


/**
 * Finds the best route to build track between two points.
 *
 * @author Luke
 *
 */
public class TrackPathFinder implements IncrementalPathFinder {
    private static final Logger logger = Logger.getLogger(TrackPathFinder.class.getName());
    private SimpleAStarPathFinder m_pathFinder = new SimpleAStarPathFinder();
    private final ReadOnlyWorld m_world;
    private Point m_startPoint;
    private final FreerailsPrincipal m_principal;

    public TrackPathFinder(ReadOnlyWorld world, FreerailsPrincipal principal) {
        m_world = world;
        m_principal = principal;
    }

    public void abandonSearch() {
        m_pathFinder.abandonSearch();
    }

    private List<Point> convertPath2Points(IntArray path) {
        PositionOnTrack progress = new PositionOnTrack();
        List<Point> proposedTrack = new ArrayList<Point>();

        Point p;
        for (int i = 0; i < path.size(); i++) {
            progress.setValuesFromInt(path.get(i));
            p = new Point(progress.getX(), progress.getY());
            proposedTrack.add(p);
            logger.fine("Adding point " + p);
        }

        return proposedTrack;
    }

    private int[] findTargets(Point targetPoint) {
        FreerailsTile tile = (FreerailsTile)m_world.getTile(targetPoint.x,
                targetPoint.y);
        int ruleNumber = tile.getTrackTypeID();

        int[] targetInts;

        if (NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER != ruleNumber) {
            /*
             * If there is already track here, we need to check what directions
             * we can build in without creating an illegel track config.
             */
            TrackRule trackRule = (TrackRule)m_world.get(SKEY.TRACK_RULES,
                    ruleNumber);

            /* Count number of possible directions. */
            ArrayList<OneTileMoveVector> possibleDirections = new ArrayList<OneTileMoveVector>();

            for (int i = 0; i < 8; i++) {
                OneTileMoveVector direction = OneTileMoveVector.getInstance(i);
                TrackConfiguration config = tile.getTrackConfiguration();
                TrackConfiguration testConfig = TrackConfiguration.add(config,
                        direction);

                if (trackRule.trackPieceIsLegal(testConfig)) {
                    possibleDirections.add(direction);
                }
            }

            /* Put them into an array.*/
            targetInts = new int[possibleDirections.size()];

            for (int i = 0; i < targetInts.length; i++) {
                OneTileMoveVector direction = possibleDirections.get(i);               
                PositionOnTrack targetPot = PositionOnTrack.createFacing(targetPoint.x, targetPoint.y, direction);
                targetInts[i] = targetPot.toInt();
            }
        } else {
            /* If there is no track here, we can go in any direction. */
            targetInts = new int[8];

            for (int i = 0; i < 8; i++) {
                PositionOnTrack targetPot = PositionOnTrack.createComingFrom(targetPoint.x, targetPoint.y, OneTileMoveVector.getInstance(i));
                targetInts[i] = targetPot.toInt();
            }
        }

        return targetInts;
    }

    public List generatePath(Point startPoint, Point targetPoint,
        BuildTrackStrategy bts ) throws PathNotFoundException {
        setupSearch(startPoint, targetPoint, bts);
        m_pathFinder.search(-1);

        IntArray path = m_pathFinder.retrievePath();

        List proposedTrack = convertPath2Points(path);

        return proposedTrack;
    }

    public int getStatus() {
        return m_pathFinder.getStatus();
    }

    public List<Point> pathAsPoints() {
        IntArray path = m_pathFinder.retrievePath();

        return convertPath2Points(path);
    }
    
    public OneTileMoveVector[] pathAsVectors() {
        IntArray path = m_pathFinder.retrievePath();
        int size = path.size();
		OneTileMoveVector[] vectors = new  OneTileMoveVector[size];
        PositionOnTrack progress = new PositionOnTrack();
       
        int x = m_startPoint.x;
        int y = m_startPoint.y;
        for (int i = 0; i < size; i++) {
            progress.setValuesFromInt(path.get(i));
            int x2 = progress.getX();
			int y2 = progress.getY();
			vectors[i] = OneTileMoveVector.getInstance(x2 - x, y2 -y);
            x = x2;
            y = y2;
        }
        return vectors;

    }

    public void search(long maxDuration) throws PathNotFoundException {
        m_pathFinder.search(maxDuration);
    }

    public void setupSearch(Point startPoint, Point targetPoint,
        BuildTrackStrategy bts) throws PathNotFoundException {
        logger.fine("Find track path from " + startPoint + " to " +
            targetPoint);
        
        m_startPoint = startPoint;
        int[] targetInts = findTargets(targetPoint);
        int[] startInts = findTargets(startPoint);

        BuildTrackExplorer explorer = new BuildTrackExplorer(m_world, m_principal, 
                startPoint, targetPoint);
        explorer.setBuildTrackStrategy(bts);
       

        m_pathFinder.setupSearch(startInts, targetInts, explorer);
    }
}