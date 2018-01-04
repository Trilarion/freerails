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
import freerails.world.*;
import freerails.world.finances.Money;
import freerails.world.player.FreerailsPrincipal;

import java.text.DecimalFormat;

import static freerails.world.finances.TransactionCategory.BOND;

/**
 */
@SuppressWarnings("unused")
public class BrokerScreenGenerator {

    private static final DecimalFormat DC = new DecimalFormat("#,###");

    /**
     *
     */
    public final String playername;

    /**
     *
     */
    @SuppressWarnings("unused")
    public final String year;

    /**
     *
     */
    @SuppressWarnings("unused")
    public final Money cash;

    /**
     *
     */
    @SuppressWarnings("unused")
    public final Money loansTotal;

    /**
     *
     */
    @SuppressWarnings("unused")
    public final Money netWorth;

    /**
     *
     */
    @SuppressWarnings("unused")
    public final Money pricePerShare;

    /**
     *
     */
    @SuppressWarnings("unused")
    public final String publicShares;

    /**
     *
     */
    @SuppressWarnings("unused")
    public final String treasuryStock;

    /**
     *
     */
    @SuppressWarnings("unused")
    public final String othersRRsStockRows;

    /**
     * Creates a new instance of BrokerScreenGenerator
     *
     * @param w
     * @param principal
     */
    public BrokerScreenGenerator(ReadOnlyWorld w, FreerailsPrincipal principal) {
        FinancialDataGatherer dataGatherer = new FinancialDataGatherer(w, principal);

        int playerId = w.getID(principal);
        this.playername = w.getPlayer(playerId).getName();

        GameCalendar cal = (GameCalendar) w.get(ITEM.CALENDAR);
        GameTime time = w.currentTime();
        final int startyear = cal.getYear(time.getTicks());
        this.year = String.valueOf(startyear);
        this.cash = w.getCurrentBalance(principal);

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
                w, principal);

        aggregator.setCategory(BOND);
        this.loansTotal = aggregator.calculateValue();

        this.publicShares = DC.format(dataGatherer.sharesHeldByPublic());
        this.netWorth = dataGatherer.netWorth();
        StockPrice[] stockPrices = (new StockPriceCalculator(w)).calculate();
        this.pricePerShare = stockPrices[playerId].currentPrice;
        this.treasuryStock = DC.format(dataGatherer.treasuryStock());

        StringBuilder otherRRsStakes = new StringBuilder();
        int[] stockInThisRRs = dataGatherer.getStockInThisRRs();

        for (int i = 0; i < stockInThisRRs.length; i++) {
            if (i != playerId && stockInThisRRs[i] > 0) {
                String otherRRName = w.getPlayer(i).getName();
                String otherRRStake = DC.format(stockInThisRRs[i]);
                otherRRsStakes.append("<tr> ");
                otherRRsStakes.append("<td> </td>");
                otherRRsStakes.append("<td> </td>");
                otherRRsStakes.append("<td>" + otherRRName + "</td>");
                otherRRsStakes.append("<td>" + otherRRStake + "</td>");
                otherRRsStakes.append("</tr>");
            }
        }
        othersRRsStockRows = otherRRsStakes.toString();
    }
}
