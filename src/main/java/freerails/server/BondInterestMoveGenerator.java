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

package freerails.server;

import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.network.MoveReceiver;
import freerails.world.ReadOnlyWorld;
import freerails.world.WorldConstants;
import freerails.world.finances.*;
import freerails.world.player.FreerailsPrincipal;

/**
 * Iterates over the entries in the bank account and counts the number
 * of outstanding bonds, then calculates the interest due.
 */
public class BondInterestMoveGenerator {

    private final MoveReceiver moveReceiver;

    /**
     * @param mr
     */
    public BondInterestMoveGenerator(MoveReceiver mr) {
        moveReceiver = mr;
    }

    private static Move generateMove(ReadOnlyWorld w, FreerailsPrincipal principal) {
        long interestDue = 0;

        for (int i = 0; i < w.getNumberOfTransactions(principal); i++) {
            Transaction t = w.getTransaction(principal, i);

            if (t instanceof BondItemTransaction) {
                BondItemTransaction bt = (BondItemTransaction) t;
                int interestRate = bt.getType();
                long bondAmount = WorldConstants.BOND_VALUE_ISSUE.getAmount();
                interestDue += (interestRate * bondAmount / 100) * bt.getQuantity();
            }
        }

        Transaction t = new MoneyTransaction(new Money(-interestDue), TransactionCategory.INTEREST_CHARGE);

        return new AddTransactionMove(principal, t);
    }

    /**
     * @param w
     */
    public void update(ReadOnlyWorld w) {
        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = w.getPlayer(i).getPrincipal();
            Move m = generateMove(w, principal);
            moveReceiver.process(m);
        }
    }
}