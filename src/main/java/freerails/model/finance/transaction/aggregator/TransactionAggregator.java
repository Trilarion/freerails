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

import freerails.model.finance.Money;
import freerails.model.finance.transaction.Transaction;
import freerails.model.game.Time;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;
import org.jetbrains.annotations.NotNull;

// TODO just make AggregationCondition an interface and this class not abstract but using a certain Condition
/**
 * Adds up the value of transactions. Implements GoF Template Method pattern.
 * Subclasses that aggregate a monetary sum should only override the method
 * {@code condition(int)}; subclasses that aggregate a non-monetary sum
 * should override all 4 protected methods.
 */
public abstract class TransactionAggregator {

    protected final UnmodifiableWorld world;
    protected final Player player;
    private final Money[] values;
    protected final Time[] times;

    /**
     * Creates an aggregator whose intervals are defined by the supplied
     * nondecreasing boundary times.
     *
     * @param world world supplying transactions
     * @param player player whose transactions are aggregated
     * @param times ordered boundaries defining one or more intervals
     * @throws IllegalArgumentException if fewer than two boundaries are given
     *         or if the boundaries decrease
     */
    public TransactionAggregator(UnmodifiableWorld world, Player player, Time[] times) {
        this.world = world;
        this.player = player;

        if (times.length < 2) {
            throw new IllegalArgumentException("There must be at least two values.");
        }

        // check that times are not decreasing
        for (int i = 1; i < times.length; i++) {
            if (times[i].compareTo(times[i-1]) < 0) {
                throw new IllegalArgumentException("Clock.Time at index " + i + " < time at index " + (i - 1) + '.');
            }
        }

        // copy the array
        this.times = times.clone();

        // set up values array
        values = new Money[times.length - 1];
    }

    // TODO this exploits/assumes that the transactions are ordered in time
    /**
     * Clears prior totals and sums accepted transactions into the interval
     * containing each transaction time. Intervals include their lower bound
     * and exclude their upper bound.
     */
    public final void aggregate() {
        // clear state before aggregating
        clearState();

        // start with the first interval
        int intervalIndex = 0;

        // loop over all transactions (they are ordered in time)
        for (Transaction transaction: world.getTransactions(player)) {

            // filter out not acceptable transactions
            if (!acceptable(transaction)) {
                continue;
            }

            // check if transaction time is earlier than left interval border, ignore transaction (too early)
            if (transaction.getTime().compareTo(times[intervalIndex]) < 0) {
                continue;
            }

            // advance interval index until right interval border is later than transaction time
            while (intervalIndex  + 1 < times.length && times[intervalIndex+1].compareTo(transaction.getTime()) <= 0) {
                intervalIndex++;
            }

            // if we are at the end of the intervals, there is no transaction coming any more
            if (intervalIndex + 1 == times.length) {
                return;
            }

            // aggregate transaction values
            aggregateTransaction(intervalIndex, transaction);
        }
    }

    /** Resets all interval totals to zero before an aggregation pass. */
    protected void clearState() {
        // initialize output
        for (int i = 0; i < times.length - 1; i++) {
            values[i] = Money.ZERO;
        }
    }

    /**
     * Returns the monetary total for each interval.
     *
     * @return a copy of the interval totals
     */
    public Money[] getValues() {
        return values.clone();
    }

    /**
     * Adds a transaction's amount to the specified interval.
     *
     * @param intervalIndex index of the interval receiving the transaction
     * @param transaction transaction whose amount is added
     */
    protected void aggregateTransaction(int intervalIndex, Transaction transaction) {
        values[intervalIndex] = Money.add(values[intervalIndex], transaction.getAmount());
    }

    /**
     * Determines whether a transaction should contribute to the aggregation.
     *
     * @param transaction transaction to test
     * @return {@code true} if it should be included
     */
    protected abstract boolean acceptable(@NotNull Transaction transaction);
}