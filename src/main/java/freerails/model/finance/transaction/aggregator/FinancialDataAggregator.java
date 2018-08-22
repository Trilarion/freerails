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
     * Convenience method.
     * @param world
     * @param player
     */
    public FinancialDataAggregator(UnmodifiableWorld world, Player player) {
        this(world, player, new Time[]{Time.ZERO, world.getClock().getCurrentTime().advance()});
    }

    /**
     * @param world
     * @param player
     */
    public FinancialDataAggregator(UnmodifiableWorld world, Player player, Time[] times) {
        super(world, player, times);
        stockInRRs = new int[world.getPlayers().size()];
    }

    /**
     * @param transaction
     */
    @Override
    protected void aggregateTransaction(int intervalIndex, Transaction transaction) {
        super.aggregateTransaction(intervalIndex, transaction);
        if (transaction instanceof ItemTransaction) {
            ItemTransaction itemTransaction = (ItemTransaction) transaction;

            if (itemTransaction.getCategory() == TransactionCategory.ISSUE_STOCK && itemTransaction.getId() == -1) {
                // If it is a change in the total number of shares issued.
                totalShares += itemTransaction.getQuantity();
            } else if (itemTransaction.getCategory() == TransactionCategory.TRANSFER_STOCK) {
                stockInRRs[itemTransaction.getId()] += itemTransaction.getQuantity();
            } else if (transaction instanceof BondItemTransaction) {
                bonds += itemTransaction.getQuantity();
            }
        }
    }

    @Override
    protected void clearState() {
        super.clearState();
        bonds = 0;
    }

    /**
     * @return
     */
    public boolean canIssueBond() {
        return nextBondInterestRate() <= 7;
    }

    /**
     * @return
     */
    public double nextBondInterestRate() {
        Sentiment sentiment = world.getSentiment();
        return bonds + sentiment.getRate();
    }

    /**
     * Returns the number of stock in the Treasury
     */
    public int treasuryStock() {
        return stockInRRs[player.getId()];
    }

    /**
     * Returns The number of open Shares
     */
    public int totalShares() {
        return totalShares;
    }

    /**
     * @return
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
     * @return
     */
    public Money netWorth() {
        Time[] times = {Time.ZERO, world.getClock().getCurrentTime()};
        NetWorthAggregator netWorthAggregator = new NetWorthAggregator(world, player, times);
        netWorthAggregator.aggregate();
        return netWorthAggregator.getValues()[0];
    }

    @Override
    protected boolean acceptable(@NotNull Transaction transaction) {
        // We'll do the work when incrementRunningTotal gets called.
        return true;
    }

    // TODO make this static, compute stockInThisRRs outside
    /**
     * @return
     */
    public int[] getStockInThisRRs() {
        if (null == stockInThisRRs) {
            stockInThisRRs = new int[world.getPlayers().size()];
            for (int i = 0; i < world.getPlayers().size(); i++) {
                Player player = world.getPlayer(i);
                FinancialDataAggregator temp = new FinancialDataAggregator(world, player);
                temp.aggregate();
                stockInThisRRs[i] = temp.stockInRRs[player.getId()];
            }
        }
        return stockInThisRRs;
    }

    /**
     * @return
     */
    public int[] getStockInRRs() {
        return stockInRRs;
    }

    /**
     * @return
     */
    public int getBonds() {
        return bonds;
    }
}
