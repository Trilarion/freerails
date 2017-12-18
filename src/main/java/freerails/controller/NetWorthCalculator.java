package freerails.controller;

import freerails.world.accounts.AddItemTransaction;
import freerails.world.accounts.Transaction;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.TransactionAggregator;

/**
 * A TransactionAggregator that calculates the networth of a player by totalling
 * the value of their assets.
 *
 * @author Luke
 */
public class NetWorthCalculator extends TransactionAggregator {

    /**
     *
     * @param w
     * @param principal
     */
    public NetWorthCalculator(ReadOnlyWorld w, FreerailsPrincipal principal) {
        super(w, principal);
    }

    @Override
    protected boolean condition(int transactionID) {
        Transaction t = super.w.getTransaction(super.principal, transactionID);

        if (t instanceof AddItemTransaction) {
            return t.getCategory().equals(Transaction.Category.ISSUE_STOCK);
            // Since buying something is just converting one asset type to
            // another.
        }

        return true;
    }
}