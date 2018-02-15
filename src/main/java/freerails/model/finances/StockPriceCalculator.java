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
package freerails.model.finances;

import freerails.model.world.WorldItem;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameTime;
import freerails.model.player.FreerailsPrincipal;

/**
 * Calculates the stock price for each of the players. Stock price = [Net worth +
 * 5 * profit last year] / [ shares owned by public + 0.5 shares owned by other
 * players] Let profit last year = 100,000 in the first year.
 */
public class StockPriceCalculator {

    private final ReadOnlyWorld world;

    /**
     * @param world
     */
    public StockPriceCalculator(ReadOnlyWorld world) {
        this.world = world;
    }

    public static Money calculateStockPrice(long netWorth, long profitLastyear, int publicShares, int otherRRShares) {
        if ((publicShares + otherRRShares) == 0) return new Money(Long.MAX_VALUE);
        long price = 2 * (5 * profitLastyear + netWorth) / (2 * publicShares + otherRRShares);
        return new Money(price);
    }

    /**
     * @return
     */
    public StockPrice[] calculate() {
        StockPrice[] stockPrices = new StockPrice[world.getNumberOfPlayers()];
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
            stockPrices[playerId] = new StockPrice(netWorth, profitLastYear, publicShares, otherRRShares);
        }
        return stockPrices;
    }

    /**
     * Returns true if the current time in the same year as the first
     * transaction for the specified player.
     */
    public boolean isFirstYear(int playerId) {
        FreerailsPrincipal pr = world.getPlayer(playerId).getPrincipal();
        GameTime firstTransactionTime = world.getTransactionTimeStamp(pr, 0);
        GameCalendar calendar = (GameCalendar) world.get(WorldItem.Calendar);
        int year = calendar.getYear(firstTransactionTime.getTicks());
        GameTime currentTime = world.currentTime();
        int currentYear = calendar.getYear(currentTime.getTicks());
        return year == currentYear;
    }

    /**
     * Returns the players networth at the start of this year.
     */
    public long netWorth(int playerId) {
        FreerailsPrincipal principal = world.getPlayer(playerId).getPrincipal();
        NetWorthCalculator netWorthCalculator = new NetWorthCalculator(world, principal);

        // Set the interval to beginning of time to start of this year.
        GameCalendar calendar = (GameCalendar) world.get(WorldItem.Calendar);
        GameTime currentTime = world.currentTime();
        int currentYear = calendar.getYear(currentTime.getTicks());
        int ticksAtStartOfyear = calendar.getTicks(currentYear);
        GameTime[] times = {GameTime.BIG_BANG, new GameTime(ticksAtStartOfyear + 1)};
        netWorthCalculator.setTimes(times);

        // TODO return Money instead
        return netWorthCalculator.calculateValue().amount;
    }

    public long profitsLastYear(int playerId) {
        FreerailsPrincipal pr = world.getPlayer(playerId).getPrincipal();

        GameCalendar calendar = (GameCalendar) world.get(WorldItem.Calendar);
        GameTime currentTime = world.currentTime();
        int currentYear = calendar.getYear(currentTime.getTicks());
        int lastyear = currentYear - 1;
        int ticksAtStartOfyear = calendar.getTicks(currentYear);
        int ticksAtStartOfLastYear = calendar.getTicks(lastyear);
        GameTime[] interval = {new GameTime(ticksAtStartOfLastYear), new GameTime(ticksAtStartOfyear)};

        // TODO is this anonymous class necessary?
        TransactionAggregator aggregator = new TransactionAggregator(world, pr) {
            @Override
            protected boolean condition(int transactionID) {
                Transaction transaction = super.world.getTransaction(super.principal, transactionID);
                return !(transaction instanceof ItemTransaction);
            }
        };
        aggregator.setTimes(interval);
        // TODO return Money instead
        return aggregator.calculateValue().amount;
    }

    private int sharesOwnedByPublic(int playerId) {
        FreerailsPrincipal pr = world.getPlayer(playerId).getPrincipal();
        FinancialDataGatherer gatherer = new FinancialDataGatherer(world, pr);
        return gatherer.sharesHeldByPublic();
    }

    private int sharesOwnedByOtherPlayers(int playerId) {
        FreerailsPrincipal principal = world.getPlayer(playerId).getPrincipal();
        FinancialDataGatherer gatherer = new FinancialDataGatherer(world, principal);
        int[] stakes = gatherer.getStockInThisRRs();
        int total = 0;
        for (int i = 0; i < stakes.length; i++) {
            if (i != playerId) {
                total += stakes[i];
            }
        }
        return total;
    }

}
