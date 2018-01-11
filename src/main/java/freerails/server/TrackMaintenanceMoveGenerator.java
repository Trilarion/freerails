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
import freerails.world.ItemsTransactionAggregator;
import freerails.world.SKEY;
import freerails.world.World;
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
     * @param mr
     */
    public TrackMaintenanceMoveGenerator(MoveReceiver mr) {
        moveReceiver = mr;
    }

    /**
     * @param world
     * @param principal
     * @param category
     * @return
     */
    public static Move generateMove(World world, FreerailsPrincipal principal, TransactionCategory category) {
        if (TransactionCategory.TRACK_MAINTENANCE != category && TransactionCategory.STATION_MAINTENANCE != category) {
            throw new IllegalArgumentException(String.valueOf(category));
        }

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, principal);
        aggregator.setCategory(TransactionCategory.TRACK);

        long amount = 0;

        for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule) world.get(SKEY.TRACK_RULES, i);
            long maintenanceCost = trackRule.getMaintenanceCost().getAmount();

            // Is the track type the category we are interested in?
            boolean rightType = TransactionCategory.TRACK_MAINTENANCE == category ? !trackRule.isStation() : trackRule.isStation();

            if (rightType) {
                aggregator.setType(i);
                amount += maintenanceCost * aggregator.calculateQuantity() / WorldConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE;
            }
        }

        Transaction t = new MoneyTransaction(new Money(-amount), category);

        return new AddTransactionMove(principal, t);
    }

    /**
     * @param w
     */
    public void update(World w) {
        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = w.getPlayer(i).getPrincipal();
            Move m = generateMove(w, principal, TransactionCategory.TRACK_MAINTENANCE);
            moveReceiver.process(m);

            m = generateMove(w, principal, TransactionCategory.STATION_MAINTENANCE);
            moveReceiver.process(m);
        }
    }
}