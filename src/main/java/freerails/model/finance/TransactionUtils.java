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

package freerails.model.finance;

import freerails.model.ModelConstants;
import freerails.model.finance.transaction.BondItemTransaction;
import freerails.model.finance.transaction.ItemTransaction;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.finance.transaction.aggregator.ItemsTransactionAggregator;
import freerails.model.game.Time;
import freerails.model.player.Player;
import freerails.model.track.TrackType;
import freerails.model.world.UnmodifiableWorld;

/**
 *
 */
public class TransactionUtils {

    private TransactionUtils() {
    }

    /**
     * @param interestRate
     * @return
     */
    public static BondItemTransaction issueBond(double interestRate, Time time) {
        return new BondItemTransaction(ModelConstants.BOND_VALUE_ISSUE, time,1, interestRate);
    }

    /**
     * @param interestRate
     * @return
     */
    public static BondItemTransaction repayBond(int interestRate, Time time) {
        return new BondItemTransaction(ModelConstants.BOND_VALUE_REPAY, time,-1, interestRate);
    }

    /**
     * @param playerId
     * @param quantity
     * @param pricePerShare
     * @return
     */
    public static Transaction issueStock(int playerId, int quantity, Money pricePerShare, Time time) {
        // Issue Stock of the Player
        Money amount = Money.multiply(pricePerShare, quantity);
        return new ItemTransaction(TransactionCategory.ISSUE_STOCK, amount, time, quantity, playerId);
    }

    /**
     * @param playerId
     * @param quantity
     * @param stockPrice
     * @return
     */
    public static ItemTransaction buyOrSellStock(int playerId, int quantity, Money stockPrice, Time time) {
        // Buys another Players Stock, Uses another Category
        Money value = Money.multiply(stockPrice, -quantity);
        return new ItemTransaction(TransactionCategory.TRANSFER_STOCK, value, time, quantity, playerId);
    }

    /**
     * Returns true if some track has been built.
     */
    public static boolean hasAnyTrackBeenBuilt(UnmodifiableWorld world, Player player) {
        // TODO make this the default constructor in ItemsTransactionAggregator
        Time[] times = {Time.ZERO, world.getClock().getCurrentTime().advance()};
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player, times);
        aggregator.setCategory(TransactionCategory.TRACK);

        return aggregator.calculateQuantity() > 0;
    }

    /**
     * @param world
     * @param player
     * @param startTime
     * @return
     */
    public static Money calculateTrackTotal(UnmodifiableWorld world, Player player, Time startTime) {
        Time[] times = new Time[]{startTime, world.getClock().getCurrentTime()};
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player, times);
        aggregator.setCategory(TransactionCategory.TRACK);
        long amount = 0;

        for (TrackType trackType: world.getTrackTypes()) {
            // TODO Money arithmetic
            long trackValue = trackType.getPurchasingPrice().amount;
            aggregator.setType(trackType.getId());
            int quantity = aggregator.calculateQuantity();
            amount += trackValue * quantity / ModelConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE;
        }

        return new Money(amount);
    }
}
