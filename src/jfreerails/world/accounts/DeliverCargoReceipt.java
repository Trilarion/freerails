/*
 * Created on Dec 13, 2003
 */
package jfreerails.world.accounts;

import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.common.GameTime;

/**
 *
 *  @author Luke
 *
 */
public class DeliverCargoReceipt extends Transaction {
    private final CargoBundle cargoDelivered;

    public DeliverCargoReceipt(GameTime time, long value, CargoBundle cb) {
        super(time, value);
        cargoDelivered = cb;
    }

    public CargoBundle getCargoDelivered() {
        return cargoDelivered;
    }
    
    public int getCategory() {
	return CATEGORY_REVENUE;
    }

    public int getSubcategory() {
	return SUBCATEGORY_NO_SUBCATEGORY;
    }
}
