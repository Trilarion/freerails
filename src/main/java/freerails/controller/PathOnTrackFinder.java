/*
 * Created on 05-Jan-2005
 *
 */
package freerails.controller;

import freerails.world.common.ImPoint;
import freerails.world.common.PositionOnTrack;
import freerails.world.common.Step;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.track.FreerailsTile;

import org.apache.log4j.Logger;

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

    private SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();

    private ImPoint startPoint;

    private final ReadOnlyWorld world;

    public PathOnTrackFinder(ReadOnlyWorld world) {
        this.world = world;
    }

    public void abandonSearch() {
        pathFinder.abandonSearch();
    }

    public int getStatus() {
        return pathFinder.getStatus();
    }

    public Step[] pathAsVectors() {
        int[] pathAsInts = pathFinder.retrievePath().toArray();
        Step[] vectors = new Step[pathAsInts.length];
        int x = startPoint.x;
        int y = startPoint.y;
        for (int i = 0; i < pathAsInts.length; i++) {
            PositionOnTrack p2 = new PositionOnTrack(pathAsInts[i]);
            vectors[i] = Step.getInstance(p2.getX() - x, p2.getY() - y);
            x = p2.getX();
            y = p2.getY();
        }
        return vectors;
    }

    public int[] pathAsInts() {
        return pathFinder.retrievePath().toArray();
    }

    public void search(long maxDuration) throws PathNotFoundException {
        pathFinder.search(maxDuration);
    }

    public void setupSearch(ImPoint from, ImPoint target)
            throws PathNotFoundException {
        startPoint = from;
        if (logger.isDebugEnabled()) {
            logger.debug("Find track path from " + from + " to " + target);
        }
        /* Check there is track at both the points. */
        FreerailsTile tileA = (FreerailsTile) world.getTile(from.x, from.y);
        FreerailsTile tileB = (FreerailsTile) world.getTile(target.x, target.y);
        if (!tileA.hasTrack()) {
            throw new PathNotFoundException("No track at " + from.x + ", "
                    + from.y + ".");
        }
        if (!tileB.hasTrack()) {
            throw new PathNotFoundException("No track at " + target.x + ", "
                    + target.y + ".");
        }

        PositionOnTrack[] startPoints = FlatTrackExplorer.getPossiblePositions(
                world, from);
        PositionOnTrack[] targetPoints = FlatTrackExplorer
                .getPossiblePositions(world, target);
        FlatTrackExplorer explorer;
        try {
            explorer = new FlatTrackExplorer(world, startPoints[0]);
        } catch (NoTrackException e) {
            throw new PathNotFoundException(e.getMessage(), e);
        }
        pathFinder.setupSearch(PositionOnTrack.toInts(startPoints),
                PositionOnTrack.toInts(targetPoints), explorer);
    }

}
