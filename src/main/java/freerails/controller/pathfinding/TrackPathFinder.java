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
 *
 */
package freerails.controller.pathfinding;

import freerails.controller.explorer.BuildTrackExplorer;
import freerails.controller.BuildTrackStrategy;
import freerails.util.Vector2D;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.SharedKey;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackRule;
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
    private final ReadOnlyWorld world;
    private final FreerailsPrincipal principal;
    private Vector2D startPoint;

    /**
     * @param world
     * @param principal
     */
    public TrackPathFinder(ReadOnlyWorld world, FreerailsPrincipal principal) {
        this.world = world;
        this.principal = principal;
    }

    private static List<Vector2D> convertPathToPoints(List<Integer> path) {
        PositionOnTrack positionOnTrack = new PositionOnTrack();
        List<Vector2D> proposedTrack = new ArrayList<>();

        for (Integer aPath : path) {
            positionOnTrack.setValuesFromInt(aPath);
            Vector2D p = positionOnTrack.getLocation();
            proposedTrack.add(p);
            logger.debug("Adding point " + p);
        }

        return proposedTrack;
    }

    /**
     *
     */
    public void abandonSearch() {
        pathFinder.abandonSearch();
    }

    private int[] findTargets(Vector2D targetPoint) {
        FullTerrainTile tile = (FullTerrainTile) world.getTile(targetPoint);
        TrackPiece trackPiece = tile.getTrackPiece();
        int ruleNumber = trackPiece.getTrackTypeID();

        int[] targetInts;

        if (tile.hasTrack()) {
            /*
             * If there is already track here, we need to check what directions
             * we can build in without creating an illegal track config.
             */
            TrackRule trackRule = (TrackRule) world.get(SharedKey.TrackRules, ruleNumber);

            // Count number of possible directions.
            List<TileTransition> possibleDirections = new ArrayList<>();

            for (int i = 0; i < 8; i++) {
                TileTransition direction = TileTransition.getInstance(i);
                TrackConfiguration config = trackPiece.getTrackConfiguration();
                TrackConfiguration testConfig = TrackConfiguration.add(config, direction);

                if (trackRule.trackPieceIsLegal(testConfig)) {
                    possibleDirections.add(direction);
                }
            }

            // Put them into an array.
            targetInts = new int[possibleDirections.size()];

            for (int i = 0; i < targetInts.length; i++) {
                TileTransition direction = possibleDirections.get(i);
                PositionOnTrack targetPot = PositionOnTrack.createFacing(targetPoint, direction);
                targetInts[i] = targetPot.toInt();
            }
        } else {
            // If there is no track here, we can go in any direction.
            targetInts = new int[8];

            for (int i = 0; i < 8; i++) {
                PositionOnTrack targetPot = PositionOnTrack.createComingFrom(targetPoint, TileTransition.getInstance(i));
                targetInts[i] = targetPot.toInt();
            }
        }

        return targetInts;
    }

    /**
     * @param start
     * @param targetPoint
     * @param bts
     * @return
     * @throws PathNotFoundException
     */
    public List generatePath(Vector2D start, Vector2D targetPoint, BuildTrackStrategy bts) throws PathNotFoundException {
        setupSearch(start, targetPoint, bts);
        pathFinder.search(-1);

        List<Integer> path = pathFinder.retrievePath();

        return convertPathToPoints(path);
    }

    /**
     * @return
     */
    public PathFinderStatus getStatus() {
        return pathFinder.getStatus();
    }

    /**
     * @return
     */
    public List<Vector2D> pathAsPoints() {
        List<Integer> path = pathFinder.retrievePath();

        return convertPathToPoints(path);
    }

    /**
     * @return
     */
    public TileTransition[] pathAsVectors() {
        List<Integer> path = pathFinder.retrievePath();
        int size = path.size();
        TileTransition[] vectors = new TileTransition[size];
        PositionOnTrack progress = new PositionOnTrack();

        Vector2D p = startPoint;
        for (int i = 0; i < size; i++) {
            progress.setValuesFromInt(path.get(i));
            vectors[i] = TileTransition.getInstance(Vector2D.subtract(progress.getLocation(), p));
            p = progress.getLocation();
        }
        return vectors;
    }

    /**
     * @param maxDuration
     * @throws PathNotFoundException
     */
    public void search(long maxDuration) throws PathNotFoundException {
        pathFinder.search(maxDuration);
    }

    /**
     * @param startPoint
     * @param targetPoint
     * @param bts
     * @throws PathNotFoundException
     */
    public void setupSearch(Vector2D startPoint, Vector2D targetPoint, BuildTrackStrategy bts) throws PathNotFoundException {
        logger.debug("Find track path from " + startPoint + " to " + targetPoint);

        this.startPoint = startPoint;
        int[] targetInts = findTargets(targetPoint);
        int[] startInts = findTargets(startPoint);

        BuildTrackExplorer explorer = new BuildTrackExplorer(world, principal, startPoint);
        explorer.setBuildTrackStrategy(bts);

        pathFinder.setupSearch(startInts, targetInts, explorer);
    }
}