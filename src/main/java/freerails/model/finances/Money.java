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

import java.io.Serializable;
import java.text.DecimalFormat;

// TODO sum of two money objects and multiplication with integer, double
/**
 * Represents an immutable amount of Money.
 */
public final class Money implements Serializable, Comparable<Money> {

    private static final long serialVersionUID = 3258697615163338805L;
    public static final Money ZERO = new Money(0);
    private static final DecimalFormat df = new DecimalFormat("#,###");
    public final long amount;

    /**
     * @param amount
     */
    public Money(long amount) {
        this.amount = amount;
    }

    /**
     *
     * @param moneyA
     * @param moneyB
     * @return
     */
    public static Money add(Money moneyA, Money moneyB) {
        return new Money(moneyA.amount + moneyB.amount);
    }

    /**
     *
     * @param moneyA
     * @param moneyB
     * @return
     */
    public static Money subtract(Money moneyA, Money moneyB) {
        return new Money(moneyA.amount - moneyB.amount);
    }

    /**
     *
     * @param money
     * @param factor
     * @return
     */
    public static Money multiply(Money money, long factor) {
        return new Money(factor * money.amount);
    }

    /**
     *
     * Note: An integer division (rounding towards zero) is performed.
     *
     * @param money
     * @param factor
     * @return
     */
    public static Money divide(Money money, long factor) {
        return new Money(factor / money.amount);
    }

    /**
     * Convenience function.
     *
     * @param money
     * @return
     */
    public static Money opposite(Money money) {
        return Money.multiply(money, -1);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Money)) return false;

        final Money other = (Money) obj;
        return amount == other.amount;
    }

    // TODO add currency here (not in the client)
    @Override
    public String toString() {
        return df.format(amount);
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Money o) {
        return Long.signum(amount - o.amount);
    }
}