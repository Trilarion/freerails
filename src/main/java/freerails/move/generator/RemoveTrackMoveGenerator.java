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
import freerails.model.terrain.TileTransition;
import freerails.model.track.NoTrackException;
import freerails.model.world.UnmodifiableWorld;
import freerails.move.Move;
import freerails.move.RefusedMove;
import freerails.util.IntPoint2D;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

/**
 * Generates the move that removes one track section between two neighboring
 * tiles, with the refund the generating world sets. Removing the player's
 * station track also removes the station.
 *
 * @param from the tile the section starts on
 * @param direction the direction from {@code from} to the neighboring tile
 * @param player the removing player
 */
public record RemoveTrackMoveGenerator(IntPoint2D from, TileTransition direction, Player player)
        implements ClientMoveGenerator {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param player the player the generated move acts for
     * @return this generator removing for the supplied player
     */
    @Override
    public @NotNull RemoveTrackMoveGenerator withPlayer(@NotNull Player player) {
        return new RemoveTrackMoveGenerator(from, direction, player);
    }

    /**
     * @param world the world that sets the refund
     * @return the track removal with its transactions, or a refused move
     */
    @Override
    public Move generate(UnmodifiableWorld world) {
        Move move;
        try {
            move = TrackMoveGenerator.generateRemoveTrackMove(from, direction, world, player);
        } catch (NoTrackException | IllegalStateException e) {
            // No track to remove, or track that must not be removed such as another player's station.
            return new RefusedMove(e.getMessage());
        }
        return new TrackMoveTransactionsGenerator(world, player).addTransactions(move);
    }
}
