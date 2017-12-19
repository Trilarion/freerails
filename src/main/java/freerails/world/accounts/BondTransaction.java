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

package freerails.world.accounts;

import freerails.world.common.Money;

/**
 * A Transaction that adds or removes a Bond.
 *
 */
public class BondTransaction extends AddItemTransaction {

    /**
     *
     */
    public static final Money BOND_VALUE_ISSUE = new Money(500000);

    /**
     *
     */
    public static final Money BOND_VALUE_REPAY = new Money(-500000);
    private static final long serialVersionUID = 3257562923491473465L;

    private BondTransaction(Category category, int type, int quantity,
                            Money amount) {
        super(category, type, quantity, amount);
    }

    /**
     *
     * @param interestRate
     * @return
     */
    public static BondTransaction issueBond(int interestRate) {
        return new BondTransaction(Category.BOND, interestRate, 1,
                BOND_VALUE_ISSUE);
    }

    /**
     *
     * @param interestRate
     * @return
     */
    public static BondTransaction repayBond(int interestRate) {
        return new BondTransaction(Category.BOND, interestRate, -1,
                BOND_VALUE_REPAY);
    }
}