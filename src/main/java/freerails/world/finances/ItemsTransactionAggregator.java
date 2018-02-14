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

package freerails.world.finances;

import freerails.world.QuantitiesAndValues;
import freerails.world.finances.*;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.world.ReadOnlyWorld;

/**
 * Adds up the number of assets.
 */
public class ItemsTransactionAggregator extends TransactionAggregator {

    private static final int ANY_VALUE = Integer.MIN_VALUE;
    private int type = ANY_VALUE;
    private TransactionCategory category = null;
    private int[] quantities;
    private int quantityRunningTotal;

    /**
     * @param world
     * @param principal
     */
    public ItemsTransactionAggregator(ReadOnlyWorld world, FreerailsPrincipal principal) {
        super(world, principal);
    }

    /**
     * Returns true if the transaction with the specified ID has an acceptable
     * type and category.
     */
    @Override
    protected boolean condition(int transactionID) {
        Transaction transaction = world.getTransaction(principal, transactionID);

        if (!(transaction instanceof ItemTransaction)) {
            return false;
        }

        ItemTransaction itemTransaction = (ItemTransaction) transaction;
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

        Transaction transaction = world.getTransaction(principal, transactionID);
        ItemTransaction itemTransaction = (ItemTransaction) transaction;
        quantityRunningTotal += itemTransaction.getQuantity();
    }

    @Override
    public void setTotalsArrayLength(int length) {
        super.setTotalsArrayLength(length);
        quantities = new int[length];
        quantityRunningTotal = 0;
    }

    @Override
    public void storeRunningTotal(int timeIndex) {
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

}