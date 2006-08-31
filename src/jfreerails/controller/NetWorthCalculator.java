package jfreerails.controller;

import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.TransactionAggregator;

/**
 * A TransactionAggregator that calculates the networth of a player by
 * totalling the value of their assets.
 * 
 * @author Luke
 * 
 */
public class NetWorthCalculator extends TransactionAggregator {

	public NetWorthCalculator(ReadOnlyWorld w, FreerailsPrincipal principal) {
		super(w, principal);
	}

	@Override
	protected boolean condition(int transactionID) {
		Transaction t = super.w.getTransaction(super.principal,
				transactionID);
		
		if (t instanceof AddItemTransaction) {
			if(t.getCategory().equals(Transaction.Category.ISSUE_STOCK)){
				return true;
			}
			// Since buying something is just converting one asset type to
			// another.
			return false;
		}
		
		return true;
	}
}