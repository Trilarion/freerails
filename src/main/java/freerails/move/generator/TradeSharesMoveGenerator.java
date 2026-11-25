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

import freerails.model.economy.Broker;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;
import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.move.RefusedMove;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.Optional;

/**
 * Generates the move that buys or sells a bundle of a railroad's shares at the
 * price the generating world sets (see {@link Broker}).
 *
 * @param railroadId the player ID of the railroad whose shares are traded
 * @param quantity the number of shares, positive to buy and negative to sell
 * @param player the trading player
 */
public record TradeSharesMoveGenerator(int railroadId, int quantity, Player player) implements ClientMoveGenerator {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param player the player the generated move acts for
     * @return this generator trading for the supplied player
     */
    @Override
    public @NotNull TradeSharesMoveGenerator withPlayer(@NotNull Player player) {
        return new TradeSharesMoveGenerator(railroadId, quantity, player);
    }

    /**
     * @param world the world that sets the price
     * @return the cash-constrained share transaction, or a refused move
     */
    @Override
    public Move generate(UnmodifiableWorld world) {
        Optional<String> refusal = Broker.checkShareTrade(world, player, railroadId, quantity);
        if (refusal.isPresent()) {
            return new RefusedMove(refusal.get());
        }
        return new AddTransactionMove(player, Broker.shareTrade(world, player, railroadId, quantity), true);
    }
}
