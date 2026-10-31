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

package freerails.move.mapupdatemove;

import freerails.model.terrain.Terrain;
import freerails.model.track.TrackType;
import freerails.model.track.TrackOverlapResult;
import freerails.model.track.TrackUtils;
import freerails.model.world.*;
import freerails.move.Status;
import freerails.util.Vec2D;
import freerails.model.game.Rules;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackPiece;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

/**
 * This Move adds, removes, or upgrades the track on a single tile.

 * @param trackPieceBefore the track piece present before the change, or {@code null}
 * @param trackPieceAfter the track piece to place, or {@code null} to remove track
 * @param location the map location of the terrain
 */
public record ChangeTrackPieceMove(TrackPiece trackPieceBefore, TrackPiece trackPieceAfter, Vec2D location) implements TrackMove {

    private static final long serialVersionUID = 4120849958418591801L;

    /**
     * Creates a move that replaces the track piece at a map location.
     *
     * @param trackPieceBefore the track piece present before the change, or {@code null}
     * @param trackPieceAfter the track piece to place, or {@code null} to remove track
     * @param location the map location of the tile or track section
     */
    public ChangeTrackPieceMove {
    }

    /**
     * Returns the track piece that was present before this move.
     *
     * @return the previous track piece, or {@code null} if the tile had no track
     */
    public TrackPiece getOldTrackPiece() {
        return trackPieceBefore;
    }

    /**
     * Returns the track piece this move will place.
     *
     * @return the new track piece, or {@code null} if the move removes track
     */
    public TrackPiece getNewTrackPiece() {
        return trackPieceAfter;
    }

    /**
     * Checks whether this move can be applied to the supplied world.
     *
     * @param world the world whose state is used or updated
     * @return a status indicating whether the move can be applied
     */
    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        // TODO put part of it in model
        // Check that location is on the map.
        if (!world.boundsContain(location)) {
            return Status.fail("Tried to build track outside the map.");
        }

        // Check that we would not change another players track if this is not allowed.
        Rules rules = world.getRules();
        if (!rules.canConnectToOtherPlayersTracks()) {
            // TODO what about removing track of someone else
            if (trackPieceBefore != null && trackPieceAfter != null) {
                int oldOwner = trackPieceBefore.playerId();
                int newOwner = trackPieceAfter.playerId();

                if (oldOwner != newOwner) {
                    return Status.fail("Not allowed to connect to other RR");
                }
            }
        }

        // Check that the current track piece at this.location is the same as this.oldTrackPiece.
        TrackPiece currentTrackPieceAtLocation = world.getTile(location).trackPiece();

        if (!Objects.equals(currentTrackPieceAtLocation, trackPieceBefore)) {
            return Status.fail("Unexpected track piece found at location: " + location.x() + " ," + location.y());
        }

        if (Objects.equals(trackPieceBefore, trackPieceAfter)) {
            return Status.fail("Already the same track here!");
        }

        if (trackPieceBefore != null) {
            TrackType expectedTrackRule = trackPieceBefore.trackType();
            TrackType actualTrackRule = currentTrackPieceAtLocation.trackType();

            if (!expectedTrackRule.equals(actualTrackRule)) {
                return Status.fail("Expected '" + expectedTrackRule.getName() + "' but found '" + actualTrackRule.getName() + "' at " + location.x() + " ," + location.y());
            }

            // Check that oldTrackPiece is not the same as newTrackPiece
            // TODO equals instead of ==
            if (trackPieceAfter != null && trackPieceBefore.trackConfiguration() == trackPieceAfter.trackConfiguration() && trackPieceBefore.trackType().equals(trackPieceAfter.trackType())) {
                return Status.fail("Already track here!");
            }

            // Check for illegal track configurations.
            if (!trackPieceBefore.trackType().trackPieceIsLegal(trackPieceBefore.trackConfiguration())) {
                return Status.fail("Illegal track configuration.");
            }

            // Check for diagonal conflicts.
            if (trackPieceAfter != null && !(TrackUtils.noDiagonalTrackConflicts(location, trackPieceBefore.getTrackGraphicID(), world) && TrackUtils.noDiagonalTrackConflicts(location, trackPieceAfter.getTrackGraphicID(), world))) {
                return Status.fail("Illegal track configuration - diagonal conflict");
            }
        }

        if (trackPieceAfter != null) {

            int terrainType = world.getTile(location).terrainTypeId();
            Terrain terrain = world.getTerrain(terrainType);

            // Check for illegal track configurations.
            if (!trackPieceAfter.trackType().trackPieceIsLegal(trackPieceAfter.trackConfiguration())) {
                return Status.fail("Illegal track configuration.");
            }

            if (!trackPieceAfter.trackType().canBuildOnThisTerrainType(terrain.getCategory())) {
                String thisTrackType = trackPieceAfter.trackType().getName();
                String terrainCategory = terrain.getCategory().toString().toLowerCase();

                return Status.fail("Can't build " + thisTrackType + " on " + terrainCategory);
            }

            // Check for overlapping stations.
            if (trackPieceAfter.trackType().isStation()) {
                TrackOverlapResult overlapResult = TrackUtils.checkForOverlap(world, location, trackPieceAfter);
                if (!overlapResult.isSuccess()) {
                    return Status.fail(overlapResult.message());
                }
            }

        }

        return Status.OK;
    }

    /**
     * Applies this move to the supplied world.
     *
     * @param world the world whose state is used or updated
     */
    @Override
    public void apply(@NotNull World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) {
            throw new RuntimeException(status.getMessage());
        }
        // TODO why is oldTrackPiece not used???
        TerrainTile oldTile = world.getTile(location);
        int terrain = oldTile.terrainTypeId();
        TerrainTile newTile = new TerrainTile(terrain, trackPieceAfter);
        world.setTile(location, newTile);
    }

    /**
     * @return the bounds of the map tiles updated by this move
     */
    @Override
    public Rectangle getUpdatedTiles() {
        // If we are building or removing a station, we need to repaint/remove the station radius that appears on the map.
        int radius = 1;

        if (trackPieceAfter != null) {
            TrackType trackRuleAfter = trackPieceAfter.trackType();

            if (trackRuleAfter.isStation()) {
                radius = Math.max(radius, trackRuleAfter.getStationRadius());
            }
        }

        if (trackPieceBefore != null) {
            TrackType trackRuleBefore = trackPieceBefore.trackType();
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

        x = location.x() - radius;
        y = location.y() - radius;
        width = radius * 2 + 1;
        height = radius * 2 + 1;

        return new Rectangle(x, y, width, height);
    }

}