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

package freerails.model.statistics;

import freerails.model.ModelConstants;
import freerails.model.finances.*;
import freerails.model.finances.transactions.TransactionCategory;
import freerails.model.track.TrackType;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.game.Time;
import freerails.model.player.Player;

/**
 *
 */
public class Statistics {

    public final Money loans;
    public final Money equity;

    /**
     * @param world
     * @param player
     * @param totalTimeInterval
     */
    public Statistics(UnmodifiableWorld world, Player player, final Time[] totalTimeInterval) {
        Money track = calculateTrackTotal(world, player, totalTimeInterval[0]);

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player);
        aggregator.setTimes(totalTimeInterval);

        aggregator.setCategory(TransactionCategory.STATIONS);
        Money stations = aggregator.calculateValue();

        aggregator.setCategory(TransactionCategory.TRAIN);
        Money rollingStock = aggregator.calculateValue();

        aggregator.setCategory(TransactionCategory.INDUSTRIES);
        Money industries = aggregator.calculateValue();
        aggregator.setCategory(TransactionCategory.BOND);
        loans = aggregator.calculateValue();
        aggregator.setCategory(TransactionCategory.ISSUE_STOCK);
        equity = aggregator.calculateValue();

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

    /**
     * @param world
     * @param player
     * @param startTime
     * @return
     */
    public static Money calculateTrackTotal(UnmodifiableWorld world, Player player, Time startTime) {

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player);
        aggregator.setCategory(TransactionCategory.TRACK);
        long amount = 0;

        for (TrackType trackType: world.getTrackTypes()) {
            // TODO Money arithmetic
            long trackValue = trackType.getPurchasingPrice().amount;

            Time[] times = new Time[]{startTime, Time.DOOMSDAY};

            aggregator.setType(trackType.getId());
            aggregator.setTimes(times);
            int quantity = aggregator.calculateQuantity();
            amount += trackValue * quantity / ModelConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE;
        }

        return new Money(amount);
    }
}
