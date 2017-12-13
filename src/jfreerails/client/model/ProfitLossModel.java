package jfreerails.client.model;

import java.util.GregorianCalendar;

import jfreerails.world.accounts.*;
import jfreerails.world.common.Economy;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * Presents the information for a profit and loss statement for a given year.
 */
public class ProfitLossModel {
    /**
     * values for various figures.
     */
    public final long freightRevenue;
    public final long passengerRevenue;
    public final long fuelExpenses;
    public final long grossProfit;
    public final long trackMaintenanceExpense;
    public final long rollingStockMaintenanceExpense;
    public final long interestPayableExpense;
    public final long profitBeforeTax;
    public final long incomeTax;
    public final long profitAfterTax;
    public final int incomeTaxRatePercent;

    /**
     * @param year year for which to generate P + L data
     */
    public ProfitLossModel(ModelRoot mr, int year) {
	ReadOnlyWorld world = mr.getWorld();

	Economy economy = (Economy) world.get(ITEM.ECONOMY,
		mr.getPlayerPrincipal());

	incomeTaxRatePercent = economy.getIncomeTaxRate();

	GameCalendar calendar = (GameCalendar) world.get(ITEM.CALENDAR,
		mr.getPlayerPrincipal());
	GregorianCalendar calendarYear = new GregorianCalendar(year, 0, 1);
	long deltaInMillis = calendarYear.getTimeInMillis() - (new
		    GregorianCalendar(calendar.getStartYear(), 0,
			1)).getTimeInMillis();
	long deltaInTicks = deltaInMillis / (1000 * 60 * 60 * 24) *
	    calendar.getTicksPerDay();
	GameTime now = (GameTime) world.get(ITEM.TIME, mr.getPlayerPrincipal());
	if (now.getTime() < deltaInTicks) {
	    throw new IllegalArgumentException("This year hasn't arrived " +
		    "yet!");
	}

	BankAccount account = (BankAccount) world.get(KEY.BANK_ACCOUNTS, 0,
		mr.getPlayerPrincipal());
	int nTransactions = account.size();
	int i = 0;
	while (i < account.size() &&
		account.getTransaction(i).getTime().getTime() < deltaInTicks) {
	    i++;
	}

	if (i == account.size()) {
	    // TODO
	    // everything == 0
	}

	int minIndex = i;
	long yearEndInTicks = deltaInTicks + (calendar.getTicksPerDay() *
		calendarYear.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
	int maxIndex = i;
	while (maxIndex < account.size() &&
		account.getTransaction(maxIndex).getTime().getTime() <
		yearEndInTicks) {
	    maxIndex++;
	}

	/* calculate income for freight haulage */
	/* TODO until accounting is changed, all cargo revenue is categorised
	 * as freight */
	int _freightRevenue = 0;
	int _trackMaintenanceExpense = 0;
	for (i = minIndex; i < maxIndex; i++) {
	    Transaction t = account.getTransaction(i);
	    switch (t.getCategory()) {
		case Transaction.CATEGORY_REVENUE:
		    _freightRevenue += t.getValue();
		    break;
		case Transaction.CATEGORY_OPERATING_EXPENSE:
		    if (t.getSubcategory() == Bill.TRACK_MAINTENANCE)
			_trackMaintenanceExpense -= t.getValue();
		    break;
	    }
	}
	freightRevenue = _freightRevenue;
	passengerRevenue = 0;
	fuelExpenses = 0;
	grossProfit = freightRevenue + passengerRevenue - fuelExpenses;
	trackMaintenanceExpense = _trackMaintenanceExpense;
	rollingStockMaintenanceExpense = 0;
	interestPayableExpense = 0;
	profitBeforeTax = grossProfit - trackMaintenanceExpense -
	    rollingStockMaintenanceExpense - interestPayableExpense;
	incomeTax = profitBeforeTax > 0 ? profitBeforeTax *
	    incomeTaxRatePercent / 100 : 0;
	profitAfterTax = profitBeforeTax - incomeTax;
    }
}
