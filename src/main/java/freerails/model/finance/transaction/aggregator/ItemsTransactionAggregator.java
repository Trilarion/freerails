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

package freerails.model.finance.transaction.aggregator;

import freerails.model.finance.transaction.ItemTransaction;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.game.Time;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;
import org.jetbrains.annotations.NotNull;

/** Aggregates signed quantities of item transactions by category and type. */
public class ItemsTransactionAggregator extends TransactionAggregator {

    private static final int ANY_VALUE = Integer.MIN_VALUE;
    private int type = ANY_VALUE;
    private TransactionCategory category = null;
    private int[] quantities;

    /**
     * Creates an item aggregator over the specified time intervals.
     *
     * @param world world supplying transactions
     * @param player player whose item transactions are aggregated
     * @param times ordered interval boundaries
     */
    public ItemsTransactionAggregator(UnmodifiableWorld world, Player player, Time[] times) {
        super(world, player, times);
        quantities = new int[times.length - 1];
    }

    /**
     * Accepts item transactions matching the configured category and type,
     * with either filter allowed to be unrestricted.
     *
     * @param transaction transaction to test
     * @return {@code true} when it matches the configured filters
     */
    @Override
    protected boolean acceptable(@NotNull Transaction transaction) {
        if (!(transaction instanceof ItemTransaction itemTransaction)) {
            return false;
        }

        boolean isTypeAcceptable = type == ANY_VALUE || type == itemTransaction.getId();
        boolean isCategoryAcceptable = category == null || category == itemTransaction.getCategory();

        return isCategoryAcceptable && isTypeAcceptable;
    }

    /**
     * Aggregates transactions and returns the quantity in the first interval.
     *
     * @return quantity total for the first interval
     */
    public int calculateQuantity() {
        super.aggregate();
        return quantities[0];
    }

    /** {@inheritDoc} */
    @Override
    protected void clearState() {
        super.clearState();
        // initialize output
        for (int i = 0; i < times.length - 1; i++) {
            quantities[i] = 0;
        }
    }

    /**
     * Adds an accepted item transaction's quantity to its interval total.
     *
     * @param intervalIndex interval receiving the transaction
     * @param transaction accepted item transaction
     */
    @Override
    protected void aggregateTransaction(int intervalIndex, Transaction transaction) {
        super.aggregateTransaction(intervalIndex, transaction);
        ItemTransaction itemTransaction = (ItemTransaction) transaction;
        quantities[intervalIndex] += itemTransaction.getQuantity();
    }

    /**
     * Restricts aggregation to the supplied category; {@code null} accepts
     * every category.
     *
     * @param category category to include, or {@code null} for all categories
     */
    public void setCategory(TransactionCategory category) {
        this.category = category;
    }

    /**
     * Restricts aggregation to the supplied item identifier; the sentinel
     * {@code Integer.MIN_VALUE} accepts every identifier.
     *
     * @param type item or player identifier to include
     */
    public void setType(int type) {
        this.type = type;
    }

}