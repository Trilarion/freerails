/*
 * Created on 21-Jun-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.Money;


/**
 * @author Luke Lindsay
 *
 */
public class Bill implements Transaction {
    private final Money amount;

    public Bill(Money amount) {
        this.amount = new Money(-amount.getAmount());
    }

    public Money getValue() {
        return amount;
    }

    public boolean equals(Object o) {
        if (o instanceof Bill) {
            Bill test = (Bill)o;

            return test.amount.equals(this.amount);
        } else {
            return false;
        }
    }
}