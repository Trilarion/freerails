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

package freerails.model.finances.transactions;

import freerails.model.finances.Money;

/**
 * A Transaction that adds or removes a Bond.
 */
public class BondItemTransaction extends ItemTransaction {

    private static final long serialVersionUID = 3257562923491473465L;
    private final double rate;

    public BondItemTransaction(Money amount, int quantity, double rate) {
        super(TransactionCategory.BOND, amount, quantity, -1);
        this.rate = rate;
    }

    /**
     *
     * @return
     */
    public double getRate() {
        return rate;
    }
}