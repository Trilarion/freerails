/*
 * Created on Dec 13, 2003
 */
package jfreerails.world.accounts;

import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.common.Money;

/**
 * A credit for delivering cargo.
 * 
 * @author Luke
 * 
 */
public class DeliverCargoReceipt extends Receipt {
    private static final long serialVersionUID = 3257009851963160372L;

    private final CargoBatch cb;

    private final int quantity;

    private final int stationId;

    private final int trainId;

    public DeliverCargoReceipt(Money m, int quantity, int stationId,
            CargoBatch cb, int trainId) {
        super(m, Category.CARGO_DELIVERY);
        this.stationId = stationId;
        this.quantity = quantity;
        this.cb = cb;
        this.trainId = trainId;
    }

    public int getTrainId() {
        return trainId;
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