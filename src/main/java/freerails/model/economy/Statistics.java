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

package freerails.model.economy;

import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.aggregator.ItemsTransactionAggregator;
import freerails.model.economy.transaction.aggregator.TransactionAggregator;
import freerails.model.simulation.Time;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import org.jetbrains.annotations.NotNull;

// TODO The code could maybe become static
/** Computes asset, loan, and equity statistics over a supplied time interval. */
public class Statistics {

    private final Money operatingFunds;
    private final Money track;
    private final Money stations;
    private final Money rollingStock;
    private final Money industries;
    private final Money treasuryStock;
    private final Money otherRrStock;
    private final Money loans;
    private final Money equity;
    private final Money profit;

    /**
     * Aggregates the player's financial transactions and assets within the
     * supplied interval boundaries.
     *
     * @param world world data used for transaction and asset values
     * @param player player whose statistics are calculated
     * @param totalTimeInterval ordered start and end times defining the interval
     */
    public Statistics(UnmodifiableWorld world, Player player, final Time[] totalTimeInterval) {
        Time startTime = totalTimeInterval[0];
        Time endTime = totalTimeInterval[1];
        operatingFunds = calculateOperatingFunds(world, player, startTime, endTime);
        track = TransactionUtils.calculateTrackTotal(world, player, startTime);
        stations = Money.opposite(calculateCategoryTotal(
                world, player, totalTimeInterval, TransactionCategory.STATIONS));
        rollingStock = Money.opposite(calculateCategoryTotal(
                world, player, totalTimeInterval, TransactionCategory.TRAIN));
        industries = Money.opposite(calculateCategoryTotal(
                world, player, totalTimeInterval, TransactionCategory.INDUSTRIES));
        loans = calculateCategoryTotal(world, player, totalTimeInterval, TransactionCategory.BOND);
        equity = calculateCategoryTotal(world, player, totalTimeInterval, TransactionCategory.ISSUE_STOCK);

        StockPrice[] stockPrices = new StockPriceCalculator(world).calculate();
        ItemsTransactionAggregator stockAggregator = new ItemsTransactionAggregator(world, player, totalTimeInterval);
        stockAggregator.setCategory(TransactionCategory.TRANSFER_STOCK);
        Money treasuryValue = Money.ZERO;
        Money otherStockValue = Money.ZERO;
        for (int i = 0; i < world.getPlayers().size(); i++) {
            Player company = world.getPlayer(i);
            stockAggregator.setType(company.getId());
            int quantity = stockAggregator.calculateQuantity();
            Money stockValue = Money.multiply(stockPrices[i].currentPrice, quantity);
            if (company.getId() == player.getId()) {
                treasuryValue = stockValue;
            } else {
                otherStockValue = Money.add(otherStockValue, stockValue);
            }
        }
        treasuryStock = treasuryValue;
        otherRrStock = otherStockValue;

        Money assets = Money.add(operatingFunds, track);
        assets = Money.add(assets, stations);
        assets = Money.add(assets, rollingStock);
        assets = Money.add(assets, industries);
        assets = Money.add(assets, treasuryStock);
        assets = Money.add(assets, otherRrStock);
        profit = Money.subtract(Money.subtract(assets, loans), equity);
    }

    /**
     * Adds the monetary transactions in one category over the supplied interval.
     *
     * @param world world supplying transactions
     * @param player account owner
     * @param interval interval boundaries
     * @param category category to aggregate
     * @return the category's net monetary value
     */
    private static Money calculateCategoryTotal(
            UnmodifiableWorld world, Player player, Time[] interval, TransactionCategory category) {
        TransactionAggregator aggregator = new TransactionAggregator(world, player, interval) {
            /** {@inheritDoc} */
            @Override
            protected boolean acceptable(@NotNull Transaction transaction) {
                return transaction.getCategory() == category;
            }
        };
        aggregator.aggregate();
        return aggregator.getValues()[0];
    }

    /**
     * Calculates cash movement within the supplied interval.
     *
     * @param world world supplying player transactions
     * @param player account owner
     * @param startTime inclusive interval start
     * @param endTime exclusive interval end
     * @return net cash movement during the interval
     */
    private static Money calculateOperatingFunds(
            UnmodifiableWorld world, Player player, Time startTime, Time endTime) {
        TransactionAggregator aggregator = new TransactionAggregator(world, player, new Time[]{startTime, endTime}) {
            /** {@inheritDoc} */
            @Override
            protected boolean acceptable(@NotNull Transaction transaction) {
                return true;
            }
        };
        aggregator.aggregate();
        return aggregator.getValues()[0];
    }

    /**
     * Returns operating funds or cash movement for this interval.
     *
     * @return operating funds
     */
    public Money getOperatingFunds() {
        return operatingFunds;
    }

    /**
     * Returns track value or its change for this interval.
     *
     * @return track value
     */
    public Money getTrack() {
        return track;
    }

    /**
     * Returns station value or its change for this interval.
     *
     * @return station value
     */
    public Money getStations() {
        return stations;
    }

    /**
     * Returns rolling-stock value or its change for this interval.
     *
     * @return rolling-stock value
     */
    public Money getRollingStock() {
        return rollingStock;
    }

    /**
     * Returns industry value or its change for this interval.
     *
     * @return industry value
     */
    public Money getIndustries() {
        return industries;
    }

    /**
     * Returns treasury-stock value or its change for this interval.
     *
     * @return treasury-stock value
     */
    public Money getTreasuryStock() {
        return treasuryStock;
    }

    /**
     * Returns the value of other railroad shares or its change for this interval.
     *
     * @return other railroad stock value
     */
    public Money getOtherRrStock() {
        return otherRrStock;
    }

    /**
     * Returns the outstanding value of bonds in the interval.
     *
     * @return aggregated bond value
     */
    public Money getLoans() {
        return loans;
    }

    /**
     * Returns the value of issued equity in the interval.
     *
     * @return aggregated equity value
     */
    public Money getEquity() {
        return equity;
    }

    /**
     * Returns the calculated profit or loss for this interval.
     *
     * @return profit or loss
     */
    public Money getProfit() {
        return profit;
    }

}
