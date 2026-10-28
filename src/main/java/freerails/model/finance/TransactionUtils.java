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

/** Factory and query helpers for financial transactions. */
public class TransactionUtils {

    /** Prevents instantiation of this utility class. */
    private TransactionUtils() {
    }

    /**
     * Creates a transaction that issues one bond.
     *
     * @param interestRate annual interest rate stored with the bond
     * @param time time at which the bond is issued
     * @return bond issuance transaction
     */
    public static BondItemTransaction issueBond(double interestRate, Time time) {
        return new BondItemTransaction(ModelConstants.BOND_VALUE_ISSUE, time,1, interestRate);
    }

    /**
     * Creates a transaction that repays one bond.
     *
     * @param interestRate annual interest rate stored with the bond
     * @param time time at which the bond is repaid
     * @return bond repayment transaction
     */
    public static BondItemTransaction repayBond(int interestRate, Time time) {
        return new BondItemTransaction(ModelConstants.BOND_VALUE_REPAY, time,-1, interestRate);
    }

    /**
     * Creates a transaction recording the issue or withdrawal of shares.
     *
     * @param playerId identifier associated with the stock item
     * @param quantity number of shares
     * @param pricePerShare price per share
     * @param time time of the transaction
     * @return stock issuance transaction
     */
    public static Transaction issueStock(int playerId, int quantity, Money pricePerShare, Time time) {
        // Issue Stock of the Player
        Money amount = Money.multiply(pricePerShare, quantity);
        return new ItemTransaction(TransactionCategory.ISSUE_STOCK, amount, time, quantity, playerId);
    }

    /**
     * Creates a share-transfer transaction at the supplied price.
     *
     * @param playerId identifier of the company whose shares are transferred
     * @param quantity number of shares transferred
     * @param stockPrice price per share
     * @param time time of the transaction
     * @return stock transfer transaction
     */
    public static ItemTransaction buyOrSellStock(int playerId, int quantity, Money stockPrice, Time time) {
        // Buys another Players Stock, Uses another Category
        Money value = Money.multiply(stockPrice, -quantity);
        return new ItemTransaction(TransactionCategory.TRANSFER_STOCK, value, time, quantity, playerId);
    }

    /**
     * Tests whether the player has any positive recorded track quantity.
     *
     * @param world world containing the player's transactions
     * @param player player to examine
     * @return {@code true} if the player's net track quantity is positive
     */
    public static boolean hasAnyTrackBeenBuilt(UnmodifiableWorld world, Player player) {
        // TODO make this the default constructor in ItemsTransactionAggregator
        Time[] times = {Time.ZERO, world.getClock().getCurrentTime().advance()};
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player, times);
        aggregator.setCategory(TransactionCategory.TRACK);

        return aggregator.calculateQuantity() > 0;
    }

    /**
     * Values the player's track holdings from the specified start time through
     * the current game time, using each track type's purchase price.
     *
     * @param world world providing track types, clock, and transactions
     * @param player player whose track is valued
     * @param startTime inclusive start of the transaction interval
     * @return total value of the player's track holdings
     */
    public static Money calculateTrackTotal(UnmodifiableWorld world, Player player, Time startTime) {
        Time[] times = new Time[]{startTime, world.getClock().getCurrentTime()};
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player, times);
        aggregator.setCategory(TransactionCategory.TRACK);
        long amount = 0;

        for (TrackType trackType: world.getTrackTypes()) {
            // TODO Money arithmetic
            long trackValue = trackType.getPurchasingPrice().amount();
            aggregator.setType(trackType.getId());
            int quantity = aggregator.calculateQuantity();
            amount += trackValue * quantity / ModelConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE;
        }

        return new Money(amount);
    }
}
