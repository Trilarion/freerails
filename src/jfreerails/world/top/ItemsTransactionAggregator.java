/*
 * Created on Mar 30, 2004
 */
package jfreerails.world.top;

import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;


/**
 * @author Luke
 *
 */
public class ItemsTransactionAggregator extends TransactionAggregator {
    public static final int ANY_VALUE = Integer.MIN_VALUE;
    private int type = ANY_VALUE;
    private int category = ANY_VALUE;
    private static GameCalendar calendar;
    private int startYear = 0;

    public ItemsTransactionAggregator(ReadOnlyWorld w,
        FreerailsPrincipal principal) {
        super(w, principal);
        calendar = (GameCalendar)w.get(ITEM.CALENDAR);
    }

    /**
     * Returns true if the transaction with the specified ID has an acceptable
     * type and category.
     */
    protected boolean condition(int transactionID) {
        Transaction t = w.getTransaction(transactionID, principal);

        if (!(t instanceof AddItemTransaction)) {
            return false;
        }

        AddItemTransaction addItemTransaction = (AddItemTransaction)t;
        boolean isTypeAcceptable = (type == ANY_VALUE) ||
            (type == addItemTransaction.getType());
        boolean isCategoryAcceptable = (category == ANY_VALUE) ||
            (category == addItemTransaction.getCategory());
        int transactionTime = w.getTransactionTimeStamp(transactionID, principal)
                               .getTime();
        int transactionYear = calendar.getYear(transactionTime);

        return isCategoryAcceptable && isTypeAcceptable &&
        transactionYear >= startYear;
    }

    public int calulateQuantity() {
        int quantity = 0;

        for (int i = 0; i < w.getNumberOfTransactions(this.principal); i++) {
            Transaction t = w.getTransaction(i, principal);
            GameTime time = w.getTransactionTimeStamp(i, principal);

            if (condition(i)) {
                AddItemTransaction addItemTransaction = (AddItemTransaction)t;
                quantity += addItemTransaction.getQuantity();
            }
        }

        return quantity;
    }

    public Money calulateAssetValue() {
        return new Money(-super.calulateValue().getAmount());
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}