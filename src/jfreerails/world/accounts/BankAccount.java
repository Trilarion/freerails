/*
 * Created on 24-Jun-2003
 *
 */
package jfreerails.world.accounts;

import java.util.ArrayList;
import jfreerails.world.common.FreerailsSerializable;

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
