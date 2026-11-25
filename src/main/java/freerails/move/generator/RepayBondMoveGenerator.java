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
 * Generates the move that repays one bond (see {@link Broker}).
 *
 * @param player the repaying player
 */
public record RepayBondMoveGenerator(Player player) implements ClientMoveGenerator {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param player the player the generated move acts for
     * @return this generator repaying a bond of the supplied player
     */
    @Override
    public @NotNull RepayBondMoveGenerator withPlayer(@NotNull Player player) {
        return new RepayBondMoveGenerator(player);
    }

    /**
     * @param world the world
     * @return the cash-constrained bond transaction, or a refused move
     */
    @Override
    public Move generate(UnmodifiableWorld world) {
        Optional<String> refusal = Broker.checkRepayBond(world, player);
        if (refusal.isPresent()) {
            return new RefusedMove(refusal.get());
        }
        return new AddTransactionMove(player, Broker.repayBond(world, player), true);
    }
}
