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
import freerails.model.track.TrackUtils;
import freerails.model.world.*;
import freerails.move.Status;
import freerails.move.generator.MoveTrainMoveGenerator;
import freerails.util.Vec2D;
import freerails.model.game.GameRules;
import freerails.model.player.Player;
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
            if (newTrackPiece != null && !(TrackUtils.noDiagonalTrackConflicts(location, oldTrackPiece.getTrackGraphicID(), world) && TrackUtils.noDiagonalTrackConflicts(location, newTrackPiece.getTrackGraphicID(), world))) {
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
                Status status = TrackUtils.checkForOverlap(world, location, newTrackPiece);
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