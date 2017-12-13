/*
 * Created on 21-Jun-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;

/**
 * @author Luke Lindsay
 *
 * Represents a transaction of some sort.
 */
public abstract class Transaction implements FreerailsSerializable {
    public static final int CATEGORY_NO_CATEGORY = 0;
    public static final int CATEGORY_REVENUE = 1;
    public static final int CATEGORY_COST_OF_SALES = 2;
    public static final int CATEGORY_OPERATING_EXPENSE = 3;
    public static final int CATEGORY_INTEREST = 4;
    public static final int CATEGORY_CAPITAL_EXPENSE = 5;
    public static final int CATEGORY_TAX = 6;
    public static final int CATEGORY_CAPITAL_GAIN = 7;

    public static final int SUBCATEGORY_NO_SUBCATEGORY = 0;

    private final GameTime time;
    private final long value;

    /**
     * @return value of the transaction. Positive means credit, negative
     * means debit.
     */
    public final long getValue() {
	return value;
    }

    protected Transaction(GameTime t, long value) {
	time = t;
	this.value = value;
    }
    
    /**
     * @return the time at which the transaction occurred
     */
    public final GameTime getTime() {
	return time;
    }

    /**
     * @return Category of the transaction for accounting purposes
     */
    public abstract int getCategory();

    /**
     * @return Subcategory of the transaction. Defined by implementations.
     */
    public abstract int getSubcategory();

    public boolean equals(Object o) {
	if (! (o instanceof Transaction))
	    return false;
	Transaction t = (Transaction) o;

	return (time.equals(t.time) && value == t.value && getCategory() ==
		t.getCategory() && getSubcategory() == t.getSubcategory());
    }

    public int hashCode() {
	return time.hashCode() ^ (new Long(value)).hashCode() ^
	    getCategory() ^ getSubcategory();
    }
}
