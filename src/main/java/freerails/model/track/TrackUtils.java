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

package freerails.model.track;

import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.explorer.FlatTrackExplorer;
import freerails.model.track.explorer.GraphExplorer;
import freerails.model.track.pathfinding.PathNotFoundException;
import freerails.model.track.pathfinding.PathOnTrackFinder;
import freerails.model.train.PositionOnTrack;
import freerails.model.world.UnmodifiableWorld;
import freerails.util.Vec2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility operations for track construction, connectivity, and routing.
 */
public final class TrackUtils {

    /**
     * Prevents instantiation of this utility class.
     */
    private TrackUtils() {
    }

    /**
     * This method may be called under 3 possible conditions: (1) when a station
     * is getting built, (2) when a station is getting upgraded, (3) when a
     * station is getting removed. Stations at the same coordinate are ignored,
     * as that represents an upgrade rather than a separate placement.
     *
     * @param world world whose stations are checked
     * @param location proposed station coordinate
     * @param trackPiece proposed station track piece
     * @return success if the proposed station does not overlap another station
     */
    public static TrackOverlapResult checkForOverlap(UnmodifiableWorld world, Vec2D location, TrackPiece trackPiece) {
        /*
         * Fix for 915945 (Stations should not overlap) Check that there is not
         * another station whose radius overlaps with the one we are building.
         */
        TrackType thisStationType = trackPiece.trackType();
        assert thisStationType.isStation();

        // for all player
        for (Player player: world.getPlayers()) {
            // for all stations of a player
            for (Station station: world.getStations(player)) {
                /*
                 * Fix for bug 948675 - Can't upgrade station types If locations
                 * are the same, then we are upgrading a station so it doesn't
                 * matter if the radii overlap.
                 */
                if (location.equals(station.getLocation())) {
                    continue;
                }

                TerrainTile tile = world.getTile(station.getLocation());
                TrackType otherStationType = tile.trackPiece().trackType();
                assert otherStationType.isStation();

                int sumOfRadii = otherStationType.getStationRadius() + thisStationType.getStationRadius();
                int sumOfRadiiSquared = sumOfRadii * sumOfRadii;
                Vec2D delta = Vec2D.subtract(station.getLocation(), location);

                // Do radii overlap?
                boolean xOverlap = sumOfRadiiSquared >= delta.x() * delta.x();
                boolean yOverlap = sumOfRadiiSquared >= delta.y() * delta.y();

                if (xOverlap && yOverlap) {
                    String message = "Too close to " + station.getStationName();

                    return TrackOverlapResult.failure(message);
                }
            }
        }

        return TrackOverlapResult.success();
    }

    /**
     * Tests whether a flat-track template conflicts diagonally with track on
     * the tiles directly above or below the supplied tile.
     *
     * @param point tile coordinate being checked
     * @param trackTemplate 9-bit track template to test
     * @param world world containing neighboring track
     * @return {@code true} if no diagonal conflict is detected
     */
    public static boolean noDiagonalTrackConflicts(Vec2D point, int trackTemplate, UnmodifiableWorld world) {
        /*
         * This method is needs replacing. It only deals with flat track pieces, and is rather hard to make sense of. LL
         */

        // int trackTemplate = (1 << (3 * (1 + tv.getY()) + (1 + tv.getX())));
        int trackTemplateAbove;
        int trackTemplateBelow;
        String templateString = "101000101";
        // Hack - so that result is as expected by earlier written code.
        StringBuilder strb = new StringBuilder(templateString);
        strb.reverse();
        templateString = strb.toString();

        // End of hack
        int cornersTemplate = Integer.parseInt(templateString, 2);
        trackTemplate = trackTemplate & cornersTemplate;

        Vec2D mapSize = world.getMapSize();

        // Avoid array-out-of-bounds exceptions.
        trackTemplateAbove = 0;
        if (point.y() > 0) {
            TerrainTile ft = world.getTile(new Vec2D(point.x(), point.y() - 1));
            TrackPiece tp = ft.trackPiece();
            if (tp != null) {
                trackTemplateAbove = tp.getTrackGraphicID();
            }
        }

        trackTemplateBelow = 0;
        if (point.y() + 1 < mapSize.y()) {
            TerrainTile ft = world.getTile(new Vec2D(point.x(), point.y() + 1));
            TrackPiece tp = ft.trackPiece();
            if (tp != null) {
                trackTemplateBelow = tp.getTrackGraphicID();
            }
        }

        trackTemplateAbove = trackTemplateAbove >> 6;
        trackTemplateBelow = trackTemplateBelow << 6;
        trackTemplate = trackTemplate & (trackTemplateAbove | trackTemplateBelow);

        return trackTemplate == 0;
        // Things are success.
    }

