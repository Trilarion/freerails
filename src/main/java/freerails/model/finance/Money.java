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

package freerails.model.finance;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Represents an immutable amount of money in the game's unit of account.
 */
public record Money(long amount) implements Serializable, Comparable<Money> {

    private static final long serialVersionUID = 3258697615163338805L;
    public static final Money ZERO = new Money(0);

    /**
     * Creates an amount in the game's smallest accounting unit.
     */
    public Money {
    }

    /**
     * Returns whether this amount is zero or positive.
     *
     * @return {@code true} when the amount is nonnegative
     */
    public boolean isNonNegative() {
        return amount >= 0;
    }

    /**
     * Adds two amounts, checking for arithmetic overflow.
     *
     * @param moneyA first amount
     * @param moneyB second amount
     * @return their sum
     * @throws ArithmeticException if the sum overflows
     */
    public static Money add(Money moneyA, Money moneyB) {
        return new Money(Math.addExact(moneyA.amount, moneyB.amount));
    }

    /**
     * Subtracts the second amount from the first, checking for overflow.
     *
     * @param moneyA amount to subtract from
     * @param moneyB amount to subtract
     * @return the difference
     * @throws ArithmeticException if the difference overflows
     */
    public static Money subtract(Money moneyA, Money moneyB) {
        return new Money(Math.subtractExact(moneyA.amount, moneyB.amount));
    }

    /**
     * Multiplies an amount by an integer factor, checking for overflow.
     *
     * @param money  amount to multiply
     * @param factor multiplier
     * @return the product
     * @throws ArithmeticException if the product overflows
     */
    public static Money multiply(Money money, long factor) {
        return new Money(Math.multiplyExact(factor, money.amount));
    }

    /**
     * Note: An integer division (rounding towards zero) is performed.
     *
     * @param money  amount to divide
     * @param factor divisor
     * @return the quotient, truncated toward zero
     * @throws ArithmeticException if {@code factor} is zero
     */
    public static Money divide(Money money, long factor) {
        return new Money(money.amount / factor);
    }

    /**
     * Convenience function.
     *
     * @param money amount to negate
     * @return the amount with its sign reversed
     * @throws ArithmeticException if negation overflows
     */
    public static Money opposite(Money money) {
        return Money.multiply(money, -1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Long.hashCode(amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Money other)) return false;
        return compareTo(other) == 0;
    }

    // TODO add currency here (not in the client)

    /**
     * Returns this amount formatted with grouping separators and no currency
     * symbol.
     *
     * @return formatted amount
     */
    @Override
    public String toString() {
        return new DecimalFormat("#,###").format(amount);
    }

    /**
     * Compares amounts by their numeric value.
     *
     * @param o amount to compare with
     * @return a negative value, zero, or a positive value as this amount is
     * smaller than, equal to, or greater than {@code o}
     */
    @Override
    public int compareTo(Money o) {
        return Long.compare(amount, o.amount);
    }

}