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

package freerails.move.generator;

import freerails.model.player.Player;
import freerails.model.track.NoTrackException;
import freerails.model.track.TrackConfigurations;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackType;
import freerails.model.terrain.TileTransition;
import freerails.model.world.UnmodifiableWorld;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.move.mapupdatemove.TrackMove;
import freerails.util.IntPoint2D;

/**
 * Generates the moves that build, remove, and upgrade track. The track piece
 * calculations themselves are done by {@link TrackPiece}; transactions are not
 * included (see {@link TrackMoveTransactionsGenerator}).
 */
public final class TrackMoveGenerator {

    private TrackMoveGenerator() {
    }

    /**
     * Generates the move that builds one track section between two neighboring tiles.
     *
     * @param from the tile the section starts on
     * @param direction the direction from {@code from} to the neighboring tile
     * @param typeA the track type used if there is no track on {@code from}
     * @param typeB the track type used if there is no track on the neighboring tile
     * @param world the game world being queried
     * @param player the player building the track
     * @return the build track move
     */
    public static ChangeTrackPieceCompositeMove generateBuildTrackMove(IntPoint2D from, TileTransition direction, TrackType typeA, TrackType typeB, UnmodifiableWorld world, Player player) {
        ChangeTrackPieceMove a = buildTrackOnTile(from, direction, typeA, world, player);
        ChangeTrackPieceMove b = buildTrackOnTile(direction.createRelocatedPoint(from), direction.getOpposite(), typeB, world, player);

        return new ChangeTrackPieceCompositeMove(a, b, player);
    }

    /**
     * Generates the move that removes one track section between two neighboring
     * tiles. Removing the player's station track also removes the station and the
     * schedule stops at it.
     *
     * @param from the tile the section starts on
     * @param direction the direction from {@code from} to the neighboring tile
     * @param world the game world being queried
     * @param player the player removing the track
     * @return the remove track move
     * @throws NoTrackException if there is no track to remove
     * @throws IllegalStateException if the removal would remove another player's station
     */
    public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(IntPoint2D from, TileTransition direction, UnmodifiableWorld world, Player player) {
        TrackMove a = removeTrackOnTile(from, direction, world, player);
        TrackMove b = removeTrackOnTile(direction.createRelocatedPoint(from), direction.getOpposite(), world, player);

        return new ChangeTrackPieceCompositeMove(a, b, player);
    }

    /**
     * Generates the move that changes the track type on a tile, keeping its configuration.
     *
     * @param location the tile to upgrade
     * @param trackType the new track type
     * @param world the game world being queried
     * @param player the player upgrading the track
     * @return the upgrade move
     * @throws NoTrackException if there is no track to upgrade
     * @throws IllegalStateException if the tile has station track, which is never upgraded
     */
    public static ChangeTrackPieceMove generateUpgradeTrackMove(IntPoint2D location, TrackType trackType, UnmodifiableWorld world, Player player) {
        TrackPiece before = world.boundsContain(location) ? world.getTile(location).trackPiece() : null;
        if (before == null) {
            throw new NoTrackException("No track to upgrade.");
        }
        // We don't want to 'upgrade' a station to track. See bug 874416.
        if (before.trackType().isStation()) {
            throw new IllegalStateException("No need to upgrade track at station.");
        }
        return new ChangeTrackPieceMove(before, before.withTrackType(trackType, player.getId()), location);
    }

    /**
     * Returns the change that adds a connection on one tile.
     */
    private static ChangeTrackPieceMove buildTrackOnTile(IntPoint2D location, TrackConfigurations direction, TrackType trackType, UnmodifiableWorld world, Player player) {
        TrackPiece before = world.boundsContain(location) ? world.getTile(location).trackPiece() : null;
        TrackPiece after = before != null
                ? before.withConnection(direction, player.getId())
                : TrackPiece.create(direction, trackType, player.getId());

        return new ChangeTrackPieceMove(before, after, location);
    }

    /**
     * Returns the change that removes a connection on one tile; for station track
     * this is the complete station removal.
     */
    private static TrackMove removeTrackOnTile(IntPoint2D location, TrackConfigurations direction, UnmodifiableWorld world, Player player) {
        TrackPiece before = world.boundsContain(location) ? world.getTile(location).trackPiece() : null;
        if (before == null) {
            // Fix for bug [ 948670 ] Removing non-existent track
            throw new NoTrackException("No track to remove.");
        }
        TrackPiece after = before.withoutConnection(direction, player.getId()).orElse(null);

        ChangeTrackPieceMove trackChange = new ChangeTrackPieceMove(before, after, location);

        // Removing the station track also removes the station and the schedule stops at it.
        if (trackChange.removesStationTrack()) {
            if (before.playerId() != player.getId()) {
                throw new IllegalStateException("Not allowed to remove another player's station.");
            }
            return new RemoveStationMoveGenerator(trackChange, player).generate(world);
        }
        return trackChange;
    }
}
