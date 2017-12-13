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
 * Created on 24-Jun-2003
 *
 */
package org.railz.world.accounts;

import java.util.ArrayList;
import org.railz.world.common.FreerailsSerializable;

/**
 * @author Luke Lindsay
 *
 */
public class BankAccount implements FreerailsSerializable {
    private final ArrayList transactions = new ArrayList();
    private long currentBalance = 0;

    public BankAccount() {
    }

    public long getCurrentBalance() {
        return currentBalance;
    }

    public int size() {
        return transactions.size();
    }

    public void addTransaction(Transaction t) {
        transactions.add(t);
        this.currentBalance = currentBalance + t.getValue();
    }

    public Transaction removeLastTransaction() {
        int last = transactions.size() - 1;
        Transaction t = (Transaction)transactions.remove(last);
        this.currentBalance = currentBalance - t.getValue();

        return t;
    }

    public Transaction getTransaction(int i) {
        return (Transaction)transactions.get(i);
    }

    public boolean equals(Object o) {
        if (o instanceof BankAccount) {
            BankAccount test = (BankAccount)o;

            return this.transactions.equals(test.transactions);
            //No need to look at the current balance field since it 
            //can be calculated by looking at the transactions.
        } else {
            return false;
        }
    }
}
