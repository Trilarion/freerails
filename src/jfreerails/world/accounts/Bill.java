/*
 * Created on 21-Jun-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.Money;


/**
 * For example, the cost of buying a trains.
 * @author Luke Lindsay
 *
 */
public class Bill implements Transaction {
    private final Money m_amount;
    private final int m_category;

    public Bill(Money amount, int category) {
        m_amount = new Money(-amount.getAmount());
        m_category = category;
    }

    public Money getValue() {
        return m_amount;
    }

    public int hashCode() {
        int result;
        result = m_amount.hashCode();
        result = 29 * result + m_category;

        return result;
    }

    public boolean equals(Object o) {
        if (o instanceof Bill) {
            Bill test = (Bill)o;

            return test.m_amount.equals(m_amount) &&
            m_category == test.m_category;
        } else {
            return false;
        }
    }

    public int getCategory() {
        return m_category;
    }
}