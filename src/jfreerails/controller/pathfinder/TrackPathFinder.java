/*
 * Created on Sep 4, 2004
 *
 */
package jfreerails.controller.pathfinder;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import jfreerails.util.IntArray;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.PositionOnTrack;
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
    private final ReadOnlyWorld m_world;
    private SimpleAStarPathFinder m_pathFinder = new SimpleAStarPathFinder();

    public TrackPathFinder(ReadOnlyWorld world) {
        m_world = world;
    }

    public int getStatus() {
        return m_pathFinder.getStatus();
    }

    public List generatePath(Point startPoint, Point targetPoint)
        throws PathNotFoundException {
        setupSearch(startPoint, targetPoint);
        m_pathFinder.search(-1);

        IntArray path = m_pathFinder.retrievePath();

        List proposedTrack = convertPath2Points(path);

        return proposedTrack;
    }

    public void search(long maxDuration) throws PathNotFoundException {
        m_pathFinder.search(maxDuration);
    }

    public List retrievePath() {
        IntArray path = m_pathFinder.retrievePath();

        return convertPath2Points(path);
    }

    private List convertPath2Points(IntArray path) {
        int loopCount = 0;

        PositionOnTrack progress = new PositionOnTrack();
        List proposedTrack = new ArrayList();

        Point p;
        Point lastp;

        for (int i = 0; i < path.size(); i++) {
            progress.setValuesFromInt(path.get(i));
            p = new Point(progress.getX(), progress.getY());
            proposedTrack.add(p);
            logger.fine("Adding point " + p);
        }

        return proposedTrack;
    }

    public void setupSearch(Point startPoint, Point targetPoint)
        throws PathNotFoundException {
        logger.fine("Find track path from " + startPoint + " to " +
            targetPoint);

        PositionOnTrack[] pots = FlatTrackExplorer.getPossiblePositions(m_world,
                startPoint);

        int[] targetInts = findTargets(targetPoint);
        int[] startInts = findTargets(startPoint);

        BuildTrackExplorer explorer = new BuildTrackExplorer(m_world,
                startPoint, targetPoint);

        m_pathFinder.setupSearch(startInts, targetInts, explorer);
    }

    private int[] findTargets(Point targetPoint) {
        FreerailsTile tile = (FreerailsTile)m_world.getTile(targetPoint.x,
                targetPoint.y);
        int ruleNumber = tile.getTrackRule().getRuleNumber();

        int[] targetInts;

        if (NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER != ruleNumber) {
            /*
             * If there is already track here, we need to check what directions
             * we can build in without creating an illegel track config.
             */
            TrackRule trackRule = (TrackRule)m_world.get(SKEY.TRACK_RULES,
                    ruleNumber);

            /* Count number of possible directions. */
            ArrayList possibleDirections = new ArrayList();

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
                OneTileMoveVector direction = (OneTileMoveVector)possibleDirections.get(i);
                PositionOnTrack targetPot = new PositionOnTrack(targetPoint.x,
                        targetPoint.y, direction);
                targetInts[i] = targetPot.toInt();
            }
        } else {
            /* If there is no track here, we can go in any direction. */
            targetInts = new int[8];

            for (int i = 0; i < 8; i++) {
                PositionOnTrack targetPot = new PositionOnTrack(targetPoint.x,
                        targetPoint.y, OneTileMoveVector.getInstance(i));
                targetInts[i] = targetPot.toInt();
            }
        }

        return targetInts;
    }

    public void abandonSearch() {
        m_pathFinder.abandonSearch();
    }
}