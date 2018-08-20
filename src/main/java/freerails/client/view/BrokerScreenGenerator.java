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
 * BrokerScreenGenerator.java
 *
 */

package freerails.client.view;

import freerails.model.finance.*;
import freerails.model.finance.transaction.aggregator.FinancialDataAggregator;
import freerails.model.finance.transaction.aggregator.ItemsTransactionAggregator;
import freerails.model.game.Clock;
import freerails.model.game.Time;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.player.Player;

import java.text.DecimalFormat;

/**
 *
 */
class BrokerScreenGenerator {

    private static final DecimalFormat DC = new DecimalFormat("#,###");

    // TODO is the code here ever really used? move it to model.finance
    /**
     * Creates a new instance of BrokerScreenGenerator
     */
    public BrokerScreenGenerator(UnmodifiableWorld world, Player player) {
        int playerId = player.getId();
        String playername = world.getPlayer(playerId).getName();

        Clock clock = world.getClock();
        final int startyear = clock.getCurrentYear();
        String year = String.valueOf(startyear);
        Money cash = world.getCurrentBalance(player);

        Time[] times = {Time.ZERO, clock.getCurrentTime()};
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player, times);

        aggregator.setCategory(TransactionCategory.BOND);
        aggregator.aggregate();
        Money loansTotal = aggregator.getValues()[0];

        FinancialDataAggregator dataGatherer = new FinancialDataAggregator(world, player, times);
        String publicShares = DC.format(dataGatherer.sharesHeldByPublic());
        Money netWorth = dataGatherer.netWorth();
        StockPrice[] stockPrices = (new StockPriceCalculator(world)).calculate();
        Money pricePerShare = stockPrices[playerId].currentPrice;
        String treasuryStock = DC.format(dataGatherer.treasuryStock());

        StringBuilder otherRRsStakes = new StringBuilder();
        int[] stockInThisRRs = dataGatherer.getStockInThisRRs();

        for (int i = 0; i < stockInThisRRs.length; i++) {
            if (i != playerId && stockInThisRRs[i] > 0) {
                String otherRRName = world.getPlayer(i).getName();
                String otherRRStake = DC.format(stockInThisRRs[i]);
                otherRRsStakes.append("<tr> ");
                otherRRsStakes.append("<td> </td>");
                otherRRsStakes.append("<td> </td>");
                otherRRsStakes.append("<td>").append(otherRRName).append("</td>");
                otherRRsStakes.append("<td>").append(otherRRStake).append("</td>");
                otherRRsStakes.append("</tr>");
            }
        }
        String othersRRsStockRows = otherRRsStakes.toString();
    }
}
