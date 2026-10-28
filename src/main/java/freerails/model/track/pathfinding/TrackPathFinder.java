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

import freerails.model.track.*;
import freerails.model.track.explorer.BuildTrackExplorer;
import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.train.PositionOnTrack;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds the best route to build track between two points.
 */
public class TrackPathFinder implements IncrementalPathFinder {

    private static final Logger logger = Logger.getLogger(TrackPathFinder.class.getName());
    private final SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();
    private final UnmodifiableWorld world;
    private final Player player;
    private Vec2D startPoint;

    /**
     * Creates a finder for planning track construction in a world.
     *
     * @param world world whose terrain and existing track are used
     * @param player player for whom the route is planned
     */
    public TrackPathFinder(UnmodifiableWorld world, Player player) {
        this.world = world;
        this.player = player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abandonSearch() {
        pathFinder.abandonSearch();
    }

    /**
     * Builds all legal arrival vertices at a target tile.
     *
     * @param targetPoint destination tile
     * @return encoded target positions, restricted by existing track if present
     */
    private int[] findTargets(Vec2D targetPoint) {
        TerrainTile tile = world.getTile(targetPoint);
        TrackPiece trackPiece = tile.getTrackPiece();
        int[] targetInts;

        if (trackPiece != null) {
            /*
             * If there is already track here, we need to check what directions
             * we can build in without creating an illegal track config.
             */
            int ruleNumber = trackPiece.getTrackType().getId();
            TrackType trackType = world.getTrackType(ruleNumber);

            // Count number of possible directions.
            List<TileTransition> possibleDirections = new ArrayList<>();

            for (int i = 0; i < 8; i++) {
                TileTransition direction = TileTransition.getInstance(i);
                TrackConfiguration config = trackPiece.getTrackConfiguration();
                TrackConfiguration testConfig = TrackConfiguration.add(config, direction);

                if (trackType.trackPieceIsLegal(testConfig)) {
                    possibleDirections.add(direction);
                }
            }

            // Put them into an array.
            targetInts = new int[possibleDirections.size()];

            for (int i = 0; i < targetInts.length; i++) {
                TileTransition direction = possibleDirections.get(i);
                PositionOnTrack targetPot = new PositionOnTrack(targetPoint, direction.getOpposite());
                targetInts[i] = targetPot.toInt();
            }
        } else {
            // If there is no track here, we can go in any direction.
            targetInts = new int[8];

            for (int i = 0; i < 8; i++) {
                PositionOnTrack targetPot = new PositionOnTrack(targetPoint, TileTransition.getInstance(i));
                targetInts[i] = targetPot.toInt();
            }
        }

        return targetInts;
    }

    /**
     * Finds and returns a route between two tiles using the specified build
     * rules.
     *
     * @param start starting tile coordinate
     * @param targetPoint destination tile coordinate
     * @param buildTrackStrategy rules selecting track types by terrain
     * @return route as tile coordinates in traversal order
     * @throws PathNotFoundException if no route can be found
     */
    public List generatePath(Vec2D start, Vec2D targetPoint, BuildTrackStrategy buildTrackStrategy) throws PathNotFoundException {
        setupSearch(start, targetPoint, buildTrackStrategy);
        pathFinder.search(-1);

        List<Integer> path = pathFinder.retrievePath();

        return TrackUtils.convertPathToPoints(path);
    }

    /**
     * Returns the current status of the underlying search.
     *
     * @return pathfinder status
     */
    @Override
    public PathFinderStatus getStatus() {
        return pathFinder.getStatus();
    }

    /**
     * Returns the route found by the most recent search as tile coordinates.
     *
     * @return route coordinates in traversal order
     */
    public List<Vec2D> pathAsPoints() {
        List<Integer> path = pathFinder.retrievePath();

        return TrackUtils.convertPathToPoints(path);
    }

    /**
     * Returns the route found by the most recent search as transitions.
     *
     * @return route transitions in traversal order
     */
    public TileTransition[] pathAsVectors() {
        List<Integer> path = pathFinder.retrievePath();
        int size = path.size();
        TileTransition[] vectors = new TileTransition[size];
        PositionOnTrack progress = new PositionOnTrack();

        Vec2D p = startPoint;
        for (int i = 0; i < size; i++) {
            progress.setValuesFromInt(path.get(i));
            vectors[i] = TileTransition.getInstance(Vec2D.subtract(progress.getLocation(), p));
            p = progress.getLocation();
        }
        return vectors;
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
     * Configures a route search between two points using the supplied build
     * strategy.
     *
     * @param startPoint starting tile coordinate
     * @param targetPoint destination tile coordinate
     * @param buildTrackStrategy rules selecting track types by terrain
     * @throws PathNotFoundException if no route can be found or a start is
     *                               already a target
     */
    public void setupSearch(Vec2D startPoint, Vec2D targetPoint, BuildTrackStrategy buildTrackStrategy) throws PathNotFoundException {
        logger.debug("Find track path from " + startPoint + " to " + targetPoint);

        this.startPoint = startPoint;
        int[] targetInts = findTargets(targetPoint);
        int[] startInts = findTargets(startPoint);

        BuildTrackExplorer explorer = new BuildTrackExplorer(world, player, startPoint);
        explorer.setBuildTrackStrategy(buildTrackStrategy);

        pathFinder.setupSearch(startInts, targetInts, explorer);
    }
}