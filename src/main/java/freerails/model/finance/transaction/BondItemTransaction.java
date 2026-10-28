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

package freerails.model.finance.transaction;

import freerails.model.finance.Money;
import freerails.model.game.Time;

import java.io.Serial;
import java.util.Objects;

/** A transaction that adds or removes one or more bonds from a player's assets. */
public class BondItemTransaction extends ItemTransaction {

    @Serial
    private static final long serialVersionUID = 3257562923491473465L;
    private final double rate;

    /**
     * Creates a bond transaction with its amount, time, quantity, and interest
     * rate.
     *
     * @param amount monetary value recorded by the transaction
     * @param time game time when the transaction occurs
     * @param quantity positive to issue bonds, negative to repay them
     * @param rate annual interest rate associated with the bonds
     */
    public BondItemTransaction(Money amount, Time time, int quantity, double rate) {
        super(TransactionCategory.BOND, amount, time, quantity, -1);
        this.rate = rate;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && Double.compare(rate, ((BondItemTransaction) obj).rate) == 0;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rate);
    }
}