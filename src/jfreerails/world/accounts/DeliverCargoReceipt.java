/*
 * Created on Dec 13, 2003
 */
package jfreerails.world.accounts;

import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.common.Money;


/**
 *  A credit for delivering cargo.
 *  @author Luke
 *
 */
public class DeliverCargoReceipt extends Receipt {
    private final CargoBatch m_cb;
    private final int m_quantity;
    private final int m_stationId;

    public DeliverCargoReceipt(Money m, int quantity, int stationId,
        CargoBatch cb) {
        super(m, Category.CARGO_DELIVERY);
        m_stationId = stationId;
        m_quantity = quantity;
        m_cb = cb;
    }

    public CargoBatch getCb() {
        return m_cb;
    }

    public int getQuantity() {
        return m_quantity;
    }

    public int getStationId() {
        return m_stationId;
    }
}