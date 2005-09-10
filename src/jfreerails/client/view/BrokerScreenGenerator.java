/*
 * BrokerScreenGenerator.java
 *
 * Created on January 26, 2005, 1:31 PM
 */

package jfreerails.client.view;

import static jfreerails.world.accounts.Transaction.Category.BOND;

import java.util.HashMap;

import jfreerails.controller.FinancialDataGatherer;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ItemsTransactionAggregator;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * 
 * @author smackay
 */

public class BrokerScreenGenerator {

	private FinancialDataGatherer dataGatherer;

	private GameCalendar cal;

	private HashMap<Integer, Integer> otherRRShares;

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
		dataGatherer = new FinancialDataGatherer(w, principal);

		int playerId= w.getID(principal);
		this.playername = w.getPlayer(playerId).getName();

		this.cal = (GameCalendar) w.get(ITEM.CALENDAR);
		GameTime time = w.currentTime();
		final int startyear = cal.getYear(time.getTicks());
		this.year = String.valueOf(startyear);
		this.cash = w.getCurrentBalance(principal);

		ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
				w, principal);

		aggregator.setCategory(BOND);
		this.loansTotal = aggregator.calculateValue();

		this.publicShares = dataGatherer.sharesHeldByPublic();
		this.netWorth = dataGatherer.netWorth();
		this.pricePerShare = dataGatherer.sharePrice();
		this.treasuryStock = dataGatherer.treasuryStock();

		this.otherRRShares = new HashMap<Integer, Integer>(); 
		
		int[] otherRRSharesArray = dataGatherer.getStockInThisRRs();
		for (int i = 0; i < otherRRSharesArray.length; i++) {
			if(i != playerId && otherRRSharesArray[i] > 0){
				otherRRShares.put(i,  otherRRSharesArray[i]);
			}						
		}
			

		StringBuffer othersRRBuffer = new StringBuffer("");
		if (otherRRShares.size() > 0) {
			othersRRBuffer
					.append("<tr><td colspan=\"2\">&nbsp;</td><td><div align=\"right\"><table>");
			for (int i = 0; i < w.getNumberOfPlayers(); i++) {
				Integer totalstock = otherRRShares.get(i);
				if (totalstock != null) {
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
