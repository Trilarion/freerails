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
package org.railz.world.accounts;

import org.railz.world.common.*;

public class InterestTransaction extends Transaction {
    public static final int SUBCATEGORY_LOAN = 1;
    public static final int SUBCATEGORY_BOND = 2;
    public static final int SUBCATEGORY_ACCOUNT_CREDIT_INTEREST = 3;
    public static final int SUBCATEGORY_OVERDRAFT = 4;

    private final int subcategory;

    /**
     * @param amount amount of interest to be added to account
     */
    public InterestTransaction(GameTime t, long amount, int subcategory) {
	super(t, amount);
	this.subcategory = subcategory;
    }

    public int getCategory() {
	return CATEGORY_INTEREST;
    }

    public int getSubcategory() {
	return subcategory;
    }
}
