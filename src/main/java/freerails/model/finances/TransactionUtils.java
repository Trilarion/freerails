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

package freerails.model.finances;

import freerails.model.ModelConstants;
import freerails.model.finances.transactions.BondItemTransaction;
import freerails.model.finances.transactions.ItemTransaction;
import freerails.model.finances.transactions.Transaction;
import freerails.model.finances.transactions.TransactionCategory;
import freerails.model.player.Player;
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
    public static BondItemTransaction issueBond(double interestRate) {
        return new BondItemTransaction(ModelConstants.BOND_VALUE_ISSUE, 1, interestRate);
    }

    /**
     * @param interestRate
     * @return
     */
    public static BondItemTransaction repayBond(int interestRate) {
        return new BondItemTransaction(ModelConstants.BOND_VALUE_REPAY, -1, interestRate);
    }

    /**
     * @param playerId
     * @param quantity
     * @param pricePerShare
     * @return
     */
    public static Transaction issueStock(int playerId, int quantity, Money pricePerShare) {
        // Issue Stock of the Player
        Money amount = Money.multiply(pricePerShare, quantity);
        return new ItemTransaction(TransactionCategory.ISSUE_STOCK, amount, quantity, playerId);
    }

    /**
     * @param playerId
     * @param quantity
     * @param stockPrice
     * @return
     */
    public static ItemTransaction buyOrSellStock(int playerId, int quantity, Money stockPrice) {
        // Buys another Players Stock, Uses another Category
        Money value = Money.multiply(stockPrice, -quantity);
        return new ItemTransaction(TransactionCategory.TRANSFER_STOCK, value, quantity, playerId);
    }

    /**
     * Returns true if some track has been built.
     */
    public static boolean hasAnyTrackBeenBuilt(UnmodifiableWorld world, Player player) {
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player);
        aggregator.setCategory(TransactionCategory.TRACK);

        return aggregator.calculateQuantity() > 0;
    }
}
