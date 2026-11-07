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

package freerails.model.economy.transaction.aggregator;

import freerails.model.economy.transaction.ItemTransaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.simulation.Time;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;

/**
 * A TransactionAggregator that calculates the net worth of a player by totalling
 * the value of their assets.
 */
public class NetWorthAggregator extends TransactionAggregator {

    /**
     * Creates an aggregator that calculates a player's net worth across the
     * specified intervals.
     *
     * @param world world supplying the player's transactions
     * @param player player whose net worth is calculated
     * @param times ordered interval boundaries
     */
    public NetWorthAggregator(UnmodifiableWorld world, Player player, Time[] times) {
        super(world, player, times, transaction -> {
            if (transaction instanceof ItemTransaction) {
                return transaction.getCategory() == TransactionCategory.ISSUE_STOCK;
                // Since buying something is just converting one asset type to another.
            }

            return true;
        });
    }
}