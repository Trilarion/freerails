/*
 * Created on 04-Oct-2004
 *
 */
package freerails.world.accounts;

import freerails.world.common.Money;

/**
 * A transaction that occurs when a new company is founded or when a company
 * issues additional shares.
 *
 * @author Luke
 * @author smackay
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
     *
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
     *
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
     *
     * @param quantity
     * @param pricePerShare
     * @return
     */
    public static StockTransaction issueStock(int quantity, long pricePerShare) {
        Money amount = new Money(pricePerShare * quantity);

        return new StockTransaction(quantity, amount);
    }
}