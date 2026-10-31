/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.economy.transaction.Transaction;
import freerails.model.world.UnmodifiableWorld;
import freerails.util.Utils;
import freerails.model.world.World;
import freerails.model.player.Player;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

/**
 * This {@link Move} adds a {@link Transaction} to a players bank account on the
 * {@link World} object.
 */
public class AddTransactionMove implements Move {

    @Serial
    private static final long serialVersionUID = 3976738055925019701L;
    private final Transaction transaction;
    private final Player player;

    /**
     * Whether the move fails if there is not enough cash.
     */
    private final boolean cashConstrained;

    /**
     * Creates a transaction move without enforcing cash constraints.
     *
     * @param player the player who owns or performs the operation
     * @param transaction the financial transaction to apply
     */
    public AddTransactionMove(@NotNull Player player, @NotNull Transaction transaction) {
        this(player, transaction, false);
    }

    /**
     * Creates a transaction move with the requested cash constraint.
     *
     * @param player the player who owns or performs the operation
     * @param transaction the financial transaction to apply
     * @param constrain whether transaction constraints are enforced
     */
    public AddTransactionMove(@NotNull Player player, @NotNull Transaction transaction, boolean constrain) {
        this.player = player;
        this.transaction = Utils.verifyNotNull(transaction);
        cashConstrained = constrain;
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        int result;
        result = transaction.hashCode();
        result = 29 * result + player.hashCode();
        result = 29 * result + (cashConstrained ? 1 : 0);

        return result;
    }

    /**
     * Checks whether this move can be applied to the supplied world.
     *
     * @param world the world whose state is used or updated
     * @return a status indicating whether the move can be applied
     */
    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        // TODO should we check the time of the transaction here? actually it could have already been forwarded
        if (cashConstrained) {
            // TODO Money arithmetic
            long bankBalance = world.getCurrentBalance(player).amount();
            long transactionAmount = transaction.getAmount().amount();
            long balanceAfter = bankBalance + transactionAmount;

            if (transactionAmount < 0 && balanceAfter < 0) {
                return Status.fail("You can't afford that!");
            }
        }

        return Status.OK;
    }

    /**
     * Applies this move to the supplied world.
     *
     * @param world the world whose state is used or updated
     */
    @Override
    public void apply(@NotNull World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) {
            throw new RuntimeException(status.getMessage());
        }

        world.addTransaction(player, transaction);
    }

    /**
     * Returns whether this object is equal to the specified object.
     *
     * @param obj the object to compare with this object
     * @return true if this object is equal to the specified object, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AddTransactionMove test) {

            return test.player.equals(player)
                    && test.transaction.equals(transaction)
                    && test.cashConstrained == cashConstrained;
        }
        return false;
    }
}