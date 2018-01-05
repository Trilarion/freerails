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
 */
public class StockItemTransaction extends ItemTransaction {

    private static final long serialVersionUID = 3256441412924224824L;

    private StockItemTransaction(TransactionCategory category, int playerId, int quantity,
                                 Money amount) {

        super(category, playerId, quantity, amount);
        if (playerId < 0)
            throw new IllegalArgumentException();
    }

    @SuppressWarnings("unused")
    private StockItemTransaction(int quantity, Money amount) {
        super(TransactionCategory.ISSUE_STOCK, -1, quantity, amount);
    }

    /**
     * @param playerId
     * @param quantity
     * @param pricePerShare
     * @return
     */
    public static StockItemTransaction issueStock(int playerId, int quantity,
                                                  Money pricePerShare) {
        // Issue Stock of the Player
        long temp = (pricePerShare.getAmount() * quantity);
        temp = 0L - temp;
        Money amount = Money.changeSign(new Money(temp));
        return new StockItemTransaction(TransactionCategory.ISSUE_STOCK, playerId,
                quantity, amount);
    }

    /**
     * @param playerId
     * @param quantity
     * @param stockPrice
     * @return
     */
    public static StockItemTransaction buyOrSellStock(int playerId, int quantity,
                                                      Money stockPrice) {
        // Buys another Players Stock, Uses another Category
        Money value = new Money(stockPrice.getAmount() * quantity * -1);
        return new StockItemTransaction(TransactionCategory.TRANSFER_STOCK,
                playerId, quantity, value);
    }

}