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
import freerails.model.operations.station.Station;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.explorer.FlatTrackExplorer;
import freerails.model.track.explorer.GraphExplorer;
import freerails.model.track.pathfinding.PathNotFoundException;
import freerails.model.track.pathfinding.PathOnTrackFinder;
import freerails.model.operations.train.PositionOnTrack;
import freerails.model.world.UnmodifiableWorld;
import freerails.util.IntPoint2D;

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
    public static TrackOverlapResult checkForOverlap(UnmodifiableWorld world, IntPoint2D location, TrackPiece trackPiece) {
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
                IntPoint2D delta = IntPoint2D.subtract(station.getLocation(), location);

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
     * @param trackTemplate 8-bit directional mask to test
     * @param world world containing neighboring track
     * @return {@code true} if no diagonal conflict is detected
     */
    public static boolean noDiagonalTrackConflicts(IntPoint2D point, int trackTemplate, UnmodifiableWorld world) {
        IntPoint2D mapSize = world.getMapSize();

        int trackTemplateAbove = 0;
        if (point.y() > 0) {
            TerrainTile ft = world.getTile(new IntPoint2D(point.x(), point.y() - 1));
            TrackPiece tp = ft.trackPiece();
            if (tp != null) {
                trackTemplateAbove = tp.getTrackGraphicID();
            }
        }

        int trackTemplateBelow = 0;
        if (point.y() + 1 < mapSize.y()) {
            TerrainTile ft = world.getTile(new IntPoint2D(point.x(), point.y() + 1));
            TrackPiece tp = ft.trackPiece();
            if (tp != null) {
                trackTemplateBelow = tp.getTrackGraphicID();
            }
        }

        boolean conflictAbove = hasConnection(trackTemplate, TileTransition.NORTH_WEST)
                && hasConnection(trackTemplateAbove, TileTransition.SOUTH_WEST)
                || hasConnection(trackTemplate, TileTransition.NORTH_EAST)
                && hasConnection(trackTemplateAbove, TileTransition.SOUTH_EAST);
        boolean conflictBelow = hasConnection(trackTemplate, TileTransition.SOUTH_WEST)
                && hasConnection(trackTemplateBelow, TileTransition.NORTH_WEST)
                || hasConnection(trackTemplate, TileTransition.SOUTH_EAST)
                && hasConnection(trackTemplateBelow, TileTransition.NORTH_EAST);

        return !conflictAbove && !conflictBelow;
    }

    /**
     * Tests whether replacing a track piece with another creates a diagonal
     * conflict.
     *
     * @param point tile coordinate being changed
     * @param before track piece before the change, or {@code null}
     * @param after track piece after the change, or {@code null}
     * @param world world containing neighboring track
     * @return {@code true} if neither configuration conflicts diagonally
     */
    public static boolean noDiagonalTrackConflictsForChange(
            IntPoint2D point, TrackPiece before, TrackPiece after, UnmodifiableWorld world) {
        return (before == null || noDiagonalTrackConflicts(point, before.getTrackGraphicID(), world))
                && (after == null || noDiagonalTrackConflicts(point, after.getTrackGraphicID(), world));
    }

    /**
     * Tests whether a track-piece change removes any directional connections.
     *
     * @param before track piece before the change, or {@code null}
     * @param after track piece after the change, or {@code null}
     * @return {@code true} if at least one previous connection is absent after
     *         the change
     */
    public static boolean removesTrackConnections(TrackPiece before, TrackPiece after) {
        if (before == null) {
            return false;
        }

        int beforeConfiguration = before.trackConfiguration().get8bitTemplate();
        int afterConfiguration = after == null ? 0 : after.trackConfiguration().get8bitTemplate();
        return (beforeConfiguration & ~afterConfiguration) != 0;
    }

    /**
     * Tests whether a track-piece change adds any directional connections.
     *
     * @param before track piece before the change, or {@code null}
     * @param after track piece after the change, or {@code null}
     * @return {@code true} if a connection is present after but not before
     */
    public static boolean addsTrackConnections(TrackPiece before, TrackPiece after) {
        int beforeConfiguration = before == null ? 0 : before.trackConfiguration().get8bitTemplate();
        int afterConfiguration = after == null ? 0 : after.trackConfiguration().get8bitTemplate();
        return (afterConfiguration & ~beforeConfiguration) != 0;
    }

    /**
     * Tests whether a connected track piece can be replaced by a station.
     *
     * @param existingTrack track currently on the tile, or {@code null}
     * @param proposedStation station track piece to place
     * @return {@code true} if both pieces have at least one track connection
     */
    public static boolean canUpgradeToStation(TrackPiece existingTrack, TrackPiece proposedStation) {
        return proposedStation != null
                && proposedStation.trackType().isStation()
                && proposedStation.trackConfiguration().get8bitTemplate() != 0
                && existingTrack != null
                && existingTrack.trackConfiguration().get8bitTemplate() != 0;
    }

    /**
     * Tests whether a directional mask includes the supplied transition.
     *
     * @param template directional mask
     * @param transition transition to test
     * @return {@code true} if the transition is included
     */
    private static boolean hasConnection(int template, TileTransition transition) {
        return (template & transition.get8bitTemplate()) != 0;
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
    public static List<IntPoint2D> convertPathToPoints(List<Integer> path) {
        PositionOnTrack positionOnTrack = new PositionOnTrack();
        List<IntPoint2D> proposedTrack = new ArrayList<>();

        for (Integer aPath : path) {
            positionOnTrack.setValuesFromInt(aPath);
            IntPoint2D p = positionOnTrack.getLocation();
            proposedTrack.add(p);
            // TrackPathFinder.logger.debug("Adding point " + p);
        }

        return proposedTrack;
    }

    /**
     * Creates a new track piece containing the supplied outgoing direction.
     *
     * @param direction track direction to add
     * @param trackType type of track to create
     * @param playerId owning player's ID
     * @return a track piece with the combined configuration
     */
    public static TrackPiece getTrackPieceWhenOldTrackPieceIsNull(TrackConfigurations direction, TrackType trackType, int playerId) {
        TrackConfiguration trackConfiguration = TrackConfiguration.getFlatInstance(direction);
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
    public static TileTransition findNextStep(UnmodifiableWorld world, PositionOnTrack currentPosition, IntPoint2D target) {
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
