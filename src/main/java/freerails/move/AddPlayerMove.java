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

package freerails.move;

import freerails.model.finance.*;
import freerails.model.finance.transaction.Transaction;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.model.world.WorldUtils;
import org.jetbrains.annotations.NotNull;

// TODO what about a remove of a player?
/**
 * Adds a player to the world.
 */
public class AddPlayerMove implements Move {

    private static final long serialVersionUID = 3977580277537322804L;
    private final Player player;

    public AddPlayerMove(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddPlayerMove)) return false;

        final AddPlayerMove addPlayerMove = (AddPlayerMove) o;

        return player.equals(addPlayerMove.player);
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }

    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        if (WorldUtils.isAlreadyASimilarPlayer(world, player))
            return Status.fail("There is already a player with the same name.");

        return Status.OK;
    }

    @Override
    public void apply(@NotNull World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) throw new RuntimeException(status.getMessage());
        int playerId = world.addPlayer(player);
        // Sell the player 2 $500,000 bonds at 5% interest.
        world.addTransaction(player, TransactionUtils.issueBond(5, world.getClock().getCurrentTime()));
        // Issue stock
        Money initialStockPrice = new Money(5);
        Transaction transaction = TransactionUtils.issueStock(playerId, 100000, initialStockPrice, world.getClock().getCurrentTime());
        world.addTransaction(player, transaction);
    }
}