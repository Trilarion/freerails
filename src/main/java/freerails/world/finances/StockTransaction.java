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
 * Created on 04-Oct-2004
 *
 */
package freerails.world.finances;

/**
 * A transaction that occurs when a new company is founded or when a company
 * issues additional shares.
 */
public class StockTransaction extends AddItemTransaction {

    /**
     *
     */
    public static final int STOCK_BUNDLE_SIZE = 10000;
    private static final long serialVersionUID = 3256441412924224824L;

    private StockTransaction(Category category, int playerId, int quantity,
                             Money amount) {

        super(category, playerId, quantity, amount);
        if (playerId < 0)
            throw new IllegalArgumentException();
    }

    private StockTransaction(int quantity, Money amount) {
        super(Transaction.Category.ISSUE_STOCK, -1, quantity, amount);
    }

    /**
     * @param playerId
     * @param quantity
     * @param pricePerShare
     * @return
     */
    public static StockTransaction issueStock(int playerId, int quantity,
                                              Money pricePerShare) {
        // Issue Stock of the Player
        long temp = (pricePerShare.getAmount() * quantity);
        temp = 0L - temp;
        Money amount = new Money(temp).changeSign();
        return new StockTransaction(Transaction.Category.ISSUE_STOCK, playerId,
                quantity, amount);
    }

    /**
     * @param playerId
     * @param quantity
     * @param stockPrice
     * @return
     */
    public static StockTransaction buyOrSellStock(int playerId, int quantity,
                                                  Money stockPrice) {
        // Buys another Players Stock, Uses another Category
        Money value = new Money(stockPrice.getAmount() * quantity * -1);
        return new StockTransaction(Transaction.Category.TRANSFER_STOCK,
                playerId, quantity, value);
    }

    /**
     * @param quantity
     * @param pricePerShare
     * @return
     */
    public static StockTransaction issueStock(int quantity, long pricePerShare) {
        Money amount = new Money(pricePerShare * quantity);

        return new StockTransaction(quantity, amount);
    }
}