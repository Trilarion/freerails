/*
 * Created on Mar 28, 2004
 */
package jfreerails.client.view;

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
import static jfreerails.world.accounts.Transaction.Category.*;

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

	public Money operatingFundsTotal;

	public Money operatingFundsYtd;

	public Money trackTotal;

	public Money trackYtd;

	public Money stationsTotal;

	public Money stationsYtd;

	public Money rollingStockTotal;

	public Money rollingStockYtd;

	public Money industriesTotal;

	public Money industriesYtd;

	public Money loansTotal;

	public Money loansYtd;

	public Money equityTotal;

	public Money equityYtd;

	public Money profitTotal;

	public Money profitYtd;

	public String year;

	BalanceSheetGenerator(ReadOnlyWorld w, FreerailsPrincipal principal) {
		this.w = w;
		this.principal = principal;
		cal = (GameCalendar) w.get(ITEM.CALENDAR);
		// Calculate totals
		GameTime time = (GameTime) w.get(ITEM.TIME);
		final int startyear = cal.getYear(time.getTime());
		year = String.valueOf(startyear);
		GameTime startOfYear = new GameTime(cal.getTicks(startyear));

		operatingFundsTotal = w.getCurrentBalance(principal);
		trackTotal = calTrackTotal(TRACK, w, principal, GameTime.BIG_BANG);

		ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
				w, principal);

		GameTime[] totalTimeInteval = new GameTime[] { GameTime.BIG_BANG,
				GameTime.END_OF_THE_WORLD };

		aggregator.setTimes(totalTimeInteval);

		aggregator.setCategory(STATIONS);
		stationsTotal = aggregator.calculateValue();

		aggregator.setCategory(TRAIN);
		rollingStockTotal = aggregator.calculateValue();

		aggregator.setCategory(INDUSTRIES);
		industriesTotal = aggregator.calculateValue();
		aggregator.setCategory(BOND);
		loansTotal = aggregator.calculateValue();
		aggregator.setCategory(ISSUE_STOCK);
		equityTotal = aggregator.calculateValue();

		long profit = operatingFundsTotal.getAmount() + trackTotal.getAmount()
				+ stationsTotal.getAmount() + rollingStockTotal.getAmount()
				+ industriesTotal.getAmount() + loansTotal.getAmount()
				+ equityTotal.getAmount();
		profitTotal = new Money(profit);

		// Calculate ytd changes

		TransactionAggregator ytdOperatingFunds = new TransactionAggregator(w,
				principal) {
			protected boolean condition(int i) {
				int transactionYear = cal.getYear(w.getTransactionTimeStamp(i,
						principal).getTime());

				return transactionYear >= startyear;
			}
		};

		GameTime[] ytdTimeInteval = new GameTime[] { startOfYear,
				GameTime.END_OF_THE_WORLD };
		aggregator.setTimes(ytdTimeInteval);

		operatingFundsYtd = ytdOperatingFunds.calculateValue();
		trackYtd = calTrackTotal(TRACK, w, principal, startOfYear);

		aggregator.setCategory(STATIONS);
		stationsYtd = aggregator.calculateValue();

		aggregator.setCategory(TRAIN);
		rollingStockYtd = aggregator.calculateValue();

		aggregator.setCategory(INDUSTRIES);
		industriesYtd = aggregator.calculateValue();

		aggregator.setCategory(BOND);
		loansYtd = aggregator.calculateValue();
		aggregator.setCategory(ISSUE_STOCK);
		equityYtd = aggregator.calculateValue();

		profit = operatingFundsYtd.getAmount() + trackYtd.getAmount()
				+ stationsYtd.getAmount() + rollingStockYtd.getAmount()
				+ industriesYtd.getAmount() + loansYtd.getAmount()
				+ equityYtd.getAmount();
		profitYtd = new Money(profit);
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
}