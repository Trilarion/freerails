/*
 * BrokerScreenGenerator.java
 *
 * Created on January 26, 2005, 1:31 PM
 */

package jfreerails.client.view;

import java.util.HashMap;
import jfreerails.controller.FinancialDataGatherer;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ItemsTransactionAggregator;
import jfreerails.world.top.ReadOnlyWorld;
import static jfreerails.world.accounts.Transaction.Category.*;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;

/**
 *
 * @author smackay
 */

public class BrokerScreenGenerator {
    
    private FinancialDataGatherer dataGatherer;
    
    private GameCalendar cal;
    
    private HashMap otherRRShares;
    
    public String playername;
    
    public String year;
    
    public Money cash;
    
    public Money loansTotal;
    
    public Money netWorth;
    
    public Money pricePerShare;
    
    public int publicShares;
    
    public int treasuryStock;
    
    public String othersRRsStockTable;

    
    /** Creates a new instance of BrokerScreenGenerator */
    public BrokerScreenGenerator(ReadOnlyWorld w, FreerailsPrincipal principal) {
        dataGatherer = new FinancialDataGatherer(w,principal);
        
        this.playername = w.getPlayer(w.getID(principal)).getName();
        
        this.cal = (GameCalendar) w.get(ITEM.CALENDAR);
        GameTime time = (GameTime) w.get(ITEM.TIME);
        final int startyear = cal.getYear(time.getTime());
	this.year = String.valueOf(startyear);
        this.cash = w.getCurrentBalance(principal);
        
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
				w, principal);
        
        aggregator.setCategory(BOND);
	this.loansTotal = aggregator.calculateValue();
        
        this.publicShares = dataGatherer.totalShares();
        this.netWorth = dataGatherer.netWorth();
        this.pricePerShare = dataGatherer.sharePrice();
        this.treasuryStock = dataGatherer.treasuryStock();
        
        this.otherRRShares = dataGatherer.otherRRShares();
        
        StringBuffer othersRRBuffer = new StringBuffer("");
        if(otherRRShares.size() > 0){
            othersRRBuffer.append("<tr><td colspan=\"2\">&nbsp;</td><td><div align=\"right\"><table>");
            for(int i = 0; i < w.getNumberOfPlayers();i++){
                Integer totalstock = (Integer) otherRRShares.get(i);
                if(totalstock != null){
                    othersRRBuffer.append("<tr><td>");
                    Player tempPlayer = w.getPlayer(i);
                    othersRRBuffer.append(tempPlayer.getName());
                    othersRRBuffer.append("</td><td>");
                    othersRRBuffer.append(totalstock);
                    othersRRBuffer.append("</td></tr>");
                }
            }
            othersRRBuffer.append("</table></td></tr>");
        }
        othersRRsStockTable = othersRRBuffer.toString();
    }
}
