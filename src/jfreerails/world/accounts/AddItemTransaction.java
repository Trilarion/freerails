/*
 * Created on 02-Aug-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.Money;

/**
 * This Transaction represents the charge for building a piece of track.
 * 
 * @author Luke Lindsay
 *
 */
public class AddItemTransaction implements Transaction {

	/** Constants to specify the category. */
	public static final int TRACK = 0;

	/** E.g. track. */
	private final int category;

	/** E.g. standard track. */
	private final int type;

	/** E.g. 4 tiles. */
	private final int quantity;

	private final Money amount;

	public boolean equals(Object obj) {
		if (obj instanceof AddItemTransaction) {
			AddItemTransaction test = (AddItemTransaction) obj;
			return this.amount.equals(test.amount)
				&& this.category == test.category
				&& this.type == test.type
				&& this.quantity == test.quantity;
		} else {
			return false;
		}
	}

	public AddItemTransaction(
		int category,
		int type,
		int quantity,
		Money amount) {
		this.category = category;
		this.type = type;
		this.quantity = quantity;
		this.amount = amount;
	}

	public static int getTRACK() {
		return TRACK;
	}

	public int getCategory() {
		return category;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getType() {
		return type;
	}

	public Money getValue() {
		return amount;
	}
}
