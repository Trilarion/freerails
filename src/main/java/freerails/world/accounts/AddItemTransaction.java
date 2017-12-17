/*
 * Created on 02-Aug-2003
 *
 */
package freerails.world.accounts;

import freerails.world.common.Money;

/**
 * This Transaction represents the charge/credit for buying/selling an item.
 *
 * @author Luke Lindsay
 */
public class AddItemTransaction implements Transaction {

    private static final long serialVersionUID = 3690471411852326457L;

    private final Money amount;

    /**
     * For example track.
     */
    private final Category category;

    /**
     * For example, 4 tiles.
     */
    private final int quantity;

    /**
     * For example, standard track.
     */
    private final int type;

    public AddItemTransaction(Category category, int type, int quantity,
                              Money amount) {
        this.category = category;
        this.type = type;
        this.quantity = quantity;
        this.amount = amount;
    }

    public Money deltaAssets() {
        return amount.changeSign();
    }

    public Money deltaCash() {
        return amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AddItemTransaction) {
            AddItemTransaction test = (AddItemTransaction) obj;

            return this.amount.equals(test.amount) && category == test.category
                    && type == test.type && quantity == test.quantity;
        }
        return false;
    }

    public Category getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int result;
        result = category.hashCode();
        result = 29 * result + type;
        result = 29 * result + quantity;
        result = 29 * result + amount.hashCode();

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AddItemTransaction ");
        sb.append(category);
        sb.append(", type ");
        sb.append(type);
        sb.append(", quantity ");
        sb.append(quantity);
        sb.append(", amount ");
        sb.append(amount);
        return sb.toString();
    }
}