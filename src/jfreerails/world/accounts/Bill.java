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
	}
	assert false;
	// keep the compiler happy :)
	return 0;
    }

    public final int getSubcategory() {
	return subcategory;
    }
}
