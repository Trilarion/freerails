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
import freerails.model.world.UnmodifiableWorld;
import freerails.move.Move;
import freerails.move.RefusedMove;
import freerails.util.IntPoint2D;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

/**
 * Generates the move that changes the track type on a tile, with the cost the
 * generating world sets.
 *
 * @param location the tile to upgrade
 * @param trackTypeId the new track type ID
 * @param player the upgrading player
 */
public record UpgradeTrackMoveGenerator(IntPoint2D location, int trackTypeId, Player player)
        implements ClientMoveGenerator {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param player the player the generated move acts for
     * @return this generator upgrading for the supplied player
     */
    @Override
    public @NotNull UpgradeTrackMoveGenerator withPlayer(@NotNull Player player) {
        return new UpgradeTrackMoveGenerator(location, trackTypeId, player);
    }

    /**
     * @param world the world that sets the cost
     * @return the upgrade with its cash-constrained cost, or a refused move
     */
    @Override
    public Move generate(UnmodifiableWorld world) {
        Move move;
        try {
            move = TrackMoveGenerator.generateUpgradeTrackMove(location, world.getTrackType(trackTypeId), world, player);
        } catch (NoTrackException | IllegalStateException e) {
            // No track to upgrade, or station track, which is never upgraded.
            return new RefusedMove(e.getMessage());
        }
        return new TrackMoveTransactionsGenerator(world, player).addTransactions(move);
    }
}
