/*
 * Created on 21-Jun-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.Money;


/**
 * A credit.
 *
 * @author Luke Lindsay
 *
 */
public class Receipt implements Transaction {
    private final Money amount;
    private final int category;

    public Receipt(Money m, int category) {
        this.amount = m;
        this.category = category;
    }

    public int hashCode() {
        int result;
        result = amount.hashCode();
        result = 29 * result + category;

        return result;
    }

    public Money getValue() {
        return amount;
    }

    public boolean equals(Object o) {
        if (o instanceof Receipt) {
            Receipt test = (Receipt)o;

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