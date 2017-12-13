/*
 * Copyright (C) 2003 Luke Lindsay
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

/*
 * Created on 21-Jun-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.GameTime;

/**
 * @author Luke Lindsay
 */
public class Bill extends Transaction {
    /* for use with subcategory */
    public static final int TRACK_MAINTENANCE = 1;
    public static final int ROLLING_STOCK_MAINTENANCE = 2;
    public static final int FUEL = 3;
    public static final int INCOME_TAX = 4;

    public final int subcategory;

    /**
     * @param amount amount to be debited from account
     */
    public Bill(GameTime time, long amount, int subcategory) {
	super(time, -amount);
	switch (subcategory) {
	    case TRACK_MAINTENANCE:
	    case ROLLING_STOCK_MAINTENANCE:
	    case FUEL:
	    case INCOME_TAX:
		break;
	    default:
		throw new IllegalArgumentException();
	}

	this.subcategory = subcategory;
    }

    public final int getCategory() {
	switch (subcategory) {
	    case TRACK_MAINTENANCE:
	    case ROLLING_STOCK_MAINTENANCE:
		return CATEGORY_OPERATING_EXPENSE;
	    case FUEL:
		return CATEGORY_COST_OF_SALES;
	    case INCOME_TAX:
		return CATEGORY_TAX;
	}
	assert false;
	// keep the compiler happy :)
	return 0;
    }

    public final int getSubcategory() {
	return subcategory;
    }
}
