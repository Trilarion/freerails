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
package freerails.model.finance;

import freerails.model.finance.transaction.ItemTransaction;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.aggregator.FinancialDataAggregator;
import freerails.model.finance.transaction.aggregator.NetWorthAggregator;
import freerails.model.finance.transaction.aggregator.TransactionAggregator;
import freerails.model.game.Clock;
import freerails.model.game.Time;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import org.jetbrains.annotations.NotNull;

// TODO StockPrice seems to contain its own calculation model
/**
 * Calculates the stock price for each of the players.
 *
 * Stock price = [Net worth + 5 * profit last year] / [ shares owned by public + 0.5 shares owned by other players]
 *
 * Let profit last year = 100,000 in the first year.
 */
public class StockPriceCalculator {

    private final UnmodifiableWorld world;

    /**
     * @param world
     */
    public StockPriceCalculator(UnmodifiableWorld world) {
        this.world = world;
    }

    /**
     * @return
     */
    public StockPrice[] calculate() {
        // TODO use maps instead of arrays
        StockPrice[] stockPrices = new StockPrice[world.getPlayers().size()];
        for (int playerId = 0; playerId < stockPrices.length; playerId++) {
            Money profitLastYear;
            if (isFirstYear(playerId)) {
                // TODO is this a good choice (maybe scenario dependent)
                profitLastYear = new Money(100000);
            } else {
                profitLastYear = profitsLastYear(playerId);
            }
            Money netWorth = netWorth(playerId);
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
    boolean isFirstYear(int playerId) {
        Player player = world.getPlayer(playerId);
        Time firstTransactionTime = world.getTransaction(player, 0).getTime();
        Clock clock = world.getClock();
        int year = clock.getYear(firstTransactionTime);
        int currentYear = clock.getCurrentYear();
        return year == currentYear;
    }

    /**
     * Returns the players net worth at the start of this year.
     */
    public Money netWorth(int playerId) {
        // Set the interval to beginning of time to start of this year.
        Clock clock = world.getClock();
        Time[] times = {Time.ZERO, new Time(clock.getTimeAtStartOfCurrentYear(),1)};

        Player player = world.getPlayer(playerId);
        NetWorthAggregator netWorthAggregator = new NetWorthAggregator(world, player, times);
        netWorthAggregator.aggregate();
        return netWorthAggregator.getValues()[0];
    }

    // TODO make this a static function of FinanceUtils
    public Money profitsLastYear(int playerId) {
        Player player = world.getPlayer(playerId);

        Clock clock = world.getClock();
        Time[] interval = {clock.getTimeAtStartOfYear(clock.getCurrentYear() - 1), clock.getTimeAtStartOfCurrentYear()};

        // TODO is this anonymous class necessary?
        TransactionAggregator aggregator = new TransactionAggregator(world, player, interval) {
            @Override
            protected boolean acceptable(@NotNull Transaction transaction) {
                return !(transaction instanceof ItemTransaction);
            }
        };
        aggregator.aggregate();
        return aggregator.getValues()[0];
    }

    private int sharesOwnedByPublic(int playerId) {
        Player pr = world.getPlayer(playerId);
        FinancialDataAggregator gatherer = new FinancialDataAggregator(world, pr);
        return gatherer.sharesHeldByPublic();
    }

    private int sharesOwnedByOtherPlayers(int playerId) {
        Player player = world.getPlayer(playerId);
        FinancialDataAggregator gatherer = new FinancialDataAggregator(world, player);
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
