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

package freerails.move.mapupdatemove;

import freerails.model.terrain.Terrain;
import freerails.model.track.TrackType;
import freerails.model.world.*;
import freerails.move.Status;
import freerails.move.generator.MoveTrainMoveGenerator;
import freerails.util.Vec2D;
import freerails.model.game.GameRules;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackPiece;

import java.awt.*;
import java.util.Objects;

/**
 * This Move adds, removes, or upgrades the track on a single tile.
 */
public final class ChangeTrackPieceMove implements TrackMove {

    private static final long serialVersionUID = 4120849958418591801L;
    public final TrackPiece trackPieceBefore;
    public final TrackPiece trackPieceAfter;
    public final Vec2D location;

    /**
     * @param before
     * @param after
     * @param p
     */
    public ChangeTrackPieceMove(TrackPiece before, TrackPiece after, Vec2D p) {
        trackPieceBefore = before;
        trackPieceAfter = after;
        location = p;
    }

    // TODO move static code to model
    /**
     * This method may be called under 3 possible conditions: (1) when a station
     * is getting built, (2) when a station is getting upgraded, (3) when a
     * station is getting removed.
     */
    private static Status checkForOverlap(World world, Vec2D location, TrackPiece trackPiece) {
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

    private static boolean noDiagonalTrackConflicts(Vec2D point, int trackTemplate, World world) {
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

    /**
     * @return
     */
    public Vec2D getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        int result;
        result = (trackPieceBefore != null ? trackPieceBefore.hashCode() : 0);
        result = 29 * result + (trackPieceAfter != null ? trackPieceAfter.hashCode() : 0);
        result = 29 * result + location.hashCode();

        return result;
    }

    /**
     * @return
     */
    public TrackPiece getOldTrackPiece() {
        return trackPieceBefore;
    }

    /**
     * @return
     */
    public TrackPiece getNewTrackPiece() {
        return trackPieceAfter;
    }

    public Status tryDoMove(World world, Player player) {
        return tryMove(world, trackPieceBefore, trackPieceAfter);
    }

    private Status tryMove(World world, TrackPiece oldTrackPiece, TrackPiece newTrackPiece) {
        // Check that location is on the map.
        if (!world.boundsContain(location)) {
            return Status.moveFailed("Tried to build track outside the map.");
        }

        // Check that we would not change another players track if this is not allowed.
        GameRules rules = ((UnmodifiableWorld) world).getGameRules();
        if (!rules.canConnectToOtherRRTrack()) {
            // TODO what about removing track of someone else
            if (oldTrackPiece != null && newTrackPiece != null) {
                int oldOwner = oldTrackPiece.getOwnerID();
                int newOwner = newTrackPiece.getOwnerID();

                if (oldOwner != newOwner) {
                    return Status.moveFailed("Not allowed to connect to other RR");
                }
            }
        }

        // Check that the current track piece at this.location is the same as this.oldTrackPiece.
        TrackPiece currentTrackPieceAtLocation = world.getTile(location).getTrackPiece();

        if (!Objects.equals(currentTrackPieceAtLocation, oldTrackPiece)) {
            return Status.moveFailed("Unexpected track piece found at location: " + location.x + " ," + location.y);
        }

        if (Objects.equals(oldTrackPiece, newTrackPiece)) {
            return Status.moveFailed("Already the same track here!");
        }

        if (oldTrackPiece != null) {
            TrackType expectedTrackRule = oldTrackPiece.getTrackType();
            TrackType actualTrackRule = currentTrackPieceAtLocation.getTrackType();

            if (!expectedTrackRule.equals(actualTrackRule)) {
                return Status.moveFailed("Expected '" + expectedTrackRule.getName() + "' but found '" + actualTrackRule.getName() + "' at " + location.x + " ," + location.y);
            }

            // Check that oldTrackPiece is not the same as newTrackPiece
            // TODO equals instead of ==
            if (newTrackPiece != null && (oldTrackPiece.getTrackConfiguration() == newTrackPiece.getTrackConfiguration()) && (oldTrackPiece.getTrackType().equals(newTrackPiece.getTrackType()))) {
                return Status.moveFailed("Already track here!");
            }

            // Check for illegal track configurations.
            if (!(oldTrackPiece.getTrackType().trackPieceIsLegal(oldTrackPiece.getTrackConfiguration()))) {
                return Status.moveFailed("Illegal track configuration.");
            }

            // Check for diagonal conflicts.
            if (newTrackPiece != null && !(noDiagonalTrackConflicts(location, oldTrackPiece.getTrackGraphicID(), world) && noDiagonalTrackConflicts(location, newTrackPiece.getTrackGraphicID(), world))) {
                return Status.moveFailed("Illegal track configuration - diagonal conflict");
            }
        }

        if (newTrackPiece != null) {

            int terrainType = world.getTile(location).getTerrainTypeId();
            Terrain terrain = world.getTerrain(terrainType);

            // Check for illegal track configurations.
            if (!(newTrackPiece.getTrackType().trackPieceIsLegal(newTrackPiece.getTrackConfiguration()))) {
                return Status.moveFailed("Illegal track configuration.");
            }

            if (!newTrackPiece.getTrackType().canBuildOnThisTerrainType(terrain.getCategory())) {
                String thisTrackType = newTrackPiece.getTrackType().getName();
                String terrainCategory = terrain.getCategory().toString().toLowerCase();

                return Status.moveFailed("Can't build " + thisTrackType + " on " + terrainCategory);
            }

            // Check for overlapping stations.
            if (newTrackPiece.getTrackType().isStation()) {
                Status status = ChangeTrackPieceMove.checkForOverlap(world, location, newTrackPiece);
                if (!status.succeeds()) return status;
            }

        }

        return Status.OK;
    }

    public Status tryUndoMove(World world, Player player) {
        return tryMove(world, trackPieceAfter, trackPieceBefore);
    }

    public Status doMove(World world, Player player) {
        MoveTrainMoveGenerator.clearCache();
        Status status = tryDoMove(world, player);

        if (!status.succeeds()) {
            return status;
        }
        move(world, trackPieceAfter);

        return status;
    }

    private void move(World world, TrackPiece newTrackPiece) {
        // FIXME why is oldTrackPiece not used???
        TerrainTile oldTile = world.getTile(location);
        int terrain = oldTile.getTerrainTypeId();
        TerrainTile newTile = new TerrainTile(terrain, newTrackPiece);
        world.setTile(location, newTile);
    }

    public Status undoMove(World world, Player player) {
        MoveTrainMoveGenerator.clearCache();
        Status status = tryUndoMove(world, player);

        if (!status.succeeds()) {
            return status;
        }
        move(world, trackPieceBefore);

        return status;
    }

    /**
     * @return
     */
    public Rectangle getUpdatedTiles() {
        // If we are building or removing a station, we need to repaint/remove the station radius that appears on the map.
        int radius = 1;

        if (trackPieceAfter != null) {
            TrackType trackRuleAfter = trackPieceAfter.getTrackType();

            if (trackRuleAfter.isStation()) {
                radius = Math.max(radius, trackRuleAfter.getStationRadius());
            }
        }

        if (trackPieceBefore != null) {
            TrackType trackRuleBefore = trackPieceBefore.getTrackType();
            if (trackRuleBefore.isStation()) {
                radius = Math.max(radius, trackRuleBefore.getStationRadius());
            }
        }

        // Just to be safe.
        radius++;

        int x;
        int y;
        int width;
        int height;

        x = location.x - radius;
        y = location.y - radius;
        width = radius * 2 + 1;
        height = radius * 2 + 1;

        return new Rectangle(x, y, width, height);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChangeTrackPieceMove) {
            ChangeTrackPieceMove changeTrackPieceMove = (ChangeTrackPieceMove) obj;
            boolean fieldPointEqual = location.equals(changeTrackPieceMove.location);
            boolean fieldoldTrackPieceEqual = Objects.equals(trackPieceBefore, changeTrackPieceMove.trackPieceBefore);
            boolean fieldnewTrackPieceEqual = trackPieceAfter.equals(changeTrackPieceMove.trackPieceAfter);

            return fieldPointEqual && fieldoldTrackPieceEqual && fieldnewTrackPieceEqual;
        }
        return false;
    }
}