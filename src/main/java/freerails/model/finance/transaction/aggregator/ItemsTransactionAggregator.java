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
import freerails.model.game.Time;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;
import org.jetbrains.annotations.NotNull;

/**
 * Adds up the number of assets.
 */
public class ItemsTransactionAggregator extends TransactionAggregator {

    private static final int ANY_VALUE = Integer.MIN_VALUE;
    private int type = ANY_VALUE;
    private TransactionCategory category = null;
    private int[] quantities;

    /**
     * @param world
     * @param player
     */
    public ItemsTransactionAggregator(UnmodifiableWorld world, Player player, Time[] times) {
        super(world, player, times);
        quantities = new int[times.length - 1];
    }

    /**
     * Returns true if the transaction with the specified ID has an acceptable
     * type and category.
     */
    @Override
    protected boolean acceptable(@NotNull Transaction transaction) {
        if (!(transaction instanceof ItemTransaction)) {
            return false;
        }

        ItemTransaction itemTransaction = (ItemTransaction) transaction;
        boolean isTypeAcceptable = (type == ANY_VALUE) || (type == itemTransaction.getId());
        boolean isCategoryAcceptable = (category == null) || (category == itemTransaction.getCategory());

        return isCategoryAcceptable && isTypeAcceptable;
    }

    /**
     * @return
     */
    public int calculateQuantity() {
        super.aggregate();
        return quantities[0];
    }

    protected void clearState() {
        super.clearState();
        // initialize output
        for (int i = 0; i < times.length - 1; i++) {
            quantities[i] = 0;
        }
    }

    @Override
    protected void aggregateTransaction(int intervalIndex, Transaction transaction) {
        super.aggregateTransaction(intervalIndex, transaction);
        ItemTransaction itemTransaction = (ItemTransaction) transaction;
        quantities[intervalIndex] += itemTransaction.getQuantity();
    }

    /**
     * @param category
     */
    public void setCategory(TransactionCategory category) {
        this.category = category;
    }

    /**
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

}