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
    private final int category;

    public Bill(Money amount, int category) {
        this.amount = new Money(-amount.getAmount());
        this.category = category;
    }

    public Money getValue() {
        return amount;
    }

    public boolean equals(Object o) {
        if (o instanceof Bill) {
            Bill test = (Bill)o;

            return test.amount.equals(this.amount) &&
            category == test.category;
        } else {
            return false;
        }
    }

    public int getCategory() {
        return category;
    }
}