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
    private static final long serialVersionUID = 3257009851963160372L;
	private final CargoBatch m_cb;
    private final int m_quantity;
    private final int m_stationId;
    private final int m_trainId;

    public DeliverCargoReceipt(Money m, int quantity, int stationId,
        CargoBatch cb, int trainId) {
        super(m, Category.CARGO_DELIVERY);
        m_stationId = stationId;
        m_quantity = quantity;
        m_cb = cb;
        m_trainId = trainId;
    }
    public int getTrainId() {
        return m_trainId;
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