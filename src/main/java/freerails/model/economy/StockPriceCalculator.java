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
package freerails.model.economy;

import freerails.model.ModelConstants;
import freerails.model.economy.transaction.ItemTransaction;
import freerails.model.economy.transaction.aggregator.FinancialDataAggregator;
import freerails.model.economy.transaction.aggregator.NetWorthAggregator;
import freerails.model.economy.transaction.aggregator.TransactionAggregator;
import freerails.model.simulation.Clock;
import freerails.model.simulation.Time;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;

/**
 * Calculates the stock price for each of the players.
 * <p>
 * Stock price = [Net worth + 5 * profit last year] / [ shares owned by public + 0.5 shares owned by other players]
 * <p>
 * Let profit last year = 100,000 in the first year.
 */
public class StockPriceCalculator {

    private final UnmodifiableWorld world;

    /**
     * Creates a calculator for the companies in the supplied world.
     *
     * @param world world state providing players and financial transactions
     */
    public StockPriceCalculator(UnmodifiableWorld world) {
        this.world = world;
    }

    /**
     * Calculates one set of prices for each player, in world player order.
     *
     * @return calculated prices indexed by player index
     */
    public StockPrice[] calculate() {
        StockPrice[] stockPrices = new StockPrice[world.getPlayers().size()];
        for (int playerIndex = 0; playerIndex < stockPrices.length; playerIndex++) {
            int playerId = world.getPlayer(playerIndex).getId();
            Money profitLastYear;
            if (isFirstYear(playerId)) {
                profitLastYear = ModelConstants.FIRST_YEAR_PROFIT;
            } else {
                profitLastYear = profitsLastYear(playerId);
            }
            Money netWorth = netWorth(playerId);
            int publicShares = sharesOwnedByPublic(playerId);
            int otherRRShares = sharesOwnedByOtherPlayers(playerId);
            stockPrices[playerIndex] = new StockPrice(netWorth, profitLastYear, publicShares, otherRRShares);
        }
        return stockPrices;
    }

    /**
     * Tests whether the player's first recorded transaction occurred in the
     * current game year.
     *
     * @param playerId identifier of the player to examine
     * @return {@code true} when the first transaction is in the current year
     */
    boolean isFirstYear(int playerId) {
        Player player = world.getPlayer(world.getPlayerIndex(playerId));
        Time firstTransactionTime = world.getTransaction(player, 0).getTime();
        Clock clock = world.getClock();
        int year = clock.getYear(firstTransactionTime);
        int currentYear = clock.getCurrentYear();
        return year == currentYear;
    }

    /**
     * Calculates the player's net worth as of the start of the current year.
     *
     * @param playerId identifier of the player to examine
     * @return net worth at the current year's start
     */
    public Money netWorth(int playerId) {
        // Set the interval to beginning of time to start of this year.
        Clock clock = world.getClock();
        Time[] times = {Time.ZERO, new Time(clock.getTimeAtStartOfCurrentYear(),1)};

        Player player = world.getPlayer(world.getPlayerIndex(playerId));
        NetWorthAggregator netWorthAggregator = new NetWorthAggregator(world, player, times);
        netWorthAggregator.aggregate();
        return netWorthAggregator.getValues()[0];
    }

    /**
     * Sums the player's non-item transaction amounts during the previous game
     * year.
     *
     * @param playerId identifier of the player to examine
     * @return net transaction amount for the previous year
     */
    public Money profitsLastYear(int playerId) {
        Player player = world.getPlayer(world.getPlayerIndex(playerId));

        Clock clock = world.getClock();
        Time[] interval = {clock.getTimeAtStartOfYear(clock.getCurrentYear() - 1), clock.getTimeAtStartOfCurrentYear()};

        TransactionAggregator aggregator = new TransactionAggregator(world, player, interval,
                transaction -> !(transaction instanceof ItemTransaction));
        aggregator.aggregate();
        return aggregator.getValues()[0];
    }

    /**
     * Counts shares held by the public, excluding shares held by other
     * railroads and the company's own treasury.
     *
     * @param playerId company identifier
     * @return number of publicly held shares
     */
    private int sharesOwnedByPublic(int playerId) {
        Player pr = world.getPlayer(world.getPlayerIndex(playerId));
        FinancialDataAggregator gatherer = new FinancialDataAggregator(world, pr);
        gatherer.aggregate();
        return gatherer.sharesHeldByPublic();
    }

    /**
     * Counts shares in the company held by all other player companies.
     *
     * @param playerId company identifier
     * @return number of shares held by other players
     */
    private int sharesOwnedByOtherPlayers(int playerId) {
        Player player = world.getPlayer(world.getPlayerIndex(playerId));
        FinancialDataAggregator gatherer = new FinancialDataAggregator(world, player);
        int[] stakes = gatherer.getStockInThisRRs();
        int playerIndex = world.getPlayerIndex(playerId);
        int total = 0;
        for (int i = 0; i < stakes.length; i++) {
            if (i != playerIndex) {
                total += stakes[i];
            }
        }
        return total;
    }

}
