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

/*
 *
 */
package freerails.model.finances;

/**
 * For example, the cost of buying a trains.
 */
public class MoneyTransaction implements Transaction {

    private static final long serialVersionUID = 3258416144497782835L;
    private final Money money;
    private final TransactionCategory category;

    /**
     * @param money
     * @param category
     */
    public MoneyTransaction(Money money, TransactionCategory category) {
        this.money = money;
        this.category = category;
    }

    public Money price() {
        return money;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MoneyTransaction) {
            MoneyTransaction other = (MoneyTransaction) obj;
            return other.money.equals(money) && category == other.category;
        }
        return false;
    }

    /**
     * @return
     */
    public TransactionCategory getCategory() {
        return category;
    }

    @Override
    public int hashCode() {
        int result;
        result = money.hashCode();
        result = 29 * result + category.hashCode();
        return result;
    }
}