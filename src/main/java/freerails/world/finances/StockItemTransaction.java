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
package freerails.world.finances;

/**
 * A transaction that occurs when a new company is founded or when a company
 * issues additional shares.
 *
 * Additionally to the values necessary for an item transaction also a player id is needed.
 */
public class StockItemTransaction extends ItemTransaction {

    private static final long serialVersionUID = 3256441412924224824L;

    private StockItemTransaction(TransactionCategory category, int playerId, int quantity, Money amount) {

        super(category, playerId, quantity, amount);
        // TODO why should the player id ever be negative
        if (playerId < 0) throw new IllegalArgumentException();
    }

    // TODO Do these static methods have to be here?

    /**
     * @param playerId
     * @param quantity
     * @param pricePerShare
     * @return
     */
    public static Transaction issueStock(int playerId, int quantity, Money pricePerShare) {
        // Issue Stock of the Player
        Money amount = Money.multiply(pricePerShare, quantity);
        return new StockItemTransaction(TransactionCategory.ISSUE_STOCK, playerId, quantity, amount);
    }

    /**
     * @param playerId
     * @param quantity
     * @param stockPrice
     * @return
     */
    public static StockItemTransaction buyOrSellStock(int playerId, int quantity, Money stockPrice) {
        // Buys another Players Stock, Uses another Category
        Money value = Money.multiply(stockPrice, -quantity);
        return new StockItemTransaction(TransactionCategory.TRANSFER_STOCK, playerId, quantity, value);
    }
}