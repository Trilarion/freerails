/*
 * Created on Mar 29, 2004
 */
package jfreerails.world.top;

import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;


/**
 * @author Luke
 *
 */
public abstract class TransactionAggregator {
    protected final ReadOnlyWorld w;
    protected final FreerailsPrincipal principal;

    public TransactionAggregator(ReadOnlyWorld w, FreerailsPrincipal principal) {
        this.w = w;
        this.principal = principal;
    }

    public Money calulateValue() {
        long amount = 0;

        for (int i = 0; i < w.getNumberOfTransactions(this.principal); i++) {
            Transaction t = w.getTransaction(i, principal);
            GameTime time = w.getTransactionTimeStamp(i, principal);

            if (condition(i)) {
                amount += t.getValue().getAmount();
            }
        }

        return new Money(amount);
    }

    abstract protected boolean condition(int transactionID);
}