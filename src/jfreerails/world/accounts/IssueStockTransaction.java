/*
 * Created on 04-Oct-2004
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.Money;


/**
 * A transaction that occurs when a new company is founded or when a company issues
 * additional shares.
 *
 * @author Luke
 * @revised smackay
 */
public class IssueStockTransaction extends AddItemTransaction {
    private static final long serialVersionUID = 3256441412924224824L;

	private IssueStockTransaction(Category category, int playerId, int quantity, Money amount) {
        super(category, playerId, quantity, amount);
    }

    public static IssueStockTransaction issueStock(int playerId, int quantity,
        Money pricePerShare) {
        // Issue Stock of the Player 
        long temp = (pricePerShare.getAmount() * quantity);
        temp = temp - temp - temp;
        Money amount = new Money(temp);
        return new IssueStockTransaction(Transaction.Category.ISSUE_STOCK, playerId, quantity, amount);
    }
    
    public static IssueStockTransaction sellStock(int playerId, int quantity,
        Money pricePerShare) {
        // Sell Stock of the player
        long temp = (pricePerShare.getAmount() * quantity);
        Money amount = new Money(temp);
        
        return new IssueStockTransaction(Transaction.Category.SELL_STOCK, playerId, quantity, amount);
    }    
    public static IssueStockTransaction buyPlayerStock(int playerId, int quantity) {
        // Buys another Players Stock, Uses another Category
        return new IssueStockTransaction(Transaction.Category.BUY_PLAYER_STOCK, playerId, quantity, new Money(0));
    }
    public static IssueStockTransaction sellPlayerStock(int playerId, int quantity) {
        // Sells another Players Stock, Uses another Category
        return new IssueStockTransaction(Transaction.Category.SELL_PLAYER_STOCK, playerId, quantity, new Money(0));
    }
    public static IssueStockTransaction issueStock(int quantity,
            long pricePerShare) {
            Money amount = new Money(pricePerShare * quantity);

            return new IssueStockTransaction(quantity, amount);
        }
    
    
    private IssueStockTransaction(int quantity, Money amount) {
        super(Transaction.Category.ISSUE_STOCK, -1, quantity, amount);
}
}