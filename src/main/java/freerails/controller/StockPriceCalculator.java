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
 * Created on 19-Sep-2005
 *
 */
package freerails.controller;

import freerails.world.common.GameCalendar;
import freerails.world.common.GameTime;
import freerails.world.finances.AddItemTransaction;
import freerails.world.finances.Money;
import freerails.world.finances.Transaction;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ITEM;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.TransactionAggregator;

import static freerails.world.finances.StockTransaction.STOCK_BUNDLE_SIZE;

/**
 * Calculates the stock price for each of the players. Stock price = [Net worth +
 * 5 * profit last year] / [ shares owned by public + 0.5 shares owned by other
 * players] Let profit last year = 100,000 in the first year.
 */
public class StockPriceCalculator {
    private final ReadOnlyWorld w;

    /**
     * @param w
     */
    public StockPriceCalculator(ReadOnlyWorld w) {
        this.w = w;
    }

    static Money calStockPrice(long netWorth, long profitLastyear,
                               int publicShares, int otherRRShares) {
        if ((publicShares + otherRRShares) == 0)
            return new Money(Long.MAX_VALUE);
        long price = 2 * (5 * profitLastyear + netWorth)
                / (2 * publicShares + otherRRShares);
        return new Money(price);
    }

    /**
     * @return
     */
    public StockPrice[] calculate() {
        StockPrice[] stockPrices = new StockPrice[w.getNumberOfPlayers()];
        for (int playerId = 0; playerId < stockPrices.length; playerId++) {
            long profitLastYear;
            if (isFirstYear(playerId)) {
                profitLastYear = 100000;
            } else {
                profitLastYear = profitsLastYear(playerId);
            }
            long netWorth = netWorth(playerId);
            int publicShares = sharesOwnedByPublic(playerId);
            int otherRRShares = sharesOwnedByOtherPlayers(playerId);
            stockPrices[playerId] = new StockPrice(netWorth, profitLastYear,
                    publicShares, otherRRShares);
        }
        return stockPrices;
    }

    /**
     * Returns true if the current time in the same year as the first
     * transaction for the specified player.
     */
    boolean isFirstYear(int playerId) {
        FreerailsPrincipal pr = w.getPlayer(playerId).getPrincipal();
        GameTime firstTransactionTime = w.getTransactionTimeStamp(pr, 0);
        GameCalendar calendar = (GameCalendar) w.get(ITEM.CALENDAR);
        int year = calendar.getYear(firstTransactionTime.getTicks());
        GameTime currentTime = w.currentTime();
        int currentYear = calendar.getYear(currentTime.getTicks());
        return year == currentYear;
    }

    /**
     * Returns the players networth at the start of this year.
     */
    long netWorth(int playerId) {
        FreerailsPrincipal pr = w.getPlayer(playerId).getPrincipal();
        NetWorthCalculator nwc = new NetWorthCalculator(w, pr);

        // Set the inteval to beginning of time to start of this year.
        GameCalendar calendar = (GameCalendar) w.get(ITEM.CALENDAR);
        GameTime currentTime = w.currentTime();
        int currentYear = calendar.getYear(currentTime.getTicks());
        int ticksAtStartOfyear = calendar.getTicks(currentYear);
        GameTime[] times = {GameTime.BIG_BANG,
                new GameTime(ticksAtStartOfyear + 1)};
        nwc.setTimes(times);

        return nwc.calculateValue().getAmount();
    }

    long profitsLastYear(int playerId) {
        FreerailsPrincipal pr = w.getPlayer(playerId).getPrincipal();

        GameCalendar calendar = (GameCalendar) w.get(ITEM.CALENDAR);
        GameTime currentTime = w.currentTime();
        int currentYear = calendar.getYear(currentTime.getTicks());
        int lastyear = currentYear - 1;
        int ticksAtStartOfyear = calendar.getTicks(currentYear);
        int ticksAtStartOfLastYear = calendar.getTicks(lastyear);
        GameTime[] inteval = {new GameTime(ticksAtStartOfLastYear),
                new GameTime(ticksAtStartOfyear)};

        TransactionAggregator aggregator = new TransactionAggregator(w, pr) {
            @Override
            protected boolean condition(int transactionID) {
                Transaction t = super.w.getTransaction(super.principal,
                        transactionID);
                return !(t instanceof AddItemTransaction);
            }
        };
        aggregator.setTimes(inteval);
        return aggregator.calculateValue().getAmount();
    }

    int sharesOwnedByPublic(int playerId) {
        FreerailsPrincipal pr = w.getPlayer(playerId).getPrincipal();
        FinancialDataGatherer gatherer = new FinancialDataGatherer(w, pr);
        return gatherer.sharesHeldByPublic();
    }

    int sharesOwnedByOtherPlayers(int playerId) {
        FreerailsPrincipal pr = w.getPlayer(playerId).getPrincipal();
        FinancialDataGatherer gatherer = new FinancialDataGatherer(w, pr);
        int[] stakes = gatherer.getStockInThisRRs();
        int total = 0;
        for (int i = 0; i < stakes.length; i++) {
            if (i != playerId) {
                total += stakes[i];
            }
        }
        return total;
    }

    /**
     *
     */
    public static class StockPrice {

        /**
         *
         */
        public final Money currentPrice;

        /**
         *
         */
        public final Money sellPrice;

        /**
         *
         */
        public final Money buyPrice;

        /**
         *
         */
        public final Money treasuryBuyPrice;

        /**
         *
         */
        public final Money treasurySellPrice;

        /**
         * @param netWorth
         * @param profitLastyear
         * @param publicShares
         * @param otherRRShares
         */
        public StockPrice(long netWorth, long profitLastyear, int publicShares,
                          int otherRRShares) {
            currentPrice = calStockPrice(netWorth, profitLastyear,
                    publicShares, otherRRShares);
            sellPrice = calStockPrice(netWorth, profitLastyear, publicShares
                    + STOCK_BUNDLE_SIZE, otherRRShares - STOCK_BUNDLE_SIZE);
            buyPrice = calStockPrice(netWorth, profitLastyear, publicShares
                    - STOCK_BUNDLE_SIZE, otherRRShares + STOCK_BUNDLE_SIZE);
            treasurySellPrice = calStockPrice(netWorth, profitLastyear,
                    publicShares + STOCK_BUNDLE_SIZE, otherRRShares);
            treasuryBuyPrice = calStockPrice(netWorth, profitLastyear,
                    publicShares - STOCK_BUNDLE_SIZE, otherRRShares);
        }
    }

}
