package freerails.model.finances;

import freerails.model.ModelConstants;
import freerails.model.finances.transactions.BondItemTransaction;
import freerails.model.finances.transactions.ItemTransaction;
import freerails.model.finances.transactions.Transaction;
import freerails.model.finances.transactions.TransactionCategory;

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
}
