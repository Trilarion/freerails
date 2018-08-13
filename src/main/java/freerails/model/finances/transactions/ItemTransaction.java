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

package freerails.model.finances.transactions;

// TODO implement hashcode and equals correctly (with calls to super)
import freerails.model.finances.Money;

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
     * @param category
     * @param amount
     * @param quantity
     * @param id
     */
    public ItemTransaction(TransactionCategory category, Money amount, int quantity, int id) {
        super(category, amount);
        this.quantity = quantity;
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemTransaction) {
            ItemTransaction test = (ItemTransaction) obj;

            return id == test.id && quantity == test.quantity;
        }
        return false;
    }

    /**
     * @return
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return
     */
    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + id;
        result = 29 * result + quantity;
        return result;
    }

    @Override
    public String toString() {
        return "ItemTransaction " + getQuantity() + ", type " + id + ", quantity " + quantity + ", money " + getAmount();
    }
}