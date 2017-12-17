/*
 * Created on Sep 4, 2004
 *
 */
package freerails.controller;

import freerails.util.IntArray;
import freerails.world.common.ImPoint;
import freerails.world.common.PositionOnTrack;
import freerails.world.common.Step;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;
import freerails.world.track.FreerailsTile;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackPiece;
import freerails.world.track.TrackRule;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds the best route to build track between two points.
 *
 * @author Luke
 */
public class TrackPathFinder implements IncrementalPathFinder {
    private static final Logger logger = Logger.getLogger(TrackPathFinder.class
            .getName());

    private final SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();

    private final ReadOnlyWorld world;

    private ImPoint startPoint;

    private final FreerailsPrincipal principal;

    public TrackPathFinder(ReadOnlyWorld world, FreerailsPrincipal principal) {
        this.world = world;
        this.principal = principal;
    }

    public void abandonSearch() {
        pathFinder.abandonSearch();
    }

    private List<ImPoint> convertPath2Points(IntArray path) {
        PositionOnTrack progress = new PositionOnTrack();
        List<ImPoint> proposedTrack = new ArrayList<>();

        ImPoint p;
        for (int i = 0; i < path.size(); i++) {
            progress.setValuesFromInt(path.get(i));
            p = new ImPoint(progress.getX(), progress.getY());
            proposedTrack.add(p);
            if (logger.isDebugEnabled()) {
                logger.debug("Adding point " + p);
            }
        }

        return proposedTrack;
    }

    private int[] findTargets(ImPoint targetPoint) {
        FreerailsTile tile = (FreerailsTile) world.getTile(targetPoint.x,
                targetPoint.y);
        TrackPiece trackPiece = tile.getTrackPiece();
        int ruleNumber = trackPiece.getTrackTypeID();

        int[] targetInts;

        if (tile.hasTrack()) {
            /*
             * If there is already track here, we need to check what directions
             * we can build in without creating an illegel track config.
             */
            TrackRule trackRule = (TrackRule) world.get(SKEY.TRACK_RULES,
                    ruleNumber);

            /* Count number of possible directions. */
            ArrayList<Step> possibleDirections = new ArrayList<>();

            for (int i = 0; i < 8; i++) {
                Step direction = Step.getInstance(i);
                TrackConfiguration config = trackPiece.getTrackConfiguration();
                TrackConfiguration testConfig = TrackConfiguration.add(config,
                        direction);

                if (trackRule.trackPieceIsLegal(testConfig)) {
                    possibleDirections.add(direction);
                }
            }

            /* Put them into an array. */
            targetInts = new int[possibleDirections.size()];

            for (int i = 0; i < targetInts.length; i++) {
                Step direction = possibleDirections.get(i);
                PositionOnTrack targetPot = PositionOnTrack.createFacing(
                        targetPoint.x, targetPoint.y, direction);
                targetInts[i] = targetPot.toInt();
            }
        } else {
            /* If there is no track here, we can go in any direction. */
            targetInts = new int[8];

            for (int i = 0; i < 8; i++) {
                PositionOnTrack targetPot = PositionOnTrack.createComingFrom(
                        targetPoint.x, targetPoint.y, Step.getInstance(i));
                targetInts[i] = targetPot.toInt();
            }
        }

        return targetInts;
    }

    public List generatePath(ImPoint start, ImPoint targetPoint,
                             BuildTrackStrategy bts) throws PathNotFoundException {
        setupSearch(start, targetPoint, bts);
        pathFinder.search(-1);

        IntArray path = pathFinder.retrievePath();

        return convertPath2Points(path);
    }

    public int getStatus() {
        return pathFinder.getStatus();
    }

    public List<ImPoint> pathAsPoints() {
        IntArray path = pathFinder.retrievePath();

        return convertPath2Points(path);
    }

    public Step[] pathAsVectors() {
        IntArray path = pathFinder.retrievePath();
        int size = path.size();
        Step[] vectors = new Step[size];
        PositionOnTrack progress = new PositionOnTrack();

        int x = startPoint.x;
        int y = startPoint.y;
        for (int i = 0; i < size; i++) {
            progress.setValuesFromInt(path.get(i));
            int x2 = progress.getX();
            int y2 = progress.getY();
            vectors[i] = Step.getInstance(x2 - x, y2 - y);
            x = x2;
            y = y2;
        }
        return vectors;

    }

    public void search(long maxDuration) throws PathNotFoundException {
        pathFinder.search(maxDuration);
    }

    public void setupSearch(ImPoint startPoint, ImPoint targetPoint,
                            BuildTrackStrategy bts) throws PathNotFoundException {
        logger.debug("Find track path from " + startPoint + " to "
                + targetPoint);

        this.startPoint = startPoint;
        int[] targetInts = findTargets(targetPoint);
        int[] startInts = findTargets(startPoint);

        BuildTrackExplorer explorer = new BuildTrackExplorer(world, principal,
                startPoint, targetPoint);
        explorer.setBuildTrackStrategy(bts);

        pathFinder.setupSearch(startInts, targetInts, explorer);
    }
}