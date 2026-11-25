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
 * Generates the move that issues one bond at the interest rate the generating
 * world sets (see {@link Broker}).
 *
 * @param player the issuing player
 */
public record IssueBondMoveGenerator(Player player) implements ClientMoveGenerator {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param player the player the generated move acts for
     * @return this generator issuing a bond for the supplied player
     */
    @Override
    public @NotNull IssueBondMoveGenerator withPlayer(@NotNull Player player) {
        return new IssueBondMoveGenerator(player);
    }

    /**
     * @param world the world that sets the interest rate
     * @return the bond transaction, or a refused move
     */
    @Override
    public Move generate(UnmodifiableWorld world) {
        Optional<String> refusal = Broker.checkIssueBond(world, player);
        if (refusal.isPresent()) {
            return new RefusedMove(refusal.get());
        }
        return new AddTransactionMove(player, Broker.issueBond(world, player), true);
    }
}
