/*
 * Created on 02-Aug-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.Money;


/**
 * This Transaction represents the charge/credit for buying/selling an item.
 *
 * @author Luke Lindsay
 *
 */
public class AddItemTransaction implements Transaction {
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("AddItemTransaction ");
		sb.append(m_category);
		sb.append(", type ");
		sb.append(m_type);
		sb.append(", quantity ");
		sb.append(m_quantity);
		sb.append(", amount ");
		sb.append(m_amount);
		return sb.toString();
	}
    /** For example track. */
    private final Category m_category;

    public int hashCode() {
        int result;
        result = m_category.hashCode();
        result = 29 * result + m_type;
        result = 29 * result + m_quantity;
        result = 29 * result + m_amount.hashCode();

        return result;
    }

    /** For example, standard track. */
    private final int m_type;

    /** For example, 4 tiles. */
    private final int m_quantity;
    private final Money m_amount;

    public boolean equals(Object obj) {
        if (obj instanceof AddItemTransaction) {
            AddItemTransaction test = (AddItemTransaction)obj;

            return this.m_amount.equals(test.m_amount) &&
            m_category == test.m_category && m_type == test.m_type &&
            m_quantity == test.m_quantity;
        }
		return false;
    }

    public AddItemTransaction(Category category, int type, int quantity, Money amount) {
        m_category = category;
        m_type = type;
        m_quantity = quantity;
        m_amount = amount;
    }

    public Category getCategory() {
        return m_category;
    }

    public int getQuantity() {
        return m_quantity;
    }

    public int getType() {
        return m_type;
    }

    public Money getValue() {
        return m_amount;
    }
}