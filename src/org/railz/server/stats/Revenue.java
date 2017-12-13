/*
 * Copyright (C) 2004 Robert Tuck
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
package org.railz.server.stats;

import java.util.*;

import org.railz.server.*;
import org.railz.world.accounts.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;

/**
 * Calculates a players annual revenue.
 */
public class Revenue implements StatMonitor {
    public String getName() {
	return "Annual Revenue";
    }

    public String getDescription() {
	return "The players annual revenue.";
    }

    public String getYUnit() {
	return "$ / 1000";
    }

    public int calculateDataPoint(ReadOnlyWorld w, FreerailsPrincipal p) {
	GameCalendar calendar = (GameCalendar) w.get(ITEM.CALENDAR, p);
	GameTime t1 = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);

	GregorianCalendar calendarYear = calendar.getCalendar(t1);
	if (calendarYear.get(Calendar.YEAR) <= calendar.getStartYear())
	    return 0;

	long t0 = calendar.getTimeFromCalendar
	    (new GregorianCalendar(calendarYear.get(Calendar.YEAR) - 1, 0, 1))
	    .getTime();

	BankAccount account = (BankAccount) w.get(KEY.BANK_ACCOUNTS, 0, p);
	int nTransactions = account.size();
	int i = 0;
	while (i < account.size() &&
		account.getTransaction(i).getTime().getTime() < t0) {
	    i++;
	}

	long yearEndInTicks = t1.getTime();

	/* calculate income for freight haulage */
	/* TODO until accounting is changed, all cargo revenue is categorised
	 * as freight */
	long freightRevenue = 0;
	for (; i < account.size() &&
		account.getTransaction(i).getTime().getTime() < yearEndInTicks;
	       	i++) {
	    Transaction t = account.getTransaction(i);
	    if (t.getCategory() == Transaction.CATEGORY_REVENUE)
		    freightRevenue += t.getValue();
	}
	return (int) (freightRevenue / 1000);
    }
}


