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
 * BrokerScreenGenerator.java
 *
 */

package freerails.client.view;

import freerails.model.economy.*;
import freerails.model.economy.transaction.aggregator.FinancialDataAggregator;
import freerails.model.economy.transaction.aggregator.ItemsTransactionAggregator;
import freerails.model.simulation.Clock;
import freerails.model.simulation.Time;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.player.Player;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * Provides the broker screen generator functionality.
 */
class BrokerScreenGenerator {

    private static final DecimalFormat DC = new DecimalFormat("#,###");
    private final Map<String, String> templateValues;

    // TODO is the code here ever really used? move it to model.economy
    /**
     * Creates broker-screen values for the specified player.
     *
     * @param world the game world
     * @param player the player whose financial details are shown
     */
    public BrokerScreenGenerator(UnmodifiableWorld world, Player player) {
        int playerIndex = world.getPlayerIndex(player.getId());
        String playerName = player.getName();

        Clock clock = world.getClock();
        Money cash = world.getCurrentBalance(player);

        Time[] times = {Time.ZERO, clock.getCurrentTime()};
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player, times);

        aggregator.setCategory(TransactionCategory.BOND);
        aggregator.aggregate();
        Money loansTotal = aggregator.getValues()[0];

        FinancialDataAggregator dataGatherer = new FinancialDataAggregator(world, player, times);
        String publicShares = DC.format(dataGatherer.sharesHeldByPublic());
        Money netWorth = dataGatherer.netWorth();
        StockPrice[] stockPrices = new StockPriceCalculator(world).calculate();
        Money pricePerShare = stockPrices[playerIndex].currentPrice;
        String treasuryStock = DC.format(dataGatherer.treasuryStock());

        StringBuilder otherRRsStakes = new StringBuilder();
        int[] stockInThisRRs = dataGatherer.getStockInThisRRs();

        for (int i = 0; i < stockInThisRRs.length; i++) {
            if (i != playerIndex && stockInThisRRs[i] > 0) {
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

        templateValues = Map.of(
                "playername", playerName,
                "netWorth", netWorth.toString(),
                "pricePerShare", pricePerShare.toString(),
                "cash", cash.toString(),
                "publicShares", publicShares,
                "loansTotal", loansTotal.toString(),
                "treasuryStock", treasuryStock,
                "othersRRsStockRows", othersRRsStockRows);
    }

    /**
     * Returns the values used to populate the broker HTML template.
     *
     * @return broker template tokens and their rendered values
     */
    public Map<String, String> getTemplateValues() {
        return templateValues;
    }
}
