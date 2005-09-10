/*
 * Created on Mar 28, 2004
 */
package jfreerails.client.view;

import static jfreerails.world.accounts.Transaction.Category.*;
import static jfreerails.world.accounts.Transaction.Category.INDUSTRIES;
import static jfreerails.world.accounts.Transaction.Category.ISSUE_STOCK;
import static jfreerails.world.accounts.Transaction.Category.STATIONS;
import static jfreerails.world.accounts.Transaction.Category.TRACK;
import static jfreerails.world.accounts.Transaction.Category.TRAIN;
import jfreerails.controller.FinancialDataGatherer;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ItemsTransactionAggregator;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.TransactionAggregator;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackRule;

/**
 * Generates the balance sheet - note, its fields are read using reflection so
 * don't change their names.
 * 
 * @author Luke
 * 
 */
public class BalanceSheetGenerator {

	GameTime from;

	GameTime to;

	final ReadOnlyWorld w;

	final FreerailsPrincipal principal;

	private GameCalendar cal;
	
	public String year;

	public Stats total;
	
	public Stats ytd;

	BalanceSheetGenerator(ReadOnlyWorld w, FreerailsPrincipal principal) {
		this.w = w;
		this.principal = principal;
		cal = (GameCalendar) w.get(ITEM.CALENDAR);
		// Calculate totals
		GameTime time = w.currentTime();
		final int startyear = cal.getYear(time.getTicks());
		year = String.valueOf(startyear);
		GameTime startOfYear = new GameTime(cal.getTicks(startyear));
		
		GameTime[] totalTimeInteval = new GameTime[] { GameTime.BIG_BANG,
				GameTime.END_OF_THE_WORLD };

		total = calulateValues(totalTimeInteval);
		GameTime[] ytdTimeInteval = new GameTime[] { startOfYear,
				GameTime.END_OF_THE_WORLD };
		ytd = calulateValues(ytdTimeInteval);		
	}

	private Stats  calulateValues(final GameTime[] totalTimeInteval) {
		
		Stats returnValue = new Stats();
		
		
		TransactionAggregator ytdOperatingFunds = new TransactionAggregator(w,
				principal) {
			protected boolean condition(int i) {
				int transactionYear = w.getTransactionTimeStamp(
						principal, i).getTicks();

				return transactionYear >= totalTimeInteval[0].getTicks();
			}
		};
		
		returnValue.operatingFunds= ytdOperatingFunds.calculateValue();
		
		returnValue.track = calTrackTotal(TRACK, w, principal, totalTimeInteval[0]);

		ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
				w, principal);		
		aggregator.setTimes(totalTimeInteval);

		aggregator.setCategory(STATIONS);
		returnValue.stations = aggregator.calculateValue();

		aggregator.setCategory(TRAIN);
		returnValue.rollingStock = aggregator.calculateValue();

		aggregator.setCategory(INDUSTRIES);
		returnValue.industries = aggregator.calculateValue();
		aggregator.setCategory(BOND);
		returnValue.loans = aggregator.calculateValue();
		aggregator.setCategory(ISSUE_STOCK);
		returnValue.equity= aggregator.calculateValue();

		//If we don't initialize this variable 
		//we get a NPE when we don't own any stock in others RRs
		returnValue.otherRrStock = new Money(0); 
		
		int thisPlayerId = w.getID(principal);
		for (int playerId = 0; playerId < w.getNumberOfPlayers(); playerId++) {
			FinancialDataGatherer fda = new FinancialDataGatherer(w, w
					.getPlayer(playerId).getPrincipal());
			Money stockPrice = fda.sharePrice();
			aggregator.setCategory(TRANSFER_STOCK);
			aggregator.setType(thisPlayerId);
			int quantity = aggregator.calculateQuantity();
			if (playerId == thisPlayerId) {
				returnValue.treasuryStock = new Money(quantity
						* stockPrice.getAmount());
			} else {
				returnValue.otherRrStock = new Money(quantity
						* stockPrice.getAmount()
						+ returnValue.otherRrStock.getAmount());
			}
		}
		returnValue.calProfit();
		return returnValue;
	}

	public static Money calTrackTotal(Transaction.Category category,
			ReadOnlyWorld w, FreerailsPrincipal principal, GameTime startTime) {
		ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
				w, principal);

		aggregator.setCategory(TRACK);
		long amount = 0;

		for (int i = 0; i < w.size(SKEY.TRACK_RULES); i++) {
			TrackRule trackRule = (TrackRule) w.get(SKEY.TRACK_RULES, i);
			long trackValue = trackRule.getPrice().getAmount();

			GameTime[] times = new GameTime[] { startTime,
					GameTime.END_OF_THE_WORLD };

			aggregator.setType(i);
			aggregator.setTimes(times);
			ItemsTransactionAggregator.QuantitiesAndValues qnv = aggregator
					.calculateQuantitiesAndValues();
			int quantity = qnv.quantities[0];
			amount += trackValue * quantity
					/ TrackConfiguration.LENGTH_OF_STRAIGHT_TRACK_PIECE;

		}

		return new Money(amount);
	}

	public static class Stats {
		public Money operatingFunds;

		public Money track;

		public Money stations;

		public Money rollingStock;

		public Money industries;

		public Money loans;

		public Money equity;		

		public Money treasuryStock;

		public Money otherRrStock;
		
		public Money profit;

		public void calProfit(){
			long profitValue = operatingFunds.getAmount() + track.getAmount()
			+ stations.getAmount() + rollingStock.getAmount()
			+ industries.getAmount() + loans.getAmount()
			+ equity.getAmount() + treasuryStock.getAmount()
			+ otherRrStock.getAmount();
			profit= new Money(profitValue);
		}
		
	}

}
