/*
 * Created on Dec 13, 2003
 */
package jfreerails.world.accounts;

import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.common.Money;


/**
 *
 *  @author Luke
 *
 */
public class DeliverCargoReceipt extends Receipt {
    private final CargoBundle cargoDelivered;

    public DeliverCargoReceipt(Money m, CargoBundle cb) {
        super(m);
        cargoDelivered = cb;
    }

    public CargoBundle getCargoDelivered() {
        return cargoDelivered;
    }
}