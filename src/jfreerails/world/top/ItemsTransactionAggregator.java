/*
 * Created on Mar 30, 2004
 */
package jfreerails.world.top;

import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;


/**
 * Adds up the number of assets.
 *
 * @author Luke
 *
 */
public class ItemsTransactionAggregator extends TransactionAggregator {
    public static final int ANY_VALUE = Integer.MIN_VALUE;
    private int type = ANY_VALUE;
    private int category = ANY_VALUE;

    //private static GameCalendar calendar;
    //private int startYear = 0;
    private int[] quantities;
    private int quantityRunningTotal;

    public static class QuantitiesAndValues {
        public int[] quantities;
        public Money[] values;
    }

    public ItemsTransactionAggregator(ReadOnlyWorld w,
        FreerailsPrincipal principal) {
        super(w, principal);
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

        return isCategoryAcceptable && isTypeAcceptable;
    }

    public int calculateQuantity() {
        QuantitiesAndValues qnv = calculateQuantitiesAndValues();

        return qnv.quantities[0];
    }

    public QuantitiesAndValues calculateQuantitiesAndValues() {
        QuantitiesAndValues returnValue = new QuantitiesAndValues();
        returnValue.values = super.calculateValues();
        returnValue.quantities = this.quantities;

        return returnValue;
    }

    protected void incrementRunningTotal(int transactionID) {
        super.incrementRunningTotal(transactionID);

        Transaction t = w.getTransaction(transactionID, principal);
        AddItemTransaction addItemTransaction = (AddItemTransaction)t;
        quantityRunningTotal += addItemTransaction.getQuantity();
    }

    protected void setTotalsArrayLength(int length) {
        super.setTotalsArrayLength(length);
        quantities = new int[length];
        quantityRunningTotal = 0;
    }

    protected void storeRunningTotal(int timeIndex) {
        /*Note, a negative sign since we are totalling
         * the value of assets not their impact on the
         * operating funds.
         */
        monetaryTotals[timeIndex] = new Money(-runningTotal);
        quantities[timeIndex] = quantityRunningTotal;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}