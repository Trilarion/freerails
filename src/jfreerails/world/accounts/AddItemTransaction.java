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
    /** Constants to specify the category. */
    public static final int TRACK = 0;

    /** For example track. */
    private final int category;

    /** For example, standard track. */
    private final int type;

    /** For example, 4 tiles. */
    private final int quantity;
    private final Money amount;

    public boolean equals(Object obj) {
        if (obj instanceof AddItemTransaction) {
            AddItemTransaction test = (AddItemTransaction)obj;

            return this.amount.equals(test.amount) &&
            this.category == test.category && this.type == test.type &&
            this.quantity == test.quantity;
        } else {
            return false;
        }
    }

    public AddItemTransaction(int category, int type, int quantity, Money amount) {
        this.category = category;
        this.type = type;
        this.quantity = quantity;
        this.amount = amount;
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