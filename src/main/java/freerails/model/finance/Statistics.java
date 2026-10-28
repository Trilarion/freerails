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

package freerails.model.finance;

import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.finance.transaction.aggregator.ItemsTransactionAggregator;
import freerails.model.game.Time;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;

// TODO The code could maybe become static
/** Computes asset, loan, and equity statistics over a supplied time interval. */
public class Statistics {

    private final Money loans;
    private final Money equity;

    /**
     * Aggregates the player's financial transactions and assets within the
     * supplied interval boundaries.
     *
     * @param world world data used for transaction and asset values
     * @param player player whose statistics are calculated
     * @param totalTimeInterval ordered start and end times defining the interval
     */
    public Statistics(UnmodifiableWorld world, Player player, final Time[] totalTimeInterval) {
        Money track = TransactionUtils.calculateTrackTotal(world, player, totalTimeInterval[0]);

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player, totalTimeInterval);

        aggregator.setCategory(TransactionCategory.STATIONS);
        aggregator.aggregate();
        Money stations = aggregator.getValues()[0];

        aggregator.setCategory(TransactionCategory.TRAIN);
        aggregator.aggregate();
        Money rollingStock = aggregator.getValues()[0];

        aggregator.setCategory(TransactionCategory.INDUSTRIES);
        aggregator.aggregate();
        Money industries = aggregator.getValues()[0];

        aggregator.setCategory(TransactionCategory.BOND);
        aggregator.aggregate();
        loans = aggregator.getValues()[0];

        aggregator.setCategory(TransactionCategory.ISSUE_STOCK);
        aggregator.aggregate();
        equity = aggregator.getValues()[0];

        // If we don't initialize this variable we get a NPE when we don't own any stock in others RRs
        Money otherRrStock = Money.ZERO;

        int thisPlayerIndex = world.getPlayerIndex(player.getId());

        // TODO use maps instead of arrays
        StockPrice[] stockPrices = new StockPriceCalculator(world).calculate();
        int otherPlayerIndex = 0;
        for (Player otherPlayer: world.getPlayers()) {
            aggregator.setCategory(TransactionCategory.TRANSFER_STOCK);
            aggregator.setType(otherPlayer.getId());
            int quantity = aggregator.calculateQuantity();
            if (otherPlayerIndex == thisPlayerIndex) {
                Money treasuryStock = Money.multiply(stockPrices[otherPlayerIndex].currentPrice, quantity);
            } else {
                otherRrStock = Money.add(Money.multiply(stockPrices[otherPlayerIndex].currentPrice, quantity), otherRrStock);
            }
            otherPlayerIndex++;
        }
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
}
