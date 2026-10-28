/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.model.finance.transaction;

import freerails.model.finance.Money;
import freerails.model.game.Time;

import java.util.Objects;

/**
 * This Transaction represents the charge/credit for buying/selling an item.
 *
 * Characterized by a category, a type, a quantity and an amount of money of course.
 *
 * Example: Buy 4 tiles of standard track
 *
 * Stock transaction: A transaction that occurs when a new company is founded or when a company
 * issues additional shares. Additionally to the values necessary for an item transaction also a player id is needed.
 */
public class ItemTransaction extends Transaction {

    private static final long serialVersionUID = 3690471411852326457L;
    private final int quantity;
    private final int id;

    /**
     * Creates a transaction for a change in the quantity of a categorized item.
     *
     * @param category category identifying the item transaction
     * @param amount monetary debit or credit
     * @param time game time when the transaction occurs
     * @param quantity signed quantity change
     * @param id item type or associated player identifier
     */
    public ItemTransaction(TransactionCategory category, Money amount, Time time, int quantity, int id) {
        super(category, amount, time);
        this.quantity = quantity;
        this.id = id;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        ItemTransaction test = (ItemTransaction) obj;
        return id == test.id && quantity == test.quantity;
    }

    /**
     * Returns the signed number of items added or removed.
     *
     * @return quantity change
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the type or player identifier associated with this item.
     *
     * @return item type or player identifier
     */
    public int getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, quantity);
    }

    /**
     * Returns a diagnostic string containing this transaction's item details.
     *
     * @return formatted transaction details
     */
    @Override
    public String toString() {
        return "ItemTransaction " + getQuantity() + ", type " + id + ", quantity " + quantity + ", money " + getAmount();
    }
}