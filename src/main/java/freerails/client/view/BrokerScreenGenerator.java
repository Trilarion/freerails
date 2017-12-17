/*
 * BrokerScreenGenerator.java
 *
 * Created on January 26, 2005, 1:31 PM
 */

package freerails.client.view;

import freerails.controller.FinancialDataGatherer;
import freerails.controller.StockPriceCalculator;
import freerails.controller.StockPriceCalculator.StockPrice;
import freerails.world.common.GameCalendar;
import freerails.world.common.GameTime;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ITEM;
import freerails.world.top.ItemsTransactionAggregator;
import freerails.world.top.ReadOnlyWorld;

import java.text.DecimalFormat;

import static freerails.world.accounts.Transaction.Category.BOND;

/**
 * @author smackay
 * @author Luke
 */

public class BrokerScreenGenerator {

    private static final DecimalFormat DC = new DecimalFormat("#,###");

    public final String playername;

    public final String year;

    public final Money cash;

    public final Money loansTotal;

    public final Money netWorth;

    public final Money pricePerShare;

    public final String publicShares;

    public final String treasuryStock;

    public final String othersRRsStockRows;

    /**
     * Creates a new instance of BrokerScreenGenerator
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
