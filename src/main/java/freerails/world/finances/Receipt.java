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
 * Created on 21-Jun-2003
 *
 */
package freerails.world.finances;

/**
 * A credit.
 */
public class Receipt implements Transaction {
    private static final long serialVersionUID = 3617576007066924596L;

    private final Money amount;

    private final Category category;

    /**
     * @param m
     * @param category
     */
    public Receipt(Money m, Category category) {
        this.amount = m;
        this.category = category;
    }

    /**
     * @return
     */
    public Money deltaAssets() {
        return amount.changeSign();
    }

    public Money deltaCash() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Receipt) {
            Receipt test = (Receipt) o;

            return test.amount.equals(amount) && category == test.category;
        }
        return false;
    }

    /**
     * @return
     */
    public Category getCategory() {
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