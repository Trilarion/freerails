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

/*
 *
 */
package freerails.model.finance.transaction;

import freerails.model.finance.Money;
import freerails.model.game.Time;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Transaction is a change in a player's bank balance and/or assets.
 * <p>
 * For example, the cost of buying a train.
 */
public class Transaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 3258416144497782835L;
    private final TransactionCategory category;
    private final Money amount;
    private final Time time;

    // TODO some safety mechanisms that gives times are not completely off
    /**
     * Creates a transaction with its category, amount, and game time.
     *
     * @param category kind of transaction
     * @param amount monetary debit or credit
     * @param time game time when the transaction occurs
     */
    public Transaction(@NotNull TransactionCategory category, @NotNull Money amount, @NotNull Time time) {
        this.category = category;
        this.amount = amount;
        this.time = time;
    }

    /**
     * Returns the transaction amount; positive amounts represent credits.
     *
     * @return transaction amount
     */
    public Money getAmount() {
        return amount;
    }

    /**
     * Returns the game time at which this transaction occurred.
     *
     * @return transaction time
     */
    public Time getTime() {
        return time;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Transaction other = (Transaction) obj;
        return category == other.category
                && amount.equals(other.amount)
                && time.equals(other.time);
    }

    /**
     * Returns the category describing this transaction.
     *
     * @return transaction category
     */
    public TransactionCategory getCategory() {
        return category;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(category, amount, time);
    }
}