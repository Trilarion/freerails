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
import freerails.move.receiver.MoveReceiver;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.WorldConstants;
import freerails.model.finances.*;
import freerails.model.player.FreerailsPrincipal;

/**
 * Iterates over the entries in the bank account and counts the number
 * of outstanding bonds, then calculates the interest due.
 */
public class BondInterestMoveGenerator {

    private final MoveReceiver moveReceiver;

    /**
     * @param moveReceiver
     */
    public BondInterestMoveGenerator(MoveReceiver moveReceiver) {
        this.moveReceiver = moveReceiver;
    }

    /**
     * @param world
     */
    public void update(ReadOnlyWorld world) {
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = world.getPlayer(i).getPrincipal();
            long interestDue = 0;

            for (int i1 = 0; i1 < world.getNumberOfTransactions(principal); i1++) {
                Transaction transaction = world.getTransaction(principal, i1);

                if (transaction instanceof BondItemTransaction) {
                    BondItemTransaction bondItemTransaction = (BondItemTransaction) transaction;
                    int interestRate = bondItemTransaction.getType();
                    // TODO Money arithmetics
                    long bondAmount = WorldConstants.BOND_VALUE_ISSUE.amount;
                    interestDue += (interestRate * bondAmount / 100) * bondItemTransaction.getQuantity();
                }
            }

            Transaction transaction = new MoneyTransaction(new Money(-interestDue), TransactionCategory.INTEREST_CHARGE);

            Move move = new AddTransactionMove(principal, transaction);
            moveReceiver.process(move);
        }
    }
}