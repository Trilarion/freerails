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
 *
 */
public class IssueStockTransaction extends AddItemTransaction {
    private IssueStockTransaction(int quantity, Money amount) {
        super(Transaction.Category.ISSUE_STOCK, -1, quantity, amount);
    }

    public static IssueStockTransaction issueStock(int quantity,
        long pricePerShare) {
        Money amount = new Money(pricePerShare * quantity);

        return new IssueStockTransaction(quantity, amount);
    }
}