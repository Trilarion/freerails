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

package freerails.model.finance.transaction.aggregator;

import freerails.model.finance.transaction.ItemTransaction;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.finance.transaction.aggregator.TransactionAggregator;
import freerails.model.game.Time;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A TransactionAggregator that calculates the net worth of a player by totalling
 * the value of their assets.
 */
public class NetWorthAggregator extends TransactionAggregator {

    /**
     * @param world
     * @param player
     */
    public NetWorthAggregator(UnmodifiableWorld world, Player player, Time[] times) {
        super(world, player, times);
    }

    @Override
    protected boolean acceptable(@NotNull Transaction transaction) {
        if (transaction instanceof ItemTransaction) {
            return transaction.getCategory() == TransactionCategory.ISSUE_STOCK;
            // Since buying something is just converting one asset type to another.
        }

        return true;
    }
}