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

import freerails.world.WorldConstants;

/**
 * A Transaction that adds or removes a Bond.
 */
public class BondItemTransaction extends ItemTransaction {

    private static final long serialVersionUID = 3257562923491473465L;

    // TODO how many values for category are really possible here, if only one, set it internally

    private BondItemTransaction(TransactionCategory category, double rate, int quantity, Money amount) {
        // TODO item transaction only understands ints, this should not be a type, but a double rate
        super(category, (int)rate, quantity, amount);
    }

    // TODO better place for these static methods
    /**
     * @param interestRate
     * @return
     */
    public static BondItemTransaction issueBond(double interestRate) {
        return new BondItemTransaction(TransactionCategory.BOND, interestRate, 1,
                WorldConstants.BOND_VALUE_ISSUE);
    }

    /**
     * @param interestRate
     * @return
     */
    public static BondItemTransaction repayBond(int interestRate) {
        return new BondItemTransaction(TransactionCategory.BOND, interestRate, -1,
                WorldConstants.BOND_VALUE_REPAY);
    }
}