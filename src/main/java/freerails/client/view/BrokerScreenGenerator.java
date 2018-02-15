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

import freerails.model.finances.FinancialDataGatherer;
import freerails.model.finances.StockPriceCalculator;
import freerails.model.finances.StockPrice;
import freerails.model.world.ITEM;
import freerails.model.finances.ItemsTransactionAggregator;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.finances.Money;
import freerails.model.finances.TransactionCategory;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameTime;
import freerails.model.player.FreerailsPrincipal;

import java.text.DecimalFormat;

/**
 *
 */
class BrokerScreenGenerator {

    private static final DecimalFormat DC = new DecimalFormat("#,###");

    // TODO is the code here ever really used?
    /**
     * Creates a new instance of BrokerScreenGenerator
     */
    public BrokerScreenGenerator(ReadOnlyWorld world, FreerailsPrincipal principal) {
        FinancialDataGatherer dataGatherer = new FinancialDataGatherer(world, principal);

        int playerId = world.getID(principal);
        String playername = world.getPlayer(playerId).getName();

        GameCalendar cal = (GameCalendar) world.get(ITEM.CALENDAR);
        GameTime time = world.currentTime();
        final int startyear = cal.getYear(time.getTicks());
        String year = String.valueOf(startyear);
        Money cash = world.getCurrentBalance(principal);

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, principal);

        aggregator.setCategory(TransactionCategory.BOND);
        Money loansTotal = aggregator.calculateValue();

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
