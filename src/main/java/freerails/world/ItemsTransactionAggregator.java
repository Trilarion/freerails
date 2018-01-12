/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.world;

import freerails.world.finances.ItemTransaction;
import freerails.world.finances.Money;
import freerails.world.finances.Transaction;
import freerails.world.finances.TransactionCategory;
import freerails.world.player.FreerailsPrincipal;

/**
 * Adds up the number of assets.
 */
public class ItemsTransactionAggregator extends TransactionAggregator {

    /**
     *
     */
    public static final int ANY_VALUE = Integer.MIN_VALUE;

    private int type = ANY_VALUE;

    private TransactionCategory category = null;

    private int[] quantities;

    private int quantityRunningTotal;

    /**
     * @param w
     * @param principal
     */
    public ItemsTransactionAggregator(ReadOnlyWorld w, FreerailsPrincipal principal) {
        super(w, principal);
    }

    /**
     * Returns true if the transaction with the specified ID has an acceptable
     * type and category.
     */
    @Override
    protected boolean condition(int transactionID) {
        Transaction t = w.getTransaction(principal, transactionID);

        if (!(t instanceof ItemTransaction)) {
            return false;
        }

        ItemTransaction itemTransaction = (ItemTransaction) t;
        boolean isTypeAcceptable = (type == ANY_VALUE) || (type == itemTransaction.getType());
        boolean isCategoryAcceptable = (category == null) || (category == itemTransaction.getCategory());

        return isCategoryAcceptable && isTypeAcceptable;
    }

    /**
     * @return
     */
    public int calculateQuantity() {
        QuantitiesAndValues qnv = calculateQuantitiesAndValues();

        return qnv.quantities[0];
    }

    /**
     * @return
     */
    public QuantitiesAndValues calculateQuantitiesAndValues() {
        QuantitiesAndValues returnValue = new QuantitiesAndValues();
        returnValue.values = super.calculateValues();
        returnValue.quantities = quantities;

        return returnValue;
    }

    /**
     * @param transactionID
     */
    @Override
    protected void incrementRunningTotal(int transactionID) {
        super.incrementRunningTotal(transactionID);

        Transaction t = w.getTransaction(principal, transactionID);
        ItemTransaction itemTransaction = (ItemTransaction) t;
        quantityRunningTotal += itemTransaction.getQuantity();
    }

    @Override
    protected void setTotalsArrayLength(int length) {
        super.setTotalsArrayLength(length);
        quantities = new int[length];
        quantityRunningTotal = 0;
    }

    @Override
    protected void storeRunningTotal(int timeIndex) {
        /*
         * Note, a negative sign since we are totalling the value of assets not
         * their impact on the operating funds.
         */
        monetaryTotals[timeIndex] = new Money(-runningTotal);
        quantities[timeIndex] = quantityRunningTotal;
    }

    /**
     * @param category
     */
    public void setCategory(TransactionCategory category) {
        this.category = category;
    }

    /**
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Stores the quantities and monetary values of a series of items.
     */

    public static class QuantitiesAndValues {

        /**
         *
         */
        public int[] quantities;

        /**
         *
         */
        public Money[] values;
    }
}