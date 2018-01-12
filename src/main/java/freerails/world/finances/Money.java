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

package freerails.world.finances;

import java.io.Serializable;
import java.text.DecimalFormat;

// TODO sum of two money objects and multiplication with integer, double

/**
 * Represents an immutable amount of Money.
 */
public final class Money implements Serializable {

    public static final Money ZERO_MONEY = new Money(0);
    private static final long serialVersionUID = 3258697615163338805L;
    private static final DecimalFormat df = new DecimalFormat("#,###");
    private final long amount;

    /**
     * @param amount
     */
    public Money(long amount) {
        this.amount = amount;
    }

    /**
     * @return
     */
    public static Money changeSign(Money money) {
        return new Money(-money.amount);
    }

    /**
     * @return
     */
    public long getAmount() {
        return amount;
    }

    @Override
    public int hashCode() {
        return (int) (amount ^ (amount >>> 32));
    }

    // TODO add currrency here (not in the client)
    @Override
    public String toString() {
        return df.format(amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Money) {
            Money test = (Money) obj;

            return test.amount == amount;
        }
        return false;
    }
}