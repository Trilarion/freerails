/*
 * Created on Mar 29, 2004
 */
package jfreerails.world.accounts;

import jfreerails.world.common.Money;

/**
 * A Transaction that adds or removes a Bond.
 * 
 * @author Luke
 * 
 */
public class BondTransaction extends AddItemTransaction {
	private static final long serialVersionUID = 3257562923491473465L;

	public static final Money BOND_VALUE_ISSUE = new Money(500000);

	public static final Money BOND_VALUE_REPAY = new Money(-500000);

	private BondTransaction(Category category, int type, int quantity,
			Money amount) {
		super(category, type, quantity, amount);
	}

	public static BondTransaction issueBond(int interestRate) {
		return new BondTransaction(Category.BOND, interestRate, 1,
				BOND_VALUE_ISSUE);
	}

	public static BondTransaction repayBond(int interestRate) {
		return new BondTransaction(Category.BOND, interestRate, -1,
				BOND_VALUE_REPAY);
	}
}