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
    private final Money m_amount;
    private final Category m_category;

    public Receipt(Money m, Category category) {
        m_amount = m;
        m_category = category;
    }

    public int hashCode() {
        int result;
        result = m_amount.hashCode();
        result = 29 * result + m_category.hashCode();

        return result;
    }

    public Money getValue() {
        return m_amount;
    }

    public boolean equals(Object o) {
        if (o instanceof Receipt) {
            Receipt test = (Receipt)o;

            return test.m_amount.equals(m_amount) &&
            m_category == test.m_category;
        }
		return false;
    }

    public Category getCategory() {
        return m_category;
    }
}