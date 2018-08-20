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

package freerails.model.finance;

import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.finance.transaction.aggregator.ItemsTransactionAggregator;
import freerails.model.game.Time;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;

// TODO The code could maybe become static
/**
 *
 */
public class Statistics {

    private final Money loans;
    private final Money equity;

    /**
     * @param world
     * @param player
     * @param totalTimeInterval
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

        int thisPlayerId = player.getId();

        // TODO use maps instead of arrays
        StockPrice[] stockPrices = (new StockPriceCalculator(world)).calculate();
        for (Player otherPlayer: world.getPlayers()) {
            aggregator.setCategory(TransactionCategory.TRANSFER_STOCK);
            aggregator.setType(thisPlayerId);
            int quantity = aggregator.calculateQuantity();
            if (otherPlayer.getId() == thisPlayerId) {
                Money treasuryStock = Money.multiply(stockPrices[otherPlayer.getId()].currentPrice, quantity);
            } else {
                otherRrStock = Money.add(Money.multiply(stockPrices[otherPlayer.getId()].currentPrice, quantity), otherRrStock);
            }
        }
    }

    public Money getLoans() {
        return loans;
    }

    public Money getEquity() {
        return equity;
    }
}
