/*
 * Copyright (C) Robert Tuck
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

/**
 * @author rtuck99@users.berlios.de
 */
package org.railz.world.accounts;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
public class BankAccountViewer {
    ReadOnlyWorld world;
    BankAccount bankAccount;
    Economy economy;

    public BankAccountViewer(ReadOnlyWorld w) {
	world = w;
	economy = (Economy) world.get(ITEM.ECONOMY, Player.AUTHORITATIVE);
    }

    public void setBankAccount(BankAccount ba) {
	bankAccount = ba;
    }

    /**
     * @return the cumulative amount of outside investment.
     */
    public long getOutsideInvestment() {
	int size = bankAccount.size();
	long total = 0;
	for (int i = 0; i < size; i++) {
	    Transaction t = bankAccount.getTransaction(i);
	    if (t.getCategory() == Transaction.CATEGORY_OUTSIDE_INVESTMENT) {
		total += t.getValue();
	    }
	}
	return total;
    }

    /**
     * @return the outstanding income tax liability
     */
    public long getIncomeTaxLiability() {
	int size = bankAccount.size();
	long income = 0;
	for (int i = size - 1; i >= 0; i--) {
	    Transaction t = bankAccount.getTransaction(i);
	    switch (t.getCategory()) {
		// expenses are -ve and deducted from total income
		case Transaction.CATEGORY_REVENUE:
		case Transaction.CATEGORY_COST_OF_SALES:
		case Transaction.CATEGORY_OPERATING_EXPENSE:
		case Transaction.CATEGORY_INTEREST:
		case Transaction.CATEGORY_CAPITAL_GAIN:
		    income += t.getValue();
		    break;
		case Transaction.CATEGORY_TAX:
		    i = 0;
		    break;
		default:
		    // ignore
	    }
	}
	if (income < 0) {
	    // no need to deduct tax
	    return 0;
	}
	return (income * economy.getIncomeTaxRate() / 100);
    }
    
    /**
     * @return the monthly interest rate in % applied to an account in credit
     */
    public float getCreditAccountInterestRate() {
	float creditRate = (float) (economy.getBaseInterestRate() - 2.0);
	if (creditRate <= 0.0) 
	    return (float) 0.0;
	
	return (float) Economy.aerToMonthly(creditRate);
    }

    /**
     * @return the monthly interest rate in % applied to an account with an
     * overdraft.
     */
    public float getOverdraftInterestRate() {
	float creditRate = (float) (economy.getBaseInterestRate() + 2.0);
	return (float) Economy.aerToMonthly(creditRate);
    }
}
