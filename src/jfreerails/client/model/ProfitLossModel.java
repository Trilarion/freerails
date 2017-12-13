/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

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
    public final long totalRevenue;
    public final long fuelExpenses;
    public final long grossProfit;
    public final long trackMaintenanceExpense;
    public final long rollingStockMaintenanceExpense;
    public final long interestPayableExpense;
    public final long totalExpenses;
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
	long _freightRevenue = 0;
	long _trackMaintenanceExpense = 0;
	long _interestPayableExpense = 0;
	long _rollingStockMaintenanceExpense = 0;
	for (i = minIndex; i < maxIndex; i++) {
	    Transaction t = account.getTransaction(i);
	    switch (t.getCategory()) {
		case Transaction.CATEGORY_REVENUE:
		    _freightRevenue += t.getValue();
		    break;
		case Transaction.CATEGORY_OPERATING_EXPENSE:
		    if (t.getSubcategory() == Bill.TRACK_MAINTENANCE)
			_trackMaintenanceExpense -= t.getValue();
		    else if (t.getSubcategory() ==
			    Bill.ROLLING_STOCK_MAINTENANCE)
			_rollingStockMaintenanceExpense -= t.getValue();
		    break;
		case Transaction.CATEGORY_INTEREST:
		    _interestPayableExpense -= t.getValue();
		    break;
	    }
	}
	if (_interestPayableExpense < 0) {
	    interestPayableExpense = 0;
	} else {
	    interestPayableExpense = _interestPayableExpense;
	}
	freightRevenue = _freightRevenue;
	passengerRevenue = 0;
	fuelExpenses = 0;
	totalRevenue = freightRevenue + passengerRevenue;
	grossProfit = totalRevenue - fuelExpenses;
	trackMaintenanceExpense = _trackMaintenanceExpense;
	rollingStockMaintenanceExpense = _rollingStockMaintenanceExpense;
	profitBeforeTax = grossProfit - trackMaintenanceExpense -
	    rollingStockMaintenanceExpense - interestPayableExpense;
	totalExpenses = fuelExpenses + trackMaintenanceExpense +
	    rollingStockMaintenanceExpense + interestPayableExpense;
	incomeTax = profitBeforeTax > 0 ? profitBeforeTax *
	    incomeTaxRatePercent / 100 : 0;
	profitAfterTax = profitBeforeTax - incomeTax;
    }
}
