/*
 * Created on 07-Jul-2003
 *
 */
package jfreerails.move;

import jfreerails.world.accounts.Transaction;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.World;


/** This {@link Move} adds a {@link Transaction} to a players
 * bank account on the {@link World} object.
 * @author Luke Lindsay
 *
 */
public class AddTransactionMove implements Move {
    private final Transaction transaction;
    private final FreerailsPrincipal principal;

    /** Whether the move fails if there is not enough cash. */
    private final boolean constrained;

    public Transaction getTransaction() {
        return transaction;
    }

    public AddTransactionMove(FreerailsPrincipal account, Transaction t) {
        if (null == t) {
            throw new NullPointerException();
        }

        this.principal = account;
        this.transaction = t;
        constrained = false;
    }

    public AddTransactionMove(FreerailsPrincipal account, Transaction t,
        boolean constrained) {
        this.principal = account;
        this.transaction = t;
        this.constrained = constrained;

        if (null == t) {
            throw new NullPointerException();
        }
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (w.isPlayer(principal)) {
            if (this.constrained) {
                long bankBalance = w.getCurrentBalance(principal).getAmount();
                long transactionAmount = this.transaction.getValue().getAmount();
                long balanceAfter = bankBalance + transactionAmount;

                if (transactionAmount < 0 && balanceAfter < 0) {
                    return MoveStatus.moveFailed("You can't afford that!");
                }
            }

            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.moveFailed(p.getName() +
                " does not have a bank account.");
        }
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        int size = w.getNumberOfTransactions(this.principal);

        if (0 == size) {
            return MoveStatus.moveFailed("No transactions to remove!");
        }

        Transaction lastTransaction = w.getTransaction(size - 1, this.principal);

        if (lastTransaction.equals(this.transaction)) {
            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.moveFailed("Expected " + this.transaction +
                "but found " + lastTransaction);
        }
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);

        if (ms.ok) {
            w.addTransaction(this.transaction, this.principal);
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

    public boolean equals(Object obj) {
        if (obj instanceof AddTransactionMove) {
            AddTransactionMove test = (AddTransactionMove)obj;

            return test.principal.equals(this.principal) &&
            test.transaction.equals(this.transaction);
        } else {
            return false;
        }
    }

    public FreerailsPrincipal getPrincipal() {
        return principal;
    }
}