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
import freerails.model.world.UnmodifiableWorld;
import freerails.util.Utils;
import freerails.model.world.World;
import freerails.model.player.Player;
import org.jetbrains.annotations.NotNull;

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
     * @param player
     * @param transaction
     */
    public AddTransactionMove(@NotNull Player player, @NotNull Transaction transaction) {
        this(player, transaction, false);
    }

    /**
     * @param player
     * @param transaction
     * @param constrain
     */
    public AddTransactionMove(@NotNull Player player, @NotNull Transaction transaction, boolean constrain) {
        this.player = player;
        this.transaction = Utils.verifyNotNull(transaction);
        cashConstrained = constrain;
    }

    @Override
    public int hashCode() {
        int result;
        result = transaction.hashCode();
        result = 29 * result + player.hashCode();
        result = 29 * result + (cashConstrained ? 1 : 0);

        return result;
    }

    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        // TODO should we check the time of the transaction here? actually it could have already been forwarded
        if (cashConstrained) {
            // TODO Money arithmetic
            long bankBalance = world.getCurrentBalance(player).amount;
            long transactionAmount = transaction.getAmount().amount;
            long balanceAfter = bankBalance + transactionAmount;

            if (transactionAmount < 0 && balanceAfter < 0) {
                return Status.fail("You can't afford that!");
            }
        }

        return Status.OK;
    }

    @Override
    public void apply(@NotNull World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) {
            throw new RuntimeException(status.getMessage());
        }

        world.addTransaction(player, transaction);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AddTransactionMove) {
            AddTransactionMove test = (AddTransactionMove) obj;

            return test.player.equals(player) && test.transaction.equals(transaction);
        }
        return false;
    }
}