    /**
     * Finds the ID of the least expensive track type in a category.
     *
     * @param category category to search
     * @param world world supplying track types
     * @return ID of the cheapest matching type, or {@code null} if none exists
     */
    static Integer getCheapest(TrackCategory category, UnmodifiableWorld world) {
        TrackType cheapest = null;
        Integer cheapestID = null;
        for (TrackType trackType: world.getTrackTypes()) {
            if (trackType.getCategory() == category) {
                if (null == cheapest || cheapest.getPurchasingPrice().compareTo(trackType.getPurchasingPrice()) > 0) {
                    cheapest = trackType;
                    cheapestID = trackType.getId();
                }
            }
        }
        return cheapestID;
    }

    /**
     * Selects the first buildable candidate type for each terrain.
     *
     * @param allowable candidate type IDs in preference order
     * @param world world supplying terrains and track types
     * @return map from terrain ID to selected track type ID
     */
    static Map<Integer, Integer> generateRules(Iterable<Integer> allowable, UnmodifiableWorld world) {

        Map<Integer, Integer> newRules = new HashMap<>();
        for (Terrain terrainType: world.getTerrains()) {
            for (Integer rule : allowable) {
                if (null != rule) {
                    TrackType trackType = world.getTrackType(rule);
                    if (trackType.canBuildOnThisTerrainType(terrainType.getCategory())) {
                        newRules.put(terrainType.getId(), rule);
                        break;
                    }
                }
            }
        }
        return newRules;
    }

    /**
     * Converts encoded positions on track to their tile coordinates.
     *
     * @param path encoded path positions
     * @return tile coordinates in path order
     */
    public static List<Vec2D> convertPathToPoints(List<Integer> path) {
        PositionOnTrack positionOnTrack = new PositionOnTrack();
        List<Vec2D> proposedTrack = new ArrayList<>();

        for (Integer aPath : path) {
            positionOnTrack.setValuesFromInt(aPath);
            Vec2D p = positionOnTrack.getLocation();
            proposedTrack.add(p);
            // TrackPathFinder.logger.debug("Adding point " + p);
        }

        return proposedTrack;
    }

    /**
     * Creates a new track piece containing the tile-center bit and the supplied
     * outgoing direction.
     *
     * @param direction track direction to add
     * @param trackType type of track to create
     * @param playerId owning player's ID
     * @return a track piece with the combined configuration
     */
    public static TrackPiece getTrackPieceWhenOldTrackPieceIsNull(TrackConfigurations direction, TrackType trackType, int playerId) {
        TrackConfiguration simplestConfig = TrackConfiguration.getFlatInstance("000010000");
        TrackConfiguration trackConfiguration = TrackConfiguration.add(simplestConfig, direction);
        return new TrackPiece(trackConfiguration, trackType, playerId);
    }

    /**
     * Finds the first direction of a path from the current position to a
     * target. If no path exists, it selects an available adjacent track edge.
     *
     * @param world world containing track
     * @param currentPosition current encoded position on track
     * @param target destination tile
     * @return transition to follow from the current position
     * @throws NoTrackException if the current position has no usable track
     */
    public static TileTransition findNextStep(UnmodifiableWorld world, PositionOnTrack currentPosition, Vec2D target) {
        HashMap<Integer, TileTransition> destPaths;
        TileTransition nextTileTransition;
        destPaths = new HashMap<>();
        PathOnTrackFinder pathFinder = new PathOnTrackFinder(world);

        try {
            pathFinder.setupSearch(currentPosition.getLocation(), target);
            pathFinder.search(-1);
            TileTransition[] pathAsVectors = pathFinder.pathAsVectors();
            List<Integer> pathAsInts = pathFinder.pathAsInts();
            for (int i = 0; i < pathAsInts.size() - 1; i++) {
                int calcPos = pathAsInts.get(i) & (PositionOnTrack.MAX_COORDINATE | PositionOnTrack.MAX_COORDINATE << PositionOnTrack.BITS_FOR_COORDINATE);
                destPaths.put(calcPos, pathAsVectors[i + 1]);
            }
            nextTileTransition = pathAsVectors[0];
            return nextTileTransition;
        } catch (PathNotFoundException e) {
            // The pathfinder couldn't find a path so we go in any legal direction.
            GraphExplorer explorer = new FlatTrackExplorer(world, currentPosition);
            explorer.nextEdge();
            int next = explorer.getVertexConnectedByEdge();
            PositionOnTrack nextPosition = new PositionOnTrack(next);
            return nextPosition.getComingFrom();
        }
    }
}
