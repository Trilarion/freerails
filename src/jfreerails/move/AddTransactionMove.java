/*
 * Created on 07-Jul-2003
 *
 */
package jfreerails.move;

import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;


/** This {@link Move} adds a {@link Transaction} to a players
 * {@link BankAccount}.
 * @author Luke Lindsay
 *
 */
public class AddTransactionMove implements Move {
    private final Transaction transaction;
    private final int account;

    /** Whether the move fails if there is not enough cash. */
    private final boolean constrained;

    public Transaction getTransaction() {
        return transaction;
    }

    public AddTransactionMove(int account, Transaction t) {
        if (null == t) {
            throw new NullPointerException();
        }

        this.account = account;
        this.transaction = t;
        constrained = false;
    }

    public AddTransactionMove(int account, Transaction t, boolean constrained) {
        this.account = account;
        this.transaction = t;
        this.constrained = constrained;

        if (null == t) {
            throw new NullPointerException();
        }
    }

    public MoveStatus tryDoMove(World w) {
        if (w.boundsContain(KEY.BANK_ACCOUNTS, account)) {
            if (this.constrained) {
                BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS,
                        this.account);
                long bankBalance = bankAccount.getCurrentBalance().getAmount();
                long transactionAmount = this.transaction.getValue().getAmount();
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

    public MoveStatus tryUndoMove(World w) {
        if (w.boundsContain(KEY.BANK_ACCOUNTS, account)) {
            BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS,
                    this.account);
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

    public MoveStatus doMove(World w) {
        MoveStatus ms = tryDoMove(w);

        if (ms.ok) {
            BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS,
                    this.account);
            bankAccount.addTransaction(this.transaction);
        }

        return ms;
    }

    public MoveStatus undoMove(World w) {
        MoveStatus ms = tryUndoMove(w);

        if (ms.ok) {
            BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS,
                    this.account);
            bankAccount.removeLastTransaction();
        }

        return ms;
    }

    public boolean equals(Object obj) {
        if (obj instanceof AddTransactionMove) {
            AddTransactionMove test = (AddTransactionMove)obj;

            return test.account == this.account &&
            test.transaction.equals(this.transaction);
        } else {
            return false;
        }
    }
}