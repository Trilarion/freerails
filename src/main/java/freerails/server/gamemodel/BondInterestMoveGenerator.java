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

package freerails.server.gamemodel;

import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.network.movereceiver.MoveReceiver;
import freerails.world.world.ReadOnlyWorld;
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
     * @param moveReceiver
     */
    public BondInterestMoveGenerator(MoveReceiver moveReceiver) {
        this.moveReceiver = moveReceiver;
    }

    private static Move generateMove(ReadOnlyWorld world, FreerailsPrincipal principal) {
        long interestDue = 0;

        for (int i = 0; i < world.getNumberOfTransactions(principal); i++) {
            Transaction transaction = world.getTransaction(principal, i);

            if (transaction instanceof BondItemTransaction) {
                BondItemTransaction bondItemTransaction = (BondItemTransaction) transaction;
                int interestRate = bondItemTransaction.getType();
                // TODO Money arithmetics
                long bondAmount = WorldConstants.BOND_VALUE_ISSUE.amount;
                interestDue += (interestRate * bondAmount / 100) * bondItemTransaction.getQuantity();
            }
        }

        Transaction transaction = new MoneyTransaction(new Money(-interestDue), TransactionCategory.INTEREST_CHARGE);

        return new AddTransactionMove(principal, transaction);
    }

    /**
     * @param world
     */
    public void update(ReadOnlyWorld world) {
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = world.getPlayer(i).getPrincipal();
            Move move = generateMove(world, principal);
            moveReceiver.process(move);
        }
    }
}