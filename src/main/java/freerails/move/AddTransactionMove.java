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
 *
 */
package freerails.move;

import freerails.model.finance.transaction.Transaction;
import freerails.nove.Status;
import freerails.util.Utils;
import freerails.model.world.World;
import freerails.model.player.Player;

/**
 * This {@link Move} adds a {@link Transaction} to a players bank account on the
 * {@link World} object.
 */
public class AddTransactionMove implements Move {

    private static final long serialVersionUID = 3976738055925019701L;
    private final Transaction transaction;
    private final Player player;

    /**
     * Whether the move fails if there is not enough cash.
     */
    private final boolean cashConstrained;

    /**
     * @param account
     * @param transaction
     */
    public AddTransactionMove(Player account, Transaction transaction) {
        player = account;
        this.transaction = Utils.verifyNotNull(transaction);
        cashConstrained = false;
    }

    /**
     * @param account
     * @param transaction
     * @param constrain
     */
    public AddTransactionMove(Player account, Transaction transaction, boolean constrain) {
        player = account;
        this.transaction = Utils.verifyNotNull(transaction);
        cashConstrained = constrain;
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
        result = 29 * result + player.hashCode();
        result = 29 * result + (cashConstrained ? 1 : 0);

        return result;
    }

    public Status tryDoMove(World world, Player player) {
        if (cashConstrained) {
            // TODO Money arithmetic
            long bankBalance = world.getCurrentBalance(this.player).amount;
            long transactionAmount = transaction.getAmount().amount;
            long balanceAfter = bankBalance + transactionAmount;

            if (transactionAmount < 0 && balanceAfter < 0) {
                return Status.fail("You can't afford that!");
            }
        }

        return Status.OK;
    }

    public Status tryUndoMove(World world, Player player) {
        int size = world.getTransactions(this.player).size();

        if (0 == size) {
            return Status.fail("No transaction to remove!");
        }

        Transaction lastTransaction = world.getTransaction(this.player, size - 1);

        if (lastTransaction.equals(transaction)) {
            return Status.OK;
        }
        return Status.fail("Expected " + transaction + "but found " + lastTransaction);
    }

    public Status doMove(World world, Player player) {
        Status status = tryDoMove(world, player);

        if (status.isSuccess()) {
            world.addTransaction(this.player, transaction);
        }

        return status;
    }

    public Status undoMove(World world, Player player) {
        Status status = tryUndoMove(world, player);

        if (status.isSuccess()) {
            world.removeLastTransaction(this.player);
        }

        return status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AddTransactionMove) {
            AddTransactionMove test = (AddTransactionMove) obj;

            return test.player.equals(player) && test.transaction.equals(transaction);
        }
        return false;
    }

    /**
     * @return
     */
    public Player getPlayer() {
        return player;
    }
}