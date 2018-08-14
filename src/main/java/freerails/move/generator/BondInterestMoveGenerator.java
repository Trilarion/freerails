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

package freerails.move.generator;

import freerails.model.finances.transactions.BondItemTransaction;
import freerails.model.finances.transactions.Transaction;
import freerails.model.finances.transactions.TransactionCategory;
import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.ModelConstants;
import freerails.model.finances.*;
import freerails.model.player.Player;

import java.util.ArrayList;
import java.util.List;

// TODO generates multiple moves, good idea?
/**
 * Iterates over the entries in the bank account and counts the number
 * of outstanding bonds, then calculates the interest due.
 */
public class BondInterestMoveGenerator {

    /**
     * @param world
     */
    public static List<Move> generate(UnmodifiableWorld world) {
        List<Move> moves = new ArrayList<>();
        for (Player player: world.getPlayers()) {
            long interestDue = 0;

            for (int i1 = 0; i1 < world.getNumberOfTransactions(player); i1++) {
                Transaction transaction = world.getTransaction(player, i1);

                if (transaction instanceof BondItemTransaction) {
                    BondItemTransaction bondItemTransaction = (BondItemTransaction) transaction;
                    int interestRate = bondItemTransaction.getId();
                    // TODO Money arithmetic
                    long bondAmount = ModelConstants.BOND_VALUE_ISSUE.amount;
                    interestDue += (interestRate * bondAmount / 100) * bondItemTransaction.getQuantity();
                }
            }

            Transaction transaction = new Transaction(TransactionCategory.INTEREST_CHARGE, new Money(-interestDue));

            Move move = new AddTransactionMove(player, transaction);
            moves.add(move);
        }
        return moves;
    }
}