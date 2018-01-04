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

package freerails.controller;

import freerails.controller.StockPriceCalculator.StockPrice;
import freerails.world.*;
import freerails.world.finances.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackRule;

import static freerails.world.finances.TransactionCategory.*;

/**
 * Generates the balance sheet - note, its fields are read using reflection so
 * don't change their names.
 */
// TODO Do not use reflection here.
@SuppressWarnings("unused")
public class BalanceSheetGenerator {

    /**
     *
     */
    public final String year;

    /**
     *
     */
    public final Stats total;

    /**
     *
     */
    public final Stats ytd;
    final ReadOnlyWorld w;
    final FreerailsPrincipal principal;
    GameTime from;
    GameTime to;

    /**
     * @param w
     * @param principal
     */
    public BalanceSheetGenerator(ReadOnlyWorld w, FreerailsPrincipal principal) {
        this.w = w;
        this.principal = principal;
        GameCalendar cal = (GameCalendar) w.get(ITEM.CALENDAR);
        // Calculate totals
        GameTime time = w.currentTime();
        final int startyear = cal.getYear(time.getTicks());
        year = String.valueOf(startyear);
        GameTime startOfYear = new GameTime(cal.getTicks(startyear));

        GameTime[] totalTimeInterval = new GameTime[]{GameTime.BIG_BANG,
                GameTime.DOOMSDAY};

        total = new Stats(w, principal, totalTimeInterval);

        GameTime[] ytdTimeInterval = new GameTime[]{startOfYear,
                GameTime.DOOMSDAY};
        ytd = new Stats(w, principal, ytdTimeInterval);

    }

    /**
     * @param w
     * @param principal
     * @param startTime
     * @return
     */
    public static Money calTrackTotal(ReadOnlyWorld w, FreerailsPrincipal principal, GameTime startTime) {
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
                w, principal);

        aggregator.setCategory(TRACK);
        long amount = 0;

        for (int i = 0; i < w.size(SKEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule) w.get(SKEY.TRACK_RULES, i);
            long trackValue = trackRule.getPrice().getAmount();

            GameTime[] times = new GameTime[]{startTime,
                    GameTime.DOOMSDAY};

            aggregator.setType(i);
            aggregator.setTimes(times);
            ItemsTransactionAggregator.QuantitiesAndValues qnv = aggregator
                    .calculateQuantitiesAndValues();
            int quantity = qnv.quantities[0];
            amount += trackValue * quantity
                    / TrackConfiguration.LENGTH_OF_STRAIGHT_TRACK_PIECE;

        }

        return new Money(amount);
    }

    /**
     *
     */
    public static class Stats {

        /**
         *
         */
        public final Money operatingFunds;

        /**
         *
         */
        public final Money track;

        /**
         *
         */
        public final Money stations;

        /**
         *
         */
        public final Money rollingStock;

        /**
         *
         */
        public final Money industries;

        /**
         *
         */
        public final Money loans;

        /**
         *
         */
        public final Money equity;

        /**
         *
         */
        public Money treasuryStock;

        /**
         *
         */
        public Money otherRrStock;

        /**
         *
         */
        public Money profit;

        /**
         * @param world
         * @param principal
         * @param totalTimeInterval
         */
        public Stats(ReadOnlyWorld world, FreerailsPrincipal principal,
                     final GameTime[] totalTimeInterval) {
            TransactionAggregator operatingFundsAggregator = new TransactionAggregator(
                    world, principal) {
                @Override
                protected boolean condition(int i) {
                    int transactionTicks = w.getTransactionTimeStamp(principal,
                            i).getTicks();

                    int from = totalTimeInterval[0].getTicks();
                    int to = totalTimeInterval[1].getTicks();
                    return transactionTicks >= from && transactionTicks <= to;
                }
            };

            operatingFunds = operatingFundsAggregator.calculateValue();

            track = calTrackTotal(world, principal, totalTimeInterval[0]);

            ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
                    world, principal);
            aggregator.setTimes(totalTimeInterval);

            aggregator.setCategory(STATIONS);
            stations = aggregator.calculateValue();

            aggregator.setCategory(TRAIN);
            rollingStock = aggregator.calculateValue();

            aggregator.setCategory(INDUSTRIES);
            industries = aggregator.calculateValue();
            aggregator.setCategory(BOND);
            loans = aggregator.calculateValue();
            aggregator.setCategory(ISSUE_STOCK);
            equity = aggregator.calculateValue();

            // If we don't initialize this variable
            // we get a NPE when we don't own any stock in others RRs
            otherRrStock = new Money(0);

            int thisPlayerId = world.getID(principal);

            StockPrice[] stockPrices = (new StockPriceCalculator(world))
                    .calculate();
            for (int playerId = 0; playerId < world.getNumberOfPlayers(); playerId++) {

                aggregator.setCategory(TRANSFER_STOCK);
                aggregator.setType(thisPlayerId);
                int quantity = aggregator.calculateQuantity();
                if (playerId == thisPlayerId) {
                    treasuryStock = new Money(quantity
                            * stockPrices[playerId].currentPrice.getAmount());
                } else {
                    otherRrStock = new Money(quantity
                            * stockPrices[playerId].currentPrice.getAmount()
                            + otherRrStock.getAmount());
                }
            }
            calProfit();

        }

        private void calProfit() {
            long profitValue = operatingFunds.getAmount() + track.getAmount()
                    + stations.getAmount() + rollingStock.getAmount()
                    + industries.getAmount() + loans.getAmount()
                    + equity.getAmount() + treasuryStock.getAmount()
                    + otherRrStock.getAmount();
            profit = new Money(profitValue);
        }

    }

}
