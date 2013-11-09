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
package org.railz.world.accounts;

import org.railz.world.common.FreerailsSerializable;
import org.railz.world.common.GameTime;

/**
 * @author Luke Lindsay
 * 
 *         Represents a transaction of some sort.
 */
public abstract class Transaction implements FreerailsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1814563977867949856L;
	public static final int CATEGORY_NO_CATEGORY = 0;
	public static final int CATEGORY_REVENUE = 1;
	public static final int CATEGORY_COST_OF_SALES = 2;
	public static final int CATEGORY_OPERATING_EXPENSE = 3;
	public static final int CATEGORY_INTEREST = 4;
	public static final int CATEGORY_CAPITAL_EXPENSE = 5;
	public static final int CATEGORY_TAX = 6;
	public static final int CATEGORY_CAPITAL_GAIN = 7;
	public static final int CATEGORY_OUTSIDE_INVESTMENT = 8;

	public static final int SUBCATEGORY_NO_SUBCATEGORY = 0;

	private final GameTime time;
	private final long value;

	/**
	 * @return value of the transaction. Positive means credit, negative means
	 *         debit.
	 */
	public final long getValue() {
		return value;
	}

	protected Transaction(GameTime t, long value) {
		assert Math.abs(value) < 10000000;
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

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Transaction))
			return false;
		Transaction t = (Transaction) o;

		return (time.equals(t.time) && value == t.value
				&& getCategory() == t.getCategory() && getSubcategory() == t
					.getSubcategory());
	}

	@Override
	public int hashCode() {
		return time.hashCode() ^ (new Long(value)).hashCode() ^ getCategory()
				^ getSubcategory();
	}

	@Override
	public String toString() {
		return "Transaction " + value;
	}
}
