/*
 * Created on 07-Jul-2003
 *
 */
package jfreerails.move;

import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;


/** This {@link Move} adds a {@link Transaction} to a players
 * {@link BankAccount}.
 * @author Luke Lindsay
 *
 */
public class AddTransactionMove implements Move {
    private final Transaction transaction;
    private final int accountNumer;

    /** Whether the move fails if there is not enough cash. */
    private final boolean constrained;

    public Transaction getTransaction() {
        return transaction;
    }

    public AddTransactionMove(int account, Transaction t) {
        if (null == t) {
            throw new NullPointerException();
        }

        this.accountNumer = account;
        this.transaction = t;
        constrained = false;
    }

    public AddTransactionMove(int account, Transaction t, boolean constrained) {
        this.accountNumer = account;
        this.transaction = t;
        this.constrained = constrained;

        if (null == t) {
            throw new NullPointerException();
        }
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (w.boundsContain(KEY.BANK_ACCOUNTS, accountNumer,
                    Player.TEST_PRINCIPAL)) {
            if (this.constrained) {
                BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS,
                        this.accountNumer, Player.TEST_PRINCIPAL);
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

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        if (w.boundsContain(KEY.BANK_ACCOUNTS, accountNumer,
                    Player.TEST_PRINCIPAL)) {
            BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS,
                    this.accountNumer, Player.TEST_PRINCIPAL);
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
                    this.accountNumer, Player.TEST_PRINCIPAL);
            bankAccount.addTransaction(this.transaction);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);

        if (ms.ok) {
            BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS,
                    this.accountNumer, Player.TEST_PRINCIPAL);
            bankAccount.removeLastTransaction();
        }

        return ms;
    }

    public boolean equals(Object obj) {
        if (obj instanceof AddTransactionMove) {
            AddTransactionMove test = (AddTransactionMove)obj;

            return test.accountNumer == this.accountNumer &&
            test.transaction.equals(this.transaction);
        } else {
            return false;
        }
    }
}