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
package freerails.world.finances;

/**
 * A credit.
 */
// TODO what is the difference between this and a MoneyTransaction? If none, merge
public class MoneyTransaction2 implements Transaction {
    private static final long serialVersionUID = 3617576007066924596L;

    private final Money amount;

    private final TransactionCategory category;

    /**
     * @param m
     * @param category
     */
    public MoneyTransaction2(Money m, TransactionCategory category) {
        this.amount = m;
        this.category = category;
    }

    public Money deltaCash() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MoneyTransaction2) {
            MoneyTransaction2 test = (MoneyTransaction2) o;

            return test.amount.equals(amount) && category == test.category;
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
        result = amount.hashCode();
        result = 29 * result + category.hashCode();

        return result;
    }
}