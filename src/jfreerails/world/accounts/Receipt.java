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
	private static final long serialVersionUID = 3617576007066924596L;

	private final Money amount;

	private final Category category;

	public Receipt(Money m, Category category) {
		this.amount = m;
		this.category = category;
	}

	public int hashCode() {
		int result;
		result = amount.hashCode();
		result = 29 * result + category.hashCode();

		return result;
	}

	public Money getValue() {
		return amount;
	}

	public boolean equals(Object o) {
		if (o instanceof Receipt) {
			Receipt test = (Receipt) o;

			return test.amount.equals(amount)
					&& category == test.category;
		}
		return false;
	}

	public Category getCategory() {
		return category;
	}
}