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
package freerails.server;

import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.network.movereceiver.MoveReceiver;
import freerails.model.world.WorldKey;
import freerails.model.NonNullElementWorldIterator;
import freerails.model.world.World;
import freerails.model.WorldIterator;
import freerails.model.finances.Money;
import freerails.model.finances.MoneyTransaction;
import freerails.model.finances.Transaction;
import freerails.model.finances.TransactionCategory;
import freerails.model.player.FreerailsPrincipal;

/**
 * Iterates over the entries in the BankAccount and counts the number
 * of trains, then calculates the cost of maintenance.
 */
public class TrainMaintenanceMoveGenerator {

    private final MoveReceiver moveReceiver;

    /**
     * @param moveReceiver
     */
    public TrainMaintenanceMoveGenerator(MoveReceiver moveReceiver) {
        this.moveReceiver = moveReceiver;
    }

    private static Move generateMove(World world, FreerailsPrincipal principal) {
        WorldIterator trains = new NonNullElementWorldIterator(WorldKey.Trains, world, principal);
        int numberOfTrains = trains.size();
        long amount = numberOfTrains * 5000;
        Transaction transaction = new MoneyTransaction(new Money(-amount), TransactionCategory.TRAIN_MAINTENANCE);

        return new AddTransactionMove(principal, transaction);
    }

    /**
     * @param world
     */
    public void update(World world) {
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = world.getPlayer(i).getPrincipal();
            Move move = generateMove(world, principal);
            moveReceiver.process(move);
        }
    }
}