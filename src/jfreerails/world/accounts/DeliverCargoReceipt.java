/*
 * Created on Dec 13, 2003
 */
package jfreerails.world.accounts;

import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.common.Money;


/**
 *
 *  @author Luke
 *
 */
public class DeliverCargoReceipt extends Receipt {
    private final CargoBatch cb;
    private final int quantity;
    private final int stationId;

    public DeliverCargoReceipt(Money m, int quantity, int stationId,
        CargoBatch cb) {
        super(m, CARGO_DELIVERY);
        this.stationId = stationId;
        this.quantity = quantity;
        this.cb = cb;
    }

    public CargoBatch getCb() {
        return cb;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getStationId() {
        return stationId;
    }
}