/*
 * Copyright (C) 2003 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

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
