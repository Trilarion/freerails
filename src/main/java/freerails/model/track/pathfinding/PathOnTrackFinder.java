/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
 *
 */
package freerails.model.track.pathfinding;

import freerails.model.track.explorer.FlatTrackExplorer;
import freerails.model.track.NoTrackException;
import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.train.PositionOnTrack;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Finds a path along existing track. Used for upgrading or removing track
 * between two points on the track.
 */
public class PathOnTrackFinder implements IncrementalPathFinder {

    private static final Logger logger = Logger.getLogger(PathOnTrackFinder.class.getName());

    private final SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();
    private final UnmodifiableWorld world;
    private Vec2D startPoint;

    /**
     * Creates a finder over the supplied world's existing track.
     *
     * @param world world to search
     */
    public PathOnTrackFinder(UnmodifiableWorld world) {
        this.world = world;
    }

    /**
     * Encodes a set of positions on track as integer vertex IDs.
     *
     * @param pos positions to encode
     * @return encoded positions in the same order
     */
    private static int[] toInts(PositionOnTrack[] pos) {
        int[] returnValue = new int[pos.length];
        for (int i = 0; i < pos.length; i++) {
            returnValue[i] = pos[i].toInt();
        }
        return returnValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abandonSearch() {
        pathFinder.abandonSearch();
    }

    /**
     * {@inheritDoc}
     *
     * @return status of the underlying A* search
     */
    @Override
    public PathFinderStatus getStatus() {
        return pathFinder.getStatus();
    }

    /**
     * Returns the completed path as tile-to-tile transitions, including the
     * initial movement from the start tile.
     *
     * @return path transitions in traversal order
     */
    public TileTransition[] pathAsVectors() {
        List<Integer> path = pathFinder.retrievePath();
        TileTransition[] vectors = new TileTransition[path.size()];
        Vec2D p = startPoint;
        for (int i = 0; i < path.size(); i++) {
            PositionOnTrack p2 = new PositionOnTrack(path.get(i));
            vectors[i] = TileTransition.getInstance(Vec2D.subtract(p2.getLocation(), p));
            p = p2.getLocation();
        }
        return vectors;
    }

    /**
     * Returns the completed path as encoded positions on track.
     *
     * @return encoded path positions in traversal order
     */
    public List<Integer> pathAsInts() {
        return pathFinder.retrievePath();
    }

    /**
     * {@inheritDoc}
     *
     * @param maxDuration maximum duration of this search call in milliseconds,
     *                    or a non-positive value for no time limit
     * @throws PathNotFoundException if the search has no path or a limited
     *                               search call has paused
     */
    @Override
    public void search(long maxDuration) throws PathNotFoundException {
        pathFinder.search(maxDuration);
    }

    /**
     * Configures a search between two tiles, requiring track at both endpoints.
     *
     * @param from starting tile coordinate
     * @param target destination tile coordinate
     * @throws PathNotFoundException if either endpoint has no track or no route exists
     */
    public void setupSearch(Vec2D from, Vec2D target) throws PathNotFoundException {
        startPoint = from;
        logger.debug("Find track path from " + from + " to " + target);

        // Check there is track at both the points.
        TerrainTile tileA = world.getTile(from);
        TerrainTile tileB = world.getTile(target);
        if (!tileA.hasTrack()) {
            throw new PathNotFoundException("No track at " + from.x + ", " + from.y + '.');
        }
        if (!tileB.hasTrack()) {
            throw new PathNotFoundException("No track at " + target.x + ", " + target.y + '.');
        }

        PositionOnTrack[] startPoints = FlatTrackExplorer.getPossiblePositions(world, from);
        PositionOnTrack[] targetPoints = FlatTrackExplorer.getPossiblePositions(world, target);
        FlatTrackExplorer explorer;
        try {
            explorer = new FlatTrackExplorer(world, startPoints[0]);
        } catch (NoTrackException e) {
            throw new PathNotFoundException(e);
        }
        pathFinder.setupSearch(toInts(startPoints), toInts(targetPoints), explorer);
    }

}
