/*
 * Created on Mar 29, 2004
 */
package jfreerails.world.accounts;

import jfreerails.world.common.Money;


/**
 * A Transaction that adds or removes a Bond.
 * @author Luke
 *
 */
public class BondTransaction extends AddItemTransaction {
    public static final Money BOND_VALUE = new Money(500000);

    private BondTransaction(int category, int type, int quantity, Money amount) {
        super(category, type, quantity, amount);
    }

    public static BondTransaction issueBond(int interestRate) {
        return new BondTransaction(BOND, interestRate, 1, BOND_VALUE);
    }

    public static BondTransaction repayBond(int interestRate) {
        return new BondTransaction(BOND, interestRate, -1, BOND_VALUE);
    }
}