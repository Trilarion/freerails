/*
 * Created on 24-Jun-2003
 *
 */
package jfreerails.world.accounts;

import java.util.ArrayList;
import jfreerails.world.common.FreerailsMutableSerializable;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;


/**
 * Stores a series of {@link Transaction} objects.
 * @author Luke Lindsay
 *
 */
public class BankAccount implements FreerailsMutableSerializable {
    private final ArrayList transactions = new ArrayList();
    private final ArrayList transactionsTimeStamps = new ArrayList();
    private Money currentBalance = new Money(0);

    public BankAccount() {
    }

    public int hashCode() {
        return transactions.size();
    }

    public Money getCurrentBalance() {
        return currentBalance;
    }

    public int size() {
        return transactions.size();
    }

    public void addTransaction(Transaction t, GameTime time) {
        transactions.add(t);
        transactionsTimeStamps.add(time);
        this.currentBalance = new Money(currentBalance.getAmount() +
                t.getValue().getAmount());
    }

    public Transaction removeLastTransaction() {
        int last = transactions.size() - 1;
        Transaction t = (Transaction)transactions.remove(last);
        transactionsTimeStamps.remove(last);
        this.currentBalance = new Money(currentBalance.getAmount() -
                t.getValue().getAmount());

        return t;
    }

    public Transaction getTransaction(int i) {
        return (Transaction)transactions.get(i);
    }

    public GameTime getTimeStamp(int transactionsId) {
        return (GameTime)transactionsTimeStamps.get(transactionsId);
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