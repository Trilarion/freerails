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

/*
 *
 */
package freerails.model.finance.transaction.aggregator;

import freerails.model.finance.Money;
import freerails.model.finance.transaction.*;
import freerails.model.game.Sentiment;
import freerails.model.game.Time;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Gathers the financial data for a company.
 */
public class FinancialDataAggregator extends TransactionAggregator {

    // TODO use maps instead of arrays
    private final int[] stockInRRs;
    private int totalShares = 100000;
    private int bonds;
    private int[] stockInThisRRs;

    /**
     * Creates an aggregator covering the player's transactions from the start
     * of the game through the current time.
     *
     * @param world world supplying players, clock, and transactions
     * @param player player whose financial data is gathered
     */
    public FinancialDataAggregator(UnmodifiableWorld world, Player player) {
        this(world, player, new Time[]{Time.ZERO, world.getClock().getCurrentTime().advance()});
    }

    /**
     * Creates an aggregator for the specified player and time intervals.
     *
     * @param world world supplying the data
     * @param player player whose financial data is gathered
     * @param times ordered interval boundaries
     */
    public FinancialDataAggregator(UnmodifiableWorld world, Player player, Time[] times) {
        super(world, player, times);
        stockInRRs = new int[world.getPlayers().size()];
    }

    /**
     * Adds the transaction to inherited monetary totals and updates share and
     * bond counts when applicable.
     *
     * @param intervalIndex interval receiving the transaction
     * @param transaction transaction to aggregate
     */
    @Override
    protected void aggregateTransaction(int intervalIndex, Transaction transaction) {
        super.aggregateTransaction(intervalIndex, transaction);
        if (transaction instanceof ItemTransaction itemTransaction) {

            if (itemTransaction.getCategory() == TransactionCategory.ISSUE_STOCK && itemTransaction.getId() == -1) {
                // If it is a change in the total number of shares issued.
                totalShares += itemTransaction.getQuantity();
            } else if (itemTransaction.getCategory() == TransactionCategory.TRANSFER_STOCK) {
                int playerIndex = world.getPlayerIndex(itemTransaction.getId());
                stockInRRs[playerIndex] += itemTransaction.getQuantity();
            } else if (transaction instanceof BondItemTransaction) {
                bonds += itemTransaction.getQuantity();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void clearState() {
        super.clearState();
        bonds = 0;
    }

    /**
     * Returns whether the current bond balance permits issuing another bond.
     *
     * @return {@code true} when the next bond's rate is no more than 7
     */
    public boolean canIssueBond() {
        return nextBondInterestRate() <= 7;
    }

    /**
     * Returns the interest rate for the next bond based on current sentiment.
     *
     * @return next bond interest rate
     */
    public double nextBondInterestRate() {
        Sentiment sentiment = world.getSentiment();
        return bonds + sentiment.getRate();
    }

    /**
     * Returns the number of this railroad's shares held in its own treasury.
     *
     * @return treasury-held share count
     */
    public int treasuryStock() {
        return stockInRRs[world.getPlayerIndex(player.getId())];
    }

    /**
     * Returns the total number of shares issued by this railroad.
     *
     * @return total issued share count
     */
    public int totalShares() {
        return totalShares;
    }

    /**
     * Returns issued shares not held by any railroad.
     *
     * @return publicly held share count
     */
    public int sharesHeldByPublic() {
        int[] stock = getStockInThisRRs();
        int returnValue = totalShares;
        for (int aStock : stock) {
            returnValue -= aStock;
        }
        return returnValue;
    }

    /**
     * Calculates the player's net worth from transactions before the current
     * time.
     *
     * @return current net worth
     */
    public Money netWorth() {
        Time[] times = {Time.ZERO, world.getClock().getCurrentTime()};
        NetWorthAggregator netWorthAggregator = new NetWorthAggregator(world, player, times);
        netWorthAggregator.aggregate();
        return netWorthAggregator.getValues()[0];
    }

    /**
     * Accepts all transactions because this aggregator also tracks item
     * quantities and bond/share state.
     *
     * @param transaction transaction being considered
     * @return always {@code true}
     */
    @Override
    protected boolean acceptable(@NotNull Transaction transaction) {
        // We'll do the work when incrementRunningTotal gets called.
        return true;
    }

    // TODO make this static, compute stockInThisRRs outside
    /**
     * Returns the number of shares in this railroad held by each player,
     * indexed in world player order.
     *
     * @return a copy of the per-player share counts
     */
    public int[] getStockInThisRRs() {
        if (null == stockInThisRRs) {
            stockInThisRRs = new int[world.getPlayers().size()];
            for (int i = 0; i < world.getPlayers().size(); i++) {
                Player player = world.getPlayer(i);
                FinancialDataAggregator temp = new FinancialDataAggregator(world, player);
                temp.aggregate();
                stockInThisRRs[i] = temp.stockInRRs[world.getPlayerIndex(player.getId())];
            }
        }
        return stockInThisRRs.clone();
    }

    /**
     * Returns the number of shares this railroad holds in each railroad,
     * indexed in world player order.
     *
     * @return a copy of the per-railroad share counts
     */
    public int[] getStockInRRs() {
        return stockInRRs.clone();
    }

    /**
     * Returns the number of bonds currently recorded for this player.
     *
     * @return outstanding bond quantity
     */
    public int getBonds() {
        return bonds;
    }
}
