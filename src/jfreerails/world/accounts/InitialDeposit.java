/**
 * Represents an initial deposit on opening an account.
 *
 * @author rtuck99@users.berlios.de
 */
package jfreerails.world.accounts;

import jfreerails.world.common.GameTime;

public class InitialDeposit extends Transaction {
    public InitialDeposit(GameTime t, long value) {
	super(t, value);
    }

    public final int getCategory() {
	return CATEGORY_NO_CATEGORY;
    }

    public final int getSubcategory() {
	return SUBCATEGORY_NO_SUBCATEGORY;
    }
}
