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
package freerails.model.finance.transaction;

import freerails.model.finance.Money;
import freerails.model.game.Time;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * A Transaction is a change in a player's bank balance and/or assets.
 *
 * For example, the cost of buying a train.
 */
public class Transaction implements Serializable {

    private static final long serialVersionUID = 3258416144497782835L;
    private final TransactionCategory category;
    private final Money amount;
    private final Time time;

    /**
     * @param category
     * @param amount
     */
    public Transaction(@NotNull TransactionCategory category, @NotNull Money amount, @NotNull Time time) {
        this.category = category;
        this.amount = amount;
        this.time = time;
    }

    /**
     * Positive means credit.
     */
    public Money getAmount() {
        return amount;
    }

    public Time getTime() {
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Transaction) {
            Transaction other = (Transaction) obj;
            return other.amount.equals(amount) && category == other.category;
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