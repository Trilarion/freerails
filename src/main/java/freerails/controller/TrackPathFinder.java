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
package freerails.controller;

import freerails.util.Point2D;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TileTransition;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackPiece;
import freerails.world.track.TrackRule;
import freerails.world.train.PositionOnTrack;
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
    private Point2D startPoint;

    /**
     * @param world
     * @param principal
     */
    public TrackPathFinder(ReadOnlyWorld world, FreerailsPrincipal principal) {
        this.world = world;
        this.principal = principal;
    }

    private static List<Point2D> convertPath2Points(List<Integer> path) {
        PositionOnTrack progress = new PositionOnTrack();
        List<Point2D> proposedTrack = new ArrayList<>();

        Point2D p;
        for (Integer aPath : path) {
            progress.setValuesFromInt(aPath);
            p = new Point2D(progress.getX(), progress.getY());
            proposedTrack.add(p);
            if (logger.isDebugEnabled()) {
                logger.debug("Adding point " + p);
            }
        }

        return proposedTrack;
    }

    /**
     *
     */
    public void abandonSearch() {
        pathFinder.abandonSearch();
    }

    private int[] findTargets(Point2D targetPoint) {
        FullTerrainTile tile = (FullTerrainTile) world.getTile(targetPoint.x, targetPoint.y);
        TrackPiece trackPiece = tile.getTrackPiece();
        int ruleNumber = trackPiece.getTrackTypeID();

        int[] targetInts;

        if (tile.hasTrack()) {
            /*
             * If there is already track here, we need to check what directions
             * we can build in without creating an illegal track config.
             */
            TrackRule trackRule = (TrackRule) world.get(SKEY.TRACK_RULES, ruleNumber);

            /* Count number of possible directions. */
            List<TileTransition> possibleDirections = new ArrayList<>();

            for (int i = 0; i < 8; i++) {
                TileTransition direction = TileTransition.getInstance(i);
                TrackConfiguration config = trackPiece.getTrackConfiguration();
                TrackConfiguration testConfig = TrackConfiguration.add(config, direction);

                if (trackRule.trackPieceIsLegal(testConfig)) {
                    possibleDirections.add(direction);
                }
            }

            /* Put them into an array. */
            targetInts = new int[possibleDirections.size()];

            for (int i = 0; i < targetInts.length; i++) {
                TileTransition direction = possibleDirections.get(i);
                PositionOnTrack targetPot = PositionOnTrack.createFacing(targetPoint.x, targetPoint.y, direction);
                targetInts[i] = targetPot.toInt();
            }
        } else {
            /* If there is no track here, we can go in any direction. */
            targetInts = new int[8];

            for (int i = 0; i < 8; i++) {
                PositionOnTrack targetPot = PositionOnTrack.createComingFrom(targetPoint.x, targetPoint.y, TileTransition.getInstance(i));
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
    public List generatePath(Point2D start, Point2D targetPoint, BuildTrackStrategy bts) throws PathNotFoundException {
        setupSearch(start, targetPoint, bts);
        pathFinder.search(-1);

        List<Integer> path = pathFinder.retrievePath();

        return convertPath2Points(path);
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
    public List<Point2D> pathAsPoints() {
        List<Integer> path = pathFinder.retrievePath();

        return convertPath2Points(path);
    }

    /**
     * @return
     */
    public TileTransition[] pathAsVectors() {
        List<Integer> path = pathFinder.retrievePath();
        int size = path.size();
        TileTransition[] vectors = new TileTransition[size];
        PositionOnTrack progress = new PositionOnTrack();

        int x = startPoint.x;
        int y = startPoint.y;
        for (int i = 0; i < size; i++) {
            progress.setValuesFromInt(path.get(i));
            int x2 = progress.getX();
            int y2 = progress.getY();
            vectors[i] = TileTransition.getInstance(x2 - x, y2 - y);
            x = x2;
            y = y2;
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
    public void setupSearch(Point2D startPoint, Point2D targetPoint, BuildTrackStrategy bts) throws PathNotFoundException {
        logger.debug("Find track path from " + startPoint + " to " + targetPoint);

        this.startPoint = startPoint;
        int[] targetInts = findTargets(targetPoint);
        int[] startInts = findTargets(startPoint);

        BuildTrackExplorer explorer = new BuildTrackExplorer(world, principal, startPoint);
        explorer.setBuildTrackStrategy(bts);

        pathFinder.setupSearch(startInts, targetInts, explorer);
    }
}