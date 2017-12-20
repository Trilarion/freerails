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
import freerails.network.MoveReceiver;
import freerails.world.KEY;
import freerails.world.NonNullElementWorldIterator;
import freerails.world.World;
import freerails.world.finances.Money;
import freerails.world.finances.MoneyTransaction;
import freerails.world.finances.Transaction;
import freerails.world.finances.TransactionCategory;
import freerails.world.player.FreerailsPrincipal;

/**
 * This class iterates over the entries in the BankAccount and counts the number
 * of trains, then calculates the cost of maintenance.
 */
public class TrainMaintenanceMoveGenerator {
    private final MoveReceiver moveReceiver;

    /**
     * @param mr
     */
    public TrainMaintenanceMoveGenerator(MoveReceiver mr) {
        this.moveReceiver = mr;
    }

    private static AddTransactionMove generateMove(World w,
                                                   FreerailsPrincipal principal) {
        NonNullElementWorldIterator trains = new NonNullElementWorldIterator(KEY.TRAINS, w, principal);
        int numberOfTrains = trains.size();
        long amount = numberOfTrains * 5000;
        Transaction t = new MoneyTransaction(new Money(amount),
                TransactionCategory.TRAIN_MAINTENANCE);

        return new AddTransactionMove(principal, t);
    }

    /**
     * @param w
     */
    public void update(World w) {
        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = w.getPlayer(i).getPrincipal();
            Move m = generateMove(w, principal);
            moveReceiver.process(m);
        }
    }
}