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

package freerails.model.finances;

// TODO remove type from here and
/**
 * This Transaction represents the charge/credit for buying/selling an item.
 *
 * Characterized by a category, a type, a quantity and an amount of money of course.
 *
 * Example: Buy 4 tiles of standard track
 */
public class ItemTransaction implements Transaction {

    private static final long serialVersionUID = 3690471411852326457L;
    private final Money amount;
    private final TransactionCategory category;
    private final int quantity;
    private final int type;


    // TODO what is the difference between category and type??

    /**
     * @param category
     * @param type
     * @param quantity
     * @param amount
     */
    public ItemTransaction(TransactionCategory category, int type, int quantity, Money amount) {
        this.category = category;
        this.type = type;
        this.quantity = quantity;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemTransaction) {
            ItemTransaction test = (ItemTransaction) obj;

            return amount.equals(test.amount) && category == test.category && type == test.type && quantity == test.quantity;
        }
        return false;
    }

    public Money price() {
        return amount;
    }

    /**
     * @return
     */
    public TransactionCategory getCategory() {
        return category;
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
    public int getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int result;
        result = category.hashCode();
        result = 29 * result + type;
        result = 29 * result + quantity;
        result = 29 * result + amount.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ItemTransaction " + category + ", type " + type + ", quantity " + quantity + ", amount " + amount;
    }
}