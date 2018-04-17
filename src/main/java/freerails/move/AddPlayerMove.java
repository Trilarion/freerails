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

import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.World;
import freerails.model.finances.BondItemTransaction;
import freerails.model.finances.Money;
import freerails.model.finances.StockItemTransaction;
import freerails.model.finances.Transaction;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.player.Player;

// TODO what about a remove of a player?
/**
 * Adds a player to the world.
 */
public class AddPlayerMove implements Move {

    private static final long serialVersionUID = 3977580277537322804L;
    private final Player playerToAdd;

    private AddPlayerMove(Player player) {
        playerToAdd = player;
    }

    /**
     * @param world
     * @param player
     * @return
     */
    public static AddPlayerMove generateMove(ReadOnlyWorld world, Player player) {
        // create a new player with a corresponding Principal
        Player player2add = new Player(player.getName(), world.getNumberOfPlayers());

        return new AddPlayerMove(player2add);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddPlayerMove)) return false;

        final AddPlayerMove addPlayerMove = (AddPlayerMove) o;

        return playerToAdd.equals(addPlayerMove.playerToAdd);
    }

    @Override
    public int hashCode() {
        return playerToAdd.hashCode();
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        if (isAlreadyASimilarPlayer(world))
            return MoveStatus.moveFailed("There is already a player with the same name.");

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        int numPlayers = world.getNumberOfPlayers();
        Player pp = world.getPlayer(numPlayers - 1);
        if (pp.equals(playerToAdd)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed("The last player is " + pp.getName() + "not " + playerToAdd.getName());
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveStatus moveStatus = tryDoMove(world, principal);
        if (!moveStatus.succeeds()) return moveStatus;
        int playerId = world.addPlayer(playerToAdd);
        // Sell the player 2 $500,000 bonds at 5% interest.
        FreerailsPrincipal principal2 = playerToAdd.getPrincipal();
        world.addTransaction(principal2, BondItemTransaction.issueBond(5));
        // Issue stock
        Money initialStockPrice = new Money(5);
        Transaction transaction = StockItemTransaction.issueStock(playerId, 100000, initialStockPrice);
        world.addTransaction(principal2, transaction);
        return moveStatus;
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveStatus moveStatus = tryUndoMove(world, principal);
        if (!moveStatus.succeeds()) return moveStatus;

        world.removeLastTransaction(playerToAdd.getPrincipal());
        world.removeLastTransaction(playerToAdd.getPrincipal());
        world.removeLastPlayer();

        return moveStatus;
    }

    private boolean isAlreadyASimilarPlayer(ReadOnlyWorld world) {
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            Player pp = world.getPlayer(i);
            if (pp.getName().equalsIgnoreCase(playerToAdd.getName())) {
                return true;
            }
        }
        return false;
    }
}