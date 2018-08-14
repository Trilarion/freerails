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

package freerails.model.track;

import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.pathfinding.TrackPathFinder;
import freerails.model.train.PositionOnTrack;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.move.Status;
import freerails.util.Vec2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public final class TrackUtils {

    private TrackUtils() {
    }

    /**
     * This method may be called under 3 possible conditions: (1) when a station
     * is getting built, (2) when a station is getting upgraded, (3) when a
     * station is getting removed.
     */
    public static Status checkForOverlap(World world, Vec2D location, TrackPiece trackPiece) {
        /*
         * Fix for 915945 (Stations should not overlap) Check that there is not
         * another station whose radius overlaps with the one we are building.
         */
        TrackType thisStationType = trackPiece.getTrackType();
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
                TrackType otherStationType = tile.getTrackPiece().getTrackType();
                assert otherStationType.isStation();

                int sumOfRadii = otherStationType.getStationRadius() + thisStationType.getStationRadius();
                int sumOfRadiiSquared = sumOfRadii * sumOfRadii;
                Vec2D delta = Vec2D.subtract(station.getLocation(), location);

                // Do radii overlap?
                boolean xOverlap = sumOfRadiiSquared >= delta.x * delta.x;
                boolean yOverlap = sumOfRadiiSquared >= delta.y * delta.y;

                if (xOverlap && yOverlap) {
                    String message = "Too close to " + station.getStationName();

                    return Status.moveFailed(message);
                }
            }
        }

        return Status.OK;
    }

    public static boolean noDiagonalTrackConflicts(Vec2D point, int trackTemplate, World world) {
        /*
         * This method is needs replacing. It only deals with flat track pieces, and is rather hard to make sense of. LL
         */

        // int trackTemplate = (1 << (3 * (1 + tv.getY()) + (1 + tv.getX())));
        int trackTemplateAbove;
        int trackTemplateBelow;
        String templateString = "101000101";
        // Hack - so that result is as expected by earlier written code.
        StringBuffer strb = new StringBuffer(templateString);
        strb.reverse();
        templateString = strb.toString();

        // End of hack
        int cornersTemplate = Integer.parseInt(templateString, 2);
        trackTemplate = trackTemplate & cornersTemplate;

        Vec2D mapSize = world.getMapSize();

        // Avoid array-out-of-bounds exceptions.
        trackTemplateAbove = 0;
        if (point.y > 0) {
            TerrainTile ft = world.getTile(new Vec2D(point.x, point.y - 1));
            TrackPiece tp = ft.getTrackPiece();
            if (tp != null) {
                trackTemplateAbove = tp.getTrackGraphicID();
            }
        }

        trackTemplateBelow = 0;
        if ((point.y + 1) < mapSize.y) {
            TerrainTile ft = world.getTile(new Vec2D(point.x, point.y + 1));
            TrackPiece tp = ft.getTrackPiece();
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
}
