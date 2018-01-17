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

import freerails.controller.FinancialDataGatherer;
import freerails.controller.StockPriceCalculator;
import freerails.controller.StockPriceCalculator.StockPrice;
import freerails.world.ITEM;
import freerails.world.ItemsTransactionAggregator;
import freerails.world.ReadOnlyWorld;
import freerails.world.finances.Money;
import freerails.world.finances.TransactionCategory;
import freerails.world.game.GameCalendar;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;

import java.text.DecimalFormat;

/**
 *
 */
class BrokerScreenGenerator {

    private static final DecimalFormat DC = new DecimalFormat("#,###");

    /**
     * Creates a new instance of BrokerScreenGenerator
     */
    public BrokerScreenGenerator(ReadOnlyWorld w, FreerailsPrincipal principal) {
        FinancialDataGatherer dataGatherer = new FinancialDataGatherer(w, principal);

        int playerId = w.getID(principal);
        String playername = w.getPlayer(playerId).getName();

        GameCalendar cal = (GameCalendar) w.get(ITEM.CALENDAR);
        GameTime time = w.currentTime();
        final int startyear = cal.getYear(time.getTicks());
        String year = String.valueOf(startyear);
        Money cash = w.getCurrentBalance(principal);

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(w, principal);

        aggregator.setCategory(TransactionCategory.BOND);
        Money loansTotal = aggregator.calculateValue();

        String publicShares = DC.format(dataGatherer.sharesHeldByPublic());
        Money netWorth = dataGatherer.netWorth();
        StockPrice[] stockPrices = (new StockPriceCalculator(w)).calculate();
        Money pricePerShare = stockPrices[playerId].currentPrice;
        String treasuryStock = DC.format(dataGatherer.treasuryStock());

        StringBuilder otherRRsStakes = new StringBuilder();
        int[] stockInThisRRs = dataGatherer.getStockInThisRRs();

        for (int i = 0; i < stockInThisRRs.length; i++) {
            if (i != playerId && stockInThisRRs[i] > 0) {
                String otherRRName = w.getPlayer(i).getName();
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
