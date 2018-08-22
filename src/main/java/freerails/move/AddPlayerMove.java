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

// TODO what about a remove of a player?
/**
 * Adds a player to the world.
 */
public class AddPlayerMove implements Move {

    private static final long serialVersionUID = 3977580277537322804L;
    private final Player player;

    private AddPlayerMove(Player player) {
        this.player = player;
    }

    /**
     * @param world
     * @param player
     * @return
     */
    public static AddPlayerMove generateMove(UnmodifiableWorld world, Player player) {
        // create a new player with a corresponding Player
        // TODO why is there a player already, just take a name, preferably in the constructor
        Player player2add = new Player(world.getPlayers().size(), player.getName());

        return new AddPlayerMove(player2add);
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

    @Override
    public Status applicable(UnmodifiableWorld world) {
        if (WorldUtils.isAlreadyASimilarPlayer(world, player))
            return Status.fail("There is already a player with the same name.");

        return Status.OK;
    }

    @Override
    public void apply(World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) throw new RuntimeException(status.getMessage());
        int playerId = world.addPlayer(player);
        // Sell the player 2 $500,000 bonds at 5% interest.
        Player player2 = player;
        world.addTransaction(player2, TransactionUtils.issueBond(5, world.getClock().getCurrentTime()));
        // Issue stock
        Money initialStockPrice = new Money(5);
        Transaction transaction = TransactionUtils.issueStock(playerId, 100000, initialStockPrice, world.getClock().getCurrentTime());
        world.addTransaction(player2, transaction);
    }
}