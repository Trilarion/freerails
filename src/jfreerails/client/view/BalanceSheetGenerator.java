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
import jfreerails.world.track.TrackRule;
/**
 * @author Luke
 *  
 */
public class BalanceSheetGenerator {
	
	final int TRACK = 0;
	final int STATIONS = 1;		
	GameTime from;
	GameTime to;
	final ReadOnlyWorld w;
	final FreerailsPrincipal principal;
	private int startyear = 0;
	private GameCalendar cal;
	public Money operatingFundsTotal, operatingFundsYtd, trackTotal, trackYtd,
			stationsTotal, stationsYtd, 
			rollingStockTotal, rollingStockYtd, loansTotal, loansYtd,
			equityTotal, equityYtd, profitTotal, profitYtd;
	public String year;
	BalanceSheetGenerator(ReadOnlyWorld w, FreerailsPrincipal principal) {
		this.w = w;
		this.principal = principal;
		cal = (GameCalendar) w.get(ITEM.CALENDAR);
		//Calculate totals
		operatingFundsTotal = w.getCurrentBalance(principal);
		trackTotal = calTrackTotal(TRACK);
		stationsTotal = calTrackTotal(STATIONS);
		
		ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(w, principal);
		aggregator.setStartYear(0);
		
		aggregator.setCategory(Transaction.TRAIN);
		rollingStockTotal = aggregator.calulateAssetValue();
		aggregator.setCategory(Transaction.BOND);
		loansTotal = aggregator.calulateAssetValue();
		aggregator.setCategory(Transaction.EQUITY);
		equityTotal = aggregator.calulateAssetValue();
	
		long profit = operatingFundsTotal.getAmount() + trackTotal.getAmount() + stationsTotal.getAmount() + rollingStockTotal.getAmount() + loansTotal.getAmount() + equityTotal.getAmount();
		profitTotal = new Money(profit);
		
		//Calculate ytd changes
		GameTime time = (GameTime) w.get(ITEM.TIME);
		startyear = cal.getYear(time.getTime());
		year = String.valueOf(startyear);
		
		TransactionAggregator ytdOperatingFunds = new TransactionAggregator(w, principal){
			protected boolean condition(int i) {				
				int transactionYear = cal.getYear(w.getTransactionTimeStamp(i, principal).getTime());
				return transactionYear >= startyear;
			}			
		};		
		operatingFundsYtd = ytdOperatingFunds.calulateValue();
		trackYtd = calTrackTotal(TRACK);
		stationsYtd = calTrackTotal(STATIONS);
		
		aggregator.setStartYear(startyear);
		
		aggregator.setCategory(Transaction.TRAIN);
		rollingStockYtd = aggregator.calulateAssetValue();
		aggregator.setCategory(Transaction.BOND);
		loansYtd = aggregator.calulateAssetValue();
		aggregator.setCategory(Transaction.EQUITY);
		equityYtd = aggregator.calulateAssetValue();
	
		profit = operatingFundsYtd.getAmount() + trackYtd.getAmount() + stationsYtd.getAmount() + rollingStockYtd.getAmount() + loansYtd.getAmount() + equityYtd.getAmount();
		profitYtd = new Money(profit);				
		
	}
	
	Money calTrackTotal(int category){
		ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(w, principal);
		aggregator.setStartYear(startyear);
		aggregator.setCategory(Transaction.TRACK);
        long amount = 0;

        for (int i = 0; i < w.size(SKEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule)w.get(SKEY.TRACK_RULES, i);
            long trackValue = trackRule.getPrice().getAmount();

            //Is the track type the category we are interested in?
            boolean rightType = TRACK == category
                ? !trackRule.isStation() : trackRule.isStation();

            if (rightType) {
            	aggregator.setType(i);
                amount += trackValue * aggregator.calulateQuantity();
            }
        }
        return new Money(amount);
	}
}