/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.world.accounts;

import freerails.world.cargo.CargoBatch;
import freerails.world.common.Money;

/**
 * A credit for delivering cargo.
 *
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