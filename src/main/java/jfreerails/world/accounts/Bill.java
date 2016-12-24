/*
 * Created on 21-Jun-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.Money;

/**
 * For example, the cost of buying a trains.
 * 
 * @author Luke Lindsay
 * 
 */
public class Bill implements Transaction {
    private static final long serialVersionUID = 3258416144497782835L;

    private final Money amount;

    private final Category category;

    public Bill(Money amount, Category category) {
        this.amount = new Money(-amount.getAmount());
        this.category = category;
    }

    public Money deltaAssets() {
        return amount.changeSign();
    }

    public Money deltaCash() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Bill) {
            Bill test = (Bill) o;

            return test.amount.equals(amount) && category == test.category;
        }
        return false;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public int hashCode() {
        int result;
        result = amount.hashCode();
        result = 29 * result + category.hashCode();

        return result;
    }
}