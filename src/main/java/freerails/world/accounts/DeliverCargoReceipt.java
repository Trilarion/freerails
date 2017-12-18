/*
 * Created on Dec 13, 2003
 */
package freerails.world.accounts;

import freerails.world.cargo.CargoBatch;
import freerails.world.common.Money;

/**
 * A credit for delivering cargo.
 *
 * @author Luke
 */
public class DeliverCargoReceipt extends Receipt {
    private static final long serialVersionUID = 3257009851963160372L;

    private final CargoBatch cb;

    private final int quantity;

    private final int stationId;

    private final int trainId;

    /**
     *
     * @param m
     * @param quantity
     * @param stationId
     * @param cb
     * @param trainId
     */
    public DeliverCargoReceipt(Money m, int quantity, int stationId,
                               CargoBatch cb, int trainId) {
        super(m, Category.CARGO_DELIVERY);
        this.stationId = stationId;
        this.quantity = quantity;
        this.cb = cb;
        this.trainId = trainId;
    }

    /**
     *
     * @return
     */
    public int getTrainId() {
        return trainId;
    }

    /**
     *
     * @return
     */
    public CargoBatch getCb() {
        return cb;
    }

    /**
     *
     * @return
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     *
     * @return
     */
    public int getStationId() {
        return stationId;
    }
}