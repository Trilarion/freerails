/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Created on 05-Jan-2005
 *
 */
package freerails.controller;

import freerails.util.ImPoint;
import freerails.world.common.PositionOnTrack;
import freerails.world.common.Step;
import freerails.world.terrain.FreerailsTile;
import freerails.world.top.ReadOnlyWorld;
import org.apache.log4j.Logger;

/**
 * Finds a path along existing track. Used for upgrading or removing track
 * between two points on the track.
 */
public class PathOnTrackFinder implements IncrementalPathFinder {

    private static final Logger logger = Logger
            .getLogger(IncrementalPathFinder.class.getName());

    private final SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();
    private final ReadOnlyWorld world;
    private ImPoint startPoint;

    /**
     * @param world
     */
    public PathOnTrackFinder(ReadOnlyWorld world) {
        this.world = world;
    }

    /**
     *
     */
    public void abandonSearch() {
        pathFinder.abandonSearch();
    }

    /**
     * @return
     */
    public int getStatus() {
        return pathFinder.getStatus();
    }

    /**
     * @return
     */
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

    /**
     * @return
     */
    public int[] pathAsInts() {
        return pathFinder.retrievePath().toArray();
    }

    /**
     * @param maxDuration
     * @throws PathNotFoundException
     */
    public void search(long maxDuration) throws PathNotFoundException {
        pathFinder.search(maxDuration);
    }

    /**
     * @param from
     * @param target
     * @throws PathNotFoundException
     */
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
