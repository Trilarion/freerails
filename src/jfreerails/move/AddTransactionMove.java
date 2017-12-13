/*
 * Created on 07-Jul-2003
 *
 */
package jfreerails.move;

import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.player.FreerailsPrincipal;

/** This {@link Move} adds a {@link Transaction} to a players
 * {@link BankAccount}.
 * @author Luke Lindsay
 *
 */
public class AddTransactionMove implements Move {
    private final Transaction transaction;
    private final int account;
    private final FreerailsPrincipal principal;

    /** Whether the move fails if there is not enough cash. */
    private final boolean constrained;

    public FreerailsPrincipal getPrincipal() {
	return principal;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public AddTransactionMove(int account, Transaction t, FreerailsPrincipal
	    p) {
	this(account, t, false, p);
    }

    public AddTransactionMove(int account, Transaction t, boolean constrained,
	    FreerailsPrincipal principal) {
        this.account = account;
        this.transaction = t;
        this.constrained = constrained;
	this.principal = principal;

        if (null == t) {
            throw new NullPointerException();
        }
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (w.boundsContain(KEY.BANK_ACCOUNTS, account, p)) {
            if (this.constrained) {
                BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS,
                        this.account, p);
                long bankBalance = bankAccount.getCurrentBalance();
                long transactionAmount = this.transaction.getValue();
                long balanceAfter = bankBalance + transactionAmount;

                if (transactionAmount < 0 && balanceAfter < 0) {
                    return MoveStatus.moveFailed("You can't afford that!");
                }
            }

            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.MOVE_FAILED;
        }
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        if (w.boundsContain(KEY.BANK_ACCOUNTS, account, p)) {
            BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS,
                    this.account, p);
            int size = bankAccount.size();

            if (0 == size) {
                return MoveStatus.MOVE_FAILED;
            }

            Transaction lastTransaction = bankAccount.getTransaction(size - 1);

            if (lastTransaction.equals(this.transaction)) {
                return MoveStatus.MOVE_OK;
            } else {
                return MoveStatus.MOVE_FAILED;
            }
        } else {
            return MoveStatus.MOVE_FAILED;
        }
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);

        if (ms.ok) {
            BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS,
                    this.account, p);
            bankAccount.addTransaction(this.transaction);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);

        if (ms.ok) {
            BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS,
                    this.account, p);
            bankAccount.removeLastTransaction();
        }

        return ms;
    }

    public boolean equals(Object obj) {
        if (obj instanceof AddTransactionMove) {
            AddTransactionMove test = (AddTransactionMove)obj;

            return test.account == this.account &&
            test.transaction.equals(this.transaction) &&
	    test.principal.equals(principal);
        } else {
            return false;
        }
    }

    public String toString() {
	return "AddTransactionMove: account = " + account + ", " +
	    "tramsaction = " + transaction + ", principal = " + principal;
    }
}
