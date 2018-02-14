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
import freerails.world.finances.ItemsTransactionAggregator;
import freerails.world.SKEY;
import freerails.world.world.World;
import freerails.world.WorldConstants;
import freerails.world.finances.Money;
import freerails.world.finances.MoneyTransaction;
import freerails.world.finances.Transaction;
import freerails.world.finances.TransactionCategory;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.track.TrackRule;

/**
 * Iterates over the entries in the BankAccount and counts the number
 * of units of each track type, then calculates the cost of maintenance.
 */
public class TrackMaintenanceMoveGenerator {

    private final MoveReceiver moveReceiver;

    /**
     * @param moveReceiver
     */
    public TrackMaintenanceMoveGenerator(MoveReceiver moveReceiver) {
        this.moveReceiver = moveReceiver;
    }

    /**
     * @param world
     * @param principal
     * @param category
     * @return
     */
    private static Move generateMove(World world, FreerailsPrincipal principal, TransactionCategory category) {
        if (TransactionCategory.TRACK_MAINTENANCE != category && TransactionCategory.STATION_MAINTENANCE != category) {
            throw new IllegalArgumentException(String.valueOf(category));
        }

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, principal);
        aggregator.setCategory(TransactionCategory.TRACK);

        long amount = 0;

        for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule) world.get(SKEY.TRACK_RULES, i);
            // TODO Money arithmetics
            long maintenanceCost = trackRule.getMaintenanceCost().amount;

            // Is the track type the category we are interested in?
            boolean rightType = TransactionCategory.TRACK_MAINTENANCE == category ? !trackRule.isStation() : trackRule.isStation();

            if (rightType) {
                aggregator.setType(i);
                amount += maintenanceCost * aggregator.calculateQuantity() / WorldConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE;
            }
        }

        Transaction transaction = new MoneyTransaction(new Money(-amount), category);

        return new AddTransactionMove(principal, transaction);
    }

    /**
     * @param world
     */
    public void update(World world) {
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = world.getPlayer(i).getPrincipal();
            Move move = generateMove(world, principal, TransactionCategory.TRACK_MAINTENANCE);
            moveReceiver.process(move);

            move = generateMove(world, principal, TransactionCategory.STATION_MAINTENANCE);
            moveReceiver.process(move);
        }
    }
}