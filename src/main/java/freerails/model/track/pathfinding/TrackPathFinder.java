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
     * @param world
     * @param player
     */
    public TrackPathFinder(UnmodifiableWorld world, Player player) {
        this.world = world;
        this.player = player;
    }

    /**
     *
     */
    @Override
    public void abandonSearch() {
        pathFinder.abandonSearch();
    }

    private int[] findTargets(Vec2D targetPoint) {
        TerrainTile tile = (TerrainTile) world.getTile(targetPoint);
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
     * @param start
     * @param targetPoint
     * @param buildTrackStrategy
     * @return
     * @throws PathNotFoundException
     */
    public List generatePath(Vec2D start, Vec2D targetPoint, BuildTrackStrategy buildTrackStrategy) throws PathNotFoundException {
        setupSearch(start, targetPoint, buildTrackStrategy);
        pathFinder.search(-1);

        List<Integer> path = pathFinder.retrievePath();

        return TrackUtils.convertPathToPoints(path);
    }

    /**
     * @return
     */
    @Override
    public PathFinderStatus getStatus() {
        return pathFinder.getStatus();
    }

    /**
     * @return
     */
    public List<Vec2D> pathAsPoints() {
        List<Integer> path = pathFinder.retrievePath();

        return TrackUtils.convertPathToPoints(path);
    }

    /**
     * @return
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
     * @param maxDuration
     * @throws PathNotFoundException
     */
    @Override
    public void search(long maxDuration) throws PathNotFoundException {
        pathFinder.search(maxDuration);
    }

    /**
     * @param startPoint
     * @param targetPoint
     * @param buildTrackStrategy
     * @throws PathNotFoundException
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