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
import freerails.model.world.UnmodifiableWorld;
import freerails.move.Move;
import freerails.util.IntPoint2D;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

/**
 * Generates the move that builds one track section between two neighboring
 * tiles, with the cost the generating world sets.
 *
 * @param from the tile the section starts on
 * @param direction the direction from {@code from} to the neighboring tile
 * @param trackTypeA the track type ID used if there is no track on {@code from}
 * @param trackTypeB the track type ID used if there is no track on the neighboring tile
 * @param player the building player
 */
public record BuildTrackMoveGenerator(IntPoint2D from, TileTransition direction, int trackTypeA, int trackTypeB,
                                      Player player) implements ClientMoveGenerator {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param player the player the generated move acts for
     * @return this generator building for the supplied player
     */
    @Override
    public @NotNull BuildTrackMoveGenerator withPlayer(@NotNull Player player) {
        return new BuildTrackMoveGenerator(from, direction, trackTypeA, trackTypeB, player);
    }

    /**
     * @param world the world that sets the cost
     * @return the track change with its cash-constrained cost
     */
    @Override
    public Move generate(UnmodifiableWorld world) {
        Move move = TrackMoveGenerator.generateBuildTrackMove(from, direction,
                world.getTrackType(trackTypeA), world.getTrackType(trackTypeB), world, player);
        return new TrackMoveTransactionsGenerator(world, player).addTransactions(move);
    }
}
