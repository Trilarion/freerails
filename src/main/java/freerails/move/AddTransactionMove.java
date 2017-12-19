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

/*
 * Created on 07-Jul-2003
 *
 */
package freerails.move;

import freerails.world.finances.Transaction;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.World;

/**
 * This {@link Move} adds a {@link Transaction} to a players bank account on the
 * {@link World} object.
 */
public class AddTransactionMove implements Move {
    private static final long serialVersionUID = 3976738055925019701L;

    private final Transaction transaction;

    private final FreerailsPrincipal principal;

    /**
     * Whether the move fails if there is not enough cash.
     */
    private final boolean constrained;

    /**
     * @param account
     * @param t
     */
    public AddTransactionMove(FreerailsPrincipal account, Transaction t) {
        if (null == t) {
            throw new NullPointerException();
        }

        principal = account;
        transaction = t;
        constrained = false;
    }

    /**
     * @param account
     * @param t
     * @param constrain
     */
    public AddTransactionMove(FreerailsPrincipal account, Transaction t,
                              boolean constrain) {
        principal = account;
        transaction = t;
        constrained = constrain;

        if (null == t) {
            throw new NullPointerException();
        }
    }

    /**
     * @return
     */
    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public int hashCode() {
        int result;
        result = transaction.hashCode();
        result = 29 * result + principal.hashCode();
        result = 29 * result + (constrained ? 1 : 0);

        return result;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (w.isPlayer(principal)) {
            if (this.constrained) {
                long bankBalance = w.getCurrentBalance(principal).getAmount();
                long transactionAmount = this.transaction.deltaCash()
                        .getAmount();
                long balanceAfter = bankBalance + transactionAmount;

                if (transactionAmount < 0 && balanceAfter < 0) {
                    return MoveStatus.moveFailed("You can't afford that!");
                }
            }

            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed(p.getName()
                + " does not have a bank account.");
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        int size = w.getNumberOfTransactions(this.principal);

        if (0 == size) {
            return MoveStatus.moveFailed("No transactions to remove!");
        }

        Transaction lastTransaction = w
                .getTransaction(this.principal, size - 1);

        if (lastTransaction.equals(this.transaction)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed("Expected " + this.transaction
                + "but found " + lastTransaction);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);

        if (ms.ok) {
            w.addTransaction(this.principal, this.transaction);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);

        if (ms.ok) {
            w.removeLastTransaction(this.principal);
        }

        return ms;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AddTransactionMove) {
            AddTransactionMove test = (AddTransactionMove) obj;

            return test.principal.equals(this.principal)
                    && test.transaction.equals(this.transaction);
        }
        return false;
    }

    /**
     * @return
     */
    public FreerailsPrincipal getPrincipal() {
        return principal;
    }
}