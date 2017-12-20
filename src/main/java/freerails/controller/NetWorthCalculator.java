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

package freerails.controller;

import freerails.world.ReadOnlyWorld;
import freerails.world.TransactionAggregator;
import freerails.world.finances.ItemTransaction;
import freerails.world.finances.Transaction;
import freerails.world.finances.TransactionCategory;
import freerails.world.player.FreerailsPrincipal;

/**
 * A TransactionAggregator that calculates the net worth of a player by totalling
 * the value of their assets.
 */
public class NetWorthCalculator extends TransactionAggregator {

    /**
     * @param w
     * @param principal
     */
    public NetWorthCalculator(ReadOnlyWorld w, FreerailsPrincipal principal) {
        super(w, principal);
    }

    @Override
    protected boolean condition(int transactionID) {
        Transaction t = super.w.getTransaction(super.principal, transactionID);

        if (t instanceof ItemTransaction) {
            return t.getCategory().equals(TransactionCategory.ISSUE_STOCK);
            // Since buying something is just converting one asset type to
            // another.
        }

        return true;
    }
